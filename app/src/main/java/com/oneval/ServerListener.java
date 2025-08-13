package com.oneval;

import org.json.JSONObject;

public interface ServerListener {
  void OnDataLoaded(JSONObject response);
}
