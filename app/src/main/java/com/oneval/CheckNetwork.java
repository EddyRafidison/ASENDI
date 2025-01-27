package com.oneval;
//modified from github

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import androidx.annotation.NonNull;

public class CheckNetwork {
    Context context;

    public CheckNetwork(Context context) {
        this.context = context;
    }

    // Network Check
    public void registerNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                                                                       @Override
                                                                       public void onAvailable(@NonNull Network network) {
                                                                           ONEVAL.isNetworkConnected = true;
                                                                       }

                                                                       @Override
                                                                       public void onLost(@NonNull Network network) {
                                                                           ONEVAL.isNetworkConnected = false;
                                                                       }
                                                                   }

                );
            }
        } catch (Exception e) {
            ONEVAL.isNetworkConnected = false;
        }
    }
}