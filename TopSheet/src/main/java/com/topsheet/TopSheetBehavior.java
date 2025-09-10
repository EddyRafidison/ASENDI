package com.topsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class TopSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
  public abstract static class TopSheetCallback {
    public abstract void onStateChanged(@NonNull View topSheet, @State int newState);

    public abstract void onSlide(@NonNull View topSheet, float slideOffset);
  }

  public static final int STATE_DRAGGING = 1;

  public static final int STATE_SETTLING = 2;

  public static final int STATE_EXPANDED = 3;

  public static final int STATE_COLLAPSED = 4;

  public static final int STATE_HIDDEN = 5;

  @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {}

  private static final float HIDE_THRESHOLD = 0.5f;
  private static final float HIDE_FRICTION = 0.1f;

  private float mMaximumVelocity;
  private int mPeekHeight;
  private int mMinOffset;
  private int mMaxOffset;
  private boolean mHideable;

  @State private int mState = STATE_COLLAPSED;

  private ViewDragHelper mViewDragHelper;
  private boolean mIgnoreEvents;
  private int mLastNestedScrollDy;
  private boolean mNestedScrolled;
  private WeakReference<V> mViewRef;
  private WeakReference<View> mNestedScrollingChildRef;
  private TopSheetCallback mCallback;
  private VelocityTracker mVelocityTracker;
  private int mActivePointerId;
  private int mInitialY;
  private boolean mTouchingScrollingChild;

  public TopSheetBehavior() {}

  @SuppressLint("PrivateResource")
  public TopSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout);
    setPeekHeight(
        a.getDimensionPixelSize(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0));
    setHideable(a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
    a.recycle();
    ViewConfiguration configuration = ViewConfiguration.get(context);
    mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }

  @Override
  public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull V child) {
    return new SavedState(super.onSaveInstanceState(parent, child), mState);
  }

  @Override
  public void onRestoreInstanceState(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(parent, child, ss.getSuperState());
    if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
      mState = STATE_COLLAPSED;
    } else {
      mState = ss.state;
    }
  }

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      WindowInsetsCompat parentInsets = ViewCompat.getRootWindowInsets(parent);
      WindowInsetsCompat childInsets = ViewCompat.getRootWindowInsets(child);

      if (parentInsets != null && childInsets == null) {
        child.setFitsSystemWindows(true);
      }
    } else {
      if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
        child.setFitsSystemWindows(true);
      }
    }

    int savedTop = child.getTop();
    parent.onLayoutChild(child, layoutDirection);

    mMinOffset = Math.max(-child.getHeight(), -(child.getHeight() - mPeekHeight));
    mMaxOffset = 0;

    if (mState == STATE_EXPANDED) {
      child.offsetTopAndBottom(mMaxOffset - child.getTop());
    } else if (mHideable && mState == STATE_HIDDEN) {
      child.offsetTopAndBottom(-child.getHeight() - child.getTop());
    } else if (mState == STATE_COLLAPSED) {
      child.offsetTopAndBottom(mMinOffset - child.getTop());
    } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
      child.offsetTopAndBottom(savedTop - child.getTop());
    }

    if (mViewDragHelper == null) {
      mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
    }

    mViewRef = new WeakReference<>(child);
    mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, V child, @NonNull MotionEvent event) {
    if (!child.isShown()) {
      return false;
    }

    int action = event.getActionMasked();

    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }

    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(event);

    switch (action) {
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        mTouchingScrollingChild = false;
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;
        if (mIgnoreEvents) {
          mIgnoreEvents = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_DOWN:
        int initialX = (int) event.getX();
        mInitialY = (int) event.getY();
        View scroll = mNestedScrollingChildRef.get();
        if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
          mActivePointerId = event.getPointerId(event.getActionIndex());
          mTouchingScrollingChild = true;
        }
        mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID
            && !parent.isPointInChildBounds(child, initialX, mInitialY);
        break;
    }

    if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event)) {
      return true;
    }

    View scroll = mNestedScrollingChildRef.get();
    return action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents
        && mState != STATE_DRAGGING
        && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY())
        && Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
  }

  @Override
  public boolean onTouchEvent(
      @NonNull CoordinatorLayout parent, V child, @NonNull MotionEvent event) {
    if (!child.isShown()) {
      return false;
    }

    int action = event.getActionMasked();

    if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
      return true;
    }

    if (mViewDragHelper != null) {
      mViewDragHelper.processTouchEvent(event);
    }

    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }

    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(event);

    if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
      if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop()) {
        mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
      }
    }
    return !mIgnoreEvents;
  }

  @Override
  public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
      @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
    mLastNestedScrollDy = 0;
    mNestedScrolled = false;
    return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
      @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
    View scrollingChild = mNestedScrollingChildRef.get();
    if (target != scrollingChild) {
      return;
    }

    int currentTop = child.getTop();
    int newTop = currentTop - dy;

    if (dy > 0) {
      if (!target.canScrollVertically(1)) {
        if (newTop >= mMinOffset || mHideable) {
          consumed[1] = dy;
          child.offsetTopAndBottom(-dy);
          setStateInternal(STATE_DRAGGING);
        } else {
          consumed[1] = currentTop - mMinOffset;
          child.offsetTopAndBottom(-consumed[1]);
          setStateInternal(STATE_COLLAPSED);
        }
      }
    } else if (dy < 0) {
      if (newTop < mMaxOffset) {
        consumed[1] = dy;
        child.offsetTopAndBottom(-dy);
        setStateInternal(STATE_DRAGGING);
      } else {
        consumed[1] = currentTop - mMaxOffset;
        child.offsetTopAndBottom(-consumed[1]);
        setStateInternal(STATE_EXPANDED);
      }
    }

    dispatchOnSlide(child.getTop());
    mLastNestedScrollDy = dy;
    mNestedScrolled = true;
  }

  @Override
  public void onStopNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout, V child, @NonNull View target, int type) {
    if (child.getTop() == mMaxOffset) {
      setStateInternal(STATE_EXPANDED);
      return;
    }

    if (target != mNestedScrollingChildRef.get() || !mNestedScrolled) {
      return;
    }

    int top;
    int targetState;

    if (mLastNestedScrollDy < 0) {
      top = mMaxOffset;
      targetState = STATE_EXPANDED;
    } else if (mHideable && shouldHide(child, getYVelocity())) {
      top = -child.getHeight();
      targetState = STATE_HIDDEN;
    } else if (mLastNestedScrollDy == 0) {
      int currentTop = child.getTop();
      if (Math.abs(currentTop - mMinOffset) > Math.abs(currentTop - mMaxOffset)) {
        top = mMaxOffset;
        targetState = STATE_EXPANDED;
      } else {
        top = mMinOffset;
        targetState = STATE_COLLAPSED;
      }
    } else {
      top = mMinOffset;
      targetState = STATE_COLLAPSED;
    }

    if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
      setStateInternal(STATE_SETTLING);
      child.postOnAnimation(new SettleRunnable(child, targetState));
    } else {
      setStateInternal(targetState);
    }
    mNestedScrolled = false;
  }

  @Override
  public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
      @NonNull View target, float velocityX, float velocityY) {
    return target == mNestedScrollingChildRef.get()
        && (mState != STATE_EXPANDED
            || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
  }

  public final void setPeekHeight(int peekHeight) {
    mPeekHeight = Math.max(0, peekHeight);
    if (mViewRef != null && mViewRef.get() != null) {
      mMinOffset =
          Math.max(-mViewRef.get().getHeight(), -(mViewRef.get().getHeight() - mPeekHeight));
    }
  }

  public void setHideable(boolean hideable) {
    mHideable = hideable;
  }

  public void setTopSheetCallback(TopSheetCallback callback) {
    mCallback = callback;
  }

  public final void setState(@State int state) {
    if (state == mState) {
      return;
    }

    if (mViewRef == null) {
      if (state == STATE_COLLAPSED || state == STATE_EXPANDED
          || (mHideable && state == STATE_HIDDEN)) {
        mState = state;
      }
      return;
    }

    V child = mViewRef.get();
    if (child == null) {
      return;
    }

    int top;
    if (state == STATE_COLLAPSED) {
      top = mMinOffset;
    } else if (state == STATE_EXPANDED) {
      top = mMaxOffset;
    } else if (mHideable && state == STATE_HIDDEN) {
      top = -child.getHeight();
    } else {
      throw new IllegalArgumentException("Illegal state argument: " + state);
    }

    setStateInternal(STATE_SETTLING);
    if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
      child.postOnAnimation(new SettleRunnable(child, state));
    }
  }

  private void setStateInternal(@State int state) {
    if (mState == state) {
      return;
    }
    mState = state;
    View topSheet = mViewRef.get();
    if (topSheet != null && mCallback != null) {
      mCallback.onStateChanged(topSheet, state);
    }
  }

  private void reset() {
    mActivePointerId = ViewDragHelper.INVALID_POINTER;
    if (mVelocityTracker != null) {
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    }
  }

  private boolean shouldHide(View child, float yvel) {
    if (child.getTop() > mMinOffset) {
      return false;
    }
    final float newTop = child.getTop() + yvel * HIDE_FRICTION;
    return Math.abs(newTop - mMinOffset) / (float) mPeekHeight > HIDE_THRESHOLD;
  }

  private View findScrollingChild(View view) {
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0, count = group.getChildCount(); i < count; i++) {
        View scrollingChild = findScrollingChild(group.getChildAt(i));
        if (scrollingChild != null) {
          return scrollingChild;
        }
      }
    }
    return null;
  }

  private float getYVelocity() {
    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
    return mVelocityTracker.getYVelocity(mActivePointerId);
  }

  private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {
    @Override
    public boolean tryCaptureView(@NonNull View child, int pointerId) {
      if (mState == STATE_DRAGGING) {
        return false;
      }
      if (mTouchingScrollingChild) {
        return false;
      }
      if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
        View scroll = mNestedScrollingChildRef.get();
        if (scroll != null && scroll.canScrollVertically(-1)) {
          return false;
        }
      }
      return mViewRef != null && mViewRef.get() == child;
    }

    @Override
    public void onViewPositionChanged(
        @NonNull View changedView, int left, int top, int dx, int dy) {
      dispatchOnSlide(top);
    }

    @Override
    public void onViewDragStateChanged(int state) {
      if (state == ViewDragHelper.STATE_DRAGGING) {
        setStateInternal(STATE_DRAGGING);
      }
    }

    @Override
    public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
      int top;
      @State int targetState;
      if (yvel > 0) {
        top = mMaxOffset;
        targetState = STATE_EXPANDED;
      } else if (mHideable && shouldHide(releasedChild, yvel)) {
        top = -releasedChild.getHeight();
        targetState = STATE_HIDDEN;
      } else if (yvel == 0.f) {
        int currentTop = releasedChild.getTop();
        if (Math.abs(currentTop - mMinOffset) > Math.abs(currentTop - mMaxOffset)) {
          top = mMaxOffset;
          targetState = STATE_EXPANDED;
        } else {
          top = mMinOffset;
          targetState = STATE_COLLAPSED;
        }
      } else {
        top = mMinOffset;
        targetState = STATE_COLLAPSED;
      }

      if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
        setStateInternal(STATE_SETTLING);
        releasedChild.postOnAnimation(new SettleRunnable(releasedChild, targetState));
      } else {
        setStateInternal(targetState);
      }
    }

    @Override
    public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
      return constrain(top, mHideable ? -child.getHeight() : mMinOffset, mMaxOffset);
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left, int dx) {
      return child.getLeft();
    }

    @Override
    public int getViewVerticalDragRange(@NonNull View child) {
      if (mHideable) {
        return child.getHeight();
      } else {
        return mMaxOffset - mMinOffset;
      }
    }
  };

  private void dispatchOnSlide(int top) {
    View topSheet = mViewRef.get();
    if (topSheet != null && mCallback != null) {
      if (top < mMinOffset) {
        mCallback.onSlide(topSheet, (float) (top - mMinOffset) / mPeekHeight);
      } else {
        mCallback.onSlide(topSheet, (float) (top - mMinOffset) / ((mMaxOffset - mMinOffset)));
      }
    }
  }

  private class SettleRunnable implements Runnable {
    private final View mView;
    @State private final int mTargetState;

    SettleRunnable(View view, @State int targetState) {
      mView = view;
      mTargetState = targetState;
    }

    @Override
    public void run() {
      if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
        mView.postOnAnimation(this);
      } else {
        setStateInternal(mTargetState);
      }
    }
  }

  protected static class SavedState extends AbsSavedState {
    @State final int state;

    public SavedState(Parcel source) {
      super(source);
      state = source.readInt();
    }

    public SavedState(Parcelable superState, @State int state) {
      super(superState);
      this.state = state;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(state);
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static <V extends View> TopSheetBehavior<V> from(V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<View> behavior =
        ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof TopSheetBehavior)) {
      throw new IllegalArgumentException("The view is not associated with TopSheetBehavior");
    }
    return (TopSheetBehavior<V>) behavior;
  }

  static int constrain(int amount, int low, int high) {
    return amount < low ? low : (Math.min(amount, high));
  }
}