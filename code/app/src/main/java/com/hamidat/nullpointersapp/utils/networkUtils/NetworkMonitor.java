/**
 * NetworkMonitor.java
 *
 * Monitors the device's internet connectivity and shows periodic Toast updates about the connection status.
 * Designed for use in background or UI-aware components to inform users of connectivity changes.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.networkUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Monitors network connectivity status and provides periodic updates.
 * <p>
 * Usage:
 * - Create instance with application context
 * - Call startMonitoring() to begin checks
 * - Call stopMonitoring() when done to prevent leaks
 */
public class NetworkMonitor {
    private final Context context;
    private final Handler handler;
    private Runnable networkCheckRunnable;
    private static final long CHECK_INTERVAL = 10000; // 10 seconds

    /**
     * Constructs a NetworkMonitor with application context.
     *
     * @param context Application context (use getApplicationContext() from activity)
     */
    public NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Starts periodic network connectivity checks.
     * Shows Toast immediately on first check and every 10 seconds thereafter.
     */
    public void startMonitoring() {
        networkCheckRunnable = new Runnable() {
            /**
             * Runs the network connectivity check and displays a Toast with the result.
             */
            @Override
            public void run() {
                boolean isConnected = isNetworkConnected();
                showConnectionToast(isConnected);
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(networkCheckRunnable);
    }

    /**
     * Stops network monitoring and removes scheduled checks.
     */
    public void stopMonitoring() {
        if (networkCheckRunnable != null) {
            handler.removeCallbacks(networkCheckRunnable);
        }
    }

    /**
     * Checks current network connectivity status.
     *
     * @return true if device has active internet connection, false otherwise
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    /**
     * Displays connection status Toast message.
     *
     * @param isConnected Current network connection status
     */
    private void showConnectionToast(boolean isConnected) {
        String message = isConnected ? "Connected to internet" : "No internet connection";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}