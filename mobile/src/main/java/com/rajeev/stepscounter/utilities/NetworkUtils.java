package com.rajeev.stepscounter.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    /**
     * Check whether there is an internet connection or not
     *
     * @param context The context being passed
     * @return Returns whether the internet is working or not
     */
    public static boolean hasNetworkConnection(Context context) {
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] allNetworks = cm.getAllNetworks(); // added in API 21 (Lollipop)

        for (Network network : allNetworks) {
            NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    isConnected = true;
            }
        }
        return isConnected;
    }

}
