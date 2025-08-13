package com.oneval;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.List;

public class HistoryAdapter extends ArrayAdapter<HistoryItem> {
  private final Activity context;

  public HistoryAdapter(Activity context, List<HistoryItem> historyList) {
    super(context, 0, historyList);
    this.context = context;
    int listSize = historyList.size();
    View v = context.findViewById(R.id.empty_data_layout);
    if (listSize >= 1) {
      v.setVisibility(View.INVISIBLE);
    } else {
      v.setVisibility(View.VISIBLE);
    }
  }

  @SuppressLint("SuspiciousIndentation")
  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    @SuppressLint({"ViewHolder", "InflateParams"})
    View view = LayoutInflater.from(context).inflate(R.layout.history_item_layout, null);
    TextView date = view.findViewById(R.id.transfer_date);
    TextView detail = view.findViewById(R.id.transfer_info);
    ImageView type = view.findViewById(R.id.transfer_type);
    TextView time = view.findViewById(R.id.transfer_time);

    HistoryItem history = getItem(position);

    assert history != null;
    date.setText(history.getDMYTime());
    time.setText(history.getHMTime());
    detail.setText(history.getDetail());
    type.setImageResource(history.getDrawable());

    return view;
  }
}
