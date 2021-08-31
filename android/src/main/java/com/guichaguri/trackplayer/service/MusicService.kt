package com.guichaguri.trackplayer.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media.session.MediaButtonReceiver
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

/**
 * @author Guichaguri
 */
class MusicService : HeadlessJsTaskService() {
    var manager: MusicManager? = null
    var handler: Handler? = null
    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        return HeadlessJsTaskConfig("TrackPlayer", Arguments.createMap(), 0, true)
    }

    override fun onHeadlessJsTaskFinish(taskId: Int) {
        // Overridden to prevent the service from being terminated
    }

    fun emit(event: String?, data: Bundle?) {
        val intent = Intent(Utils.EVENT_INTENT)
        intent.putExtra("event", event)
        if (data != null) intent.putExtra("data", data)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun destroy() {
        if (handler != null) {
            handler!!.removeMessages(0)
            handler = null
        }
        if (manager != null) {
            manager!!.destroy()
            manager = null
        }
    }

    private fun onStartForeground() {
        var serviceForeground = false
        if (manager != null) {
            // The session is only active when the service is on foreground
            serviceForeground = manager!!.metadata.session.isActive
        }
        if (!serviceForeground) {
            val reactInstanceManager = reactNativeHost.reactInstanceManager
            val reactContext = reactInstanceManager.currentReactContext

            // Checks whether there is a React activity
            if (reactContext == null || !reactContext.hasCurrentActivity()) {
                val channel = Utils.getNotificationChannel(this as Context)

                // Sets the service to foreground with an empty notification
                startForeground(1, NotificationCompat.Builder(this, channel!!).build())
                // Stops the service right after
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return if (Utils.CONNECT_INTENT == intent.action) {
            MusicBinder(this, manager)
        } else super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null && Intent.ACTION_MEDIA_BUTTON == intent.action) {
            // Check if the app is on background, then starts a foreground service and then ends it right after
            onStartForeground()
            if (manager != null) {
                MediaButtonReceiver.handleIntent(manager!!.metadata.session, intent)
            }
            return START_NOT_STICKY
        }
        manager = MusicManager(this)
        handler = Handler()
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val channel = Utils.getNotificationChannel(this as Context)
        startForeground(1, NotificationCompat.Builder(this, channel!!).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy()
        stopForeground(true)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        if (manager == null || manager!!.shouldStopWithApp()) {
            if (manager != null) {
                manager?.playback?.stop()
            }
            destroy()
            stopSelf()
        }
    }
}