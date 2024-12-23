package com.asendi;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public interface ServerListener {
    void OnDataLoaded(JSONObject response);
}