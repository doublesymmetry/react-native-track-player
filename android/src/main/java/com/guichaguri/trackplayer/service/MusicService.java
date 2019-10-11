package com.guichaguri.trackplayer.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.session.MediaButtonReceiver;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;
import android.annotation.SuppressLint;


/**
 * @author Guichaguri
 */
public class MusicService extends HeadlessJsTaskService {

    MusicManager manager;
    Handler handler;

    private WifiLock wifiLock;
    private WakeLock wakeLock;

    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        return new HeadlessJsTaskConfig("TrackPlayer", Arguments.createMap(), 0, true);
    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        // Overridden to prevent the service from being terminated
    }

    public void emit(String event, Bundle data) {
        Intent intent = new Intent(Utils.EVENT_INTENT);

        intent.putExtra("event", event);
        if(data != null) intent.putExtra("data", data);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void destroy() {
        if(handler != null) {
            handler.removeMessages(0);
            handler = null;
        }

        if(manager != null) {
            manager.destroy();
            manager = null;
        }
    }

    private void onStartForeground() {
        boolean serviceForeground = false;

        if(manager != null) {
            // The session is only active when the service is on foreground
            serviceForeground = manager.getMetadata().getSession().isActive();
        }

        if(!serviceForeground) {
            ReactInstanceManager reactInstanceManager = getReactNativeHost().getReactInstanceManager();
            ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

            // Checks whether there is a React activity
            if(reactContext == null || !reactContext.hasCurrentActivity()) {
                // Sets the service to foreground with an empty notification
                startForeground(Utils.NOTIFICATION_ID, new NotificationCompat.Builder(this, Utils.NOTIFICATION_CHANNEL).setVisibility(NotificationCompat.VISIBILITY_SECRET).build());
                // Stops the service right after
                stopSelf();
            }
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Utils.NOTIFICATION_CHANNEL, "Playback", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(Utils.SERVICE_NAME);
            channel.setShowBadge(false);
            channel.setSound(null, null);

            NotificationManager not = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            not.createNotificationChannel(channel);
        }

        this.createWifiLock();
        this.newWakeLock();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(Utils.CONNECT_INTENT.equals(intent.getAction())) {
            return new MusicBinder(this, manager);
        }

        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            // Check if the app is on background, then starts a foreground service and then ends it right after
            onStartForeground();
            
            if(manager != null) {
                MediaButtonReceiver.handleIntent(manager.getMetadata().getSession(), intent);
            }
            
            return START_NOT_STICKY;
        }

        manager = new MusicManager(this);
        handler = new Handler();

        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if (manager == null || manager.shouldStopWithApp()) {
            stopSelf();
        }
    }

    @SuppressLint("WifiManagerPotentialLeak")
    private void createWifiLock() {
        if(wifiLock != null) return;

        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, Utils.SERVICE_NAME);
        wifiLock.setReferenceCounted(false);
    }

    @SuppressLint("InvalidWakeLockTag")
    private void newWakeLock() {
        if(wakeLock != null) return;

        PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.SERVICE_NAME);
        wakeLock.setReferenceCounted(false);
    }

    @SuppressLint("WakelockTimeout")
    public void lockServices(boolean isLocal) {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        if (!isLocal && !wifiLock.isHeld()) {
            wifiLock.acquire();
        }
    }

    public void unlockServices() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
