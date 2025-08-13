package com.oneval;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AsyncTaskV2<Params, Progress, Result> {

  // This handler will be used to communicate with main thread
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final AtomicBoolean cancelled = new AtomicBoolean(false);

  private Result result;
  private Future<Result> resultFuture;
  private ExecutorService executor;

  // Base class must implement this method
  protected abstract Result doInBackground(Params params);

  // Methods with default implementation
  // Base class can optionally override these methods.
  protected void onPreExecute() {}

  protected void onPostExecute(Result result) {}

  protected void onProgressUpdate(Progress progress) {}

  protected void onCancelled() {}

  @MainThread
  public final void execute(@Nullable Params params) {
    onPreExecute();
    try {
      executor = Executors.newSingleThreadExecutor();
      Callable<Result> backgroundCallableTask = () -> doInBackground(params);
      // Execute the background task
      resultFuture = executor.submit(backgroundCallableTask);

      // On the worker thread — wait for the background task to complete
      executor.submit(this::getResult);
    } finally {
      if (executor != null) {
        executor.shutdown();
      }
    }
  }

  private Runnable getResult() {
    return () -> {
      try {
        if (isCancelled()) {
          // This will block the worker thread, till the result is available
          result = resultFuture.get();

          // Post the result to main thread
          handler.post(() -> onPostExecute(result));
        } else {
          // User cancelled the operation, ignore the result
          handler.post(this::onCancelled);
        }
      } catch (InterruptedException | ExecutionException ignored) {

      }
    };
  }

  @WorkerThread
  public final void publishProgress(Progress progress) {
    if (isCancelled()) {
      handler.post(() -> onProgressUpdate(progress));
    }
  }

  @MainThread
  public final void cancel(boolean mayInterruptIfRunning) {
    cancelled.set(true);
    if (resultFuture != null) {
      resultFuture.cancel(mayInterruptIfRunning);
    }
  }

  @AnyThread
  public final boolean isCancelled() {
    return !cancelled.get();
  }
}
