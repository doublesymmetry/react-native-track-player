package com.guichaguri.trackplayer.service.metadata

import android.annotation.SuppressLint
import com.facebook.react.bridge.ReactContext
import android.os.Bundle
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.guichaguri.trackplayer.service.MusicBinder
import com.guichaguri.trackplayer.module.MusicEvents
import android.os.IBinder
import com.guichaguri.trackplayer.service.MusicService
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.RatingCompat
import com.google.android.exoplayer2.Player
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.guichaguri.trackplayer.service.player.ExoPlayback
import com.guichaguri.trackplayer.service.models.NowPlayingMetadata
import com.google.android.exoplayer2.C
import com.guichaguri.trackplayer.service.models.TrackMetadata
import com.guichaguri.trackplayer.service.models.TrackType
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaDescriptionCompat
import com.guichaguri.trackplayer.service.player.LocalPlayback
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.guichaguri.trackplayer.service.MusicManager
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.ExoPlaybackException
import com.guichaguri.trackplayer.service.player.SourceMetadata
import com.google.android.exoplayer2.Player.MetadataComponent
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame
import com.google.android.exoplayer2.metadata.icy.IcyHeaders
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.metadata.flac.VorbisComment
import com.google.android.exoplayer2.extractor.mp4.MdtaMetadataEntry
import android.provider.MediaStore
import com.bumptech.glide.request.target.SimpleTarget
import android.graphics.Bitmap
import com.guichaguri.trackplayer.R
import android.app.NotificationManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.Glide
import android.os.Build
import androidx.media.session.MediaButtonReceiver
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper
import com.guichaguri.trackplayer.service.metadata.ButtonEvents
import android.app.PendingIntent
import android.app.NotificationChannel
import android.content.*
import com.guichaguri.trackplayer.service.metadata.MetadataManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.PowerManager.WakeLock
import android.net.wifi.WifiManager.WifiLock
import androidx.annotation.RequiresApi
import android.media.AudioFocusRequest
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import android.media.AudioManager
import android.net.Uri
import android.os.PowerManager
import android.net.wifi.WifiManager
import androidx.core.app.NotificationCompat
import com.bumptech.glide.request.transition.Transition
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import com.guichaguri.trackplayer.service.Utils
import java.util.ArrayList

/**
 * @author Guichaguri
 */
class MetadataManager(private val service: MusicService, private val manager: MusicManager) {
    val session: MediaSessionCompat
    var ratingType = RatingCompat.RATING_NONE
        private set
    var forwardJumpInterval = 15
        private set
    var backwardJumpInterval = 15
        private set
    private var actions: Long = 0
    private var compactActions: Long = 0
    private var artworkTarget: SimpleTarget<Bitmap>? = null
    private val builder: NotificationCompat.Builder
    private var previousAction: NotificationCompat.Action? = null
    private var rewindAction: NotificationCompat.Action? = null
    private var playAction: NotificationCompat.Action? = null
    private var pauseAction: NotificationCompat.Action? = null
    private var stopAction: NotificationCompat.Action? = null
    private var forwardAction: NotificationCompat.Action? = null
    private var nextAction: NotificationCompat.Action? = null

    /**
     * Updates the metadata options
     * @param options The options
     */
    fun updateOptions(options: Bundle?) {
        val capabilities: List<Int>? = options!!.getIntegerArrayList("capabilities")
        var notification: List<Int>? = options.getIntegerArrayList("notificationCapabilities")
        val compact: List<Int>? = options.getIntegerArrayList("compactCapabilities")
        actions = 0
        compactActions = 0
        if (capabilities != null) {
            // Create the actions mask
            for (cap in capabilities) actions = actions or cap.toLong()

            // If there is no notification capabilities defined, we'll show all capabilities available
            if (notification == null) notification = capabilities

            // Initialize all actions based on the options
            previousAction = createAction(
                notification, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, "Previous",
                getIcon(options, "previousIcon", R.drawable.previous)
            )
            rewindAction = createAction(
                notification, PlaybackStateCompat.ACTION_REWIND, "Rewind",
                getIcon(options, "rewindIcon", R.drawable.rewind)
            )
            playAction = createAction(
                notification, PlaybackStateCompat.ACTION_PLAY, "Play",
                getIcon(options, "playIcon", R.drawable.play)
            )
            pauseAction = createAction(
                notification, PlaybackStateCompat.ACTION_PAUSE, "Pause",
                getIcon(options, "pauseIcon", R.drawable.pause)
            )
            stopAction = createAction(
                notification, PlaybackStateCompat.ACTION_STOP, "Stop",
                getIcon(options, "stopIcon", R.drawable.stop)
            )
            forwardAction = createAction(
                notification, PlaybackStateCompat.ACTION_FAST_FORWARD, "Forward",
                getIcon(options, "forwardIcon", R.drawable.forward)
            )
            nextAction = createAction(
                notification, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, "Next",
                getIcon(options, "nextIcon", R.drawable.next)
            )

            // Update the action mask for the compact view
            if (compact != null) {
                for (cap in compact) compactActions = compactActions or cap.toLong()
            }
        }

        // Update the color
        builder.color = Utils.getInt(options, "color", NotificationCompat.COLOR_DEFAULT)

        // Update the icon
        builder.setSmallIcon(getIcon(options, "icon", R.drawable.play))

        // Update the jump interval
        forwardJumpInterval = Utils.getInt(options, "forwardJumpInterval", 15)
        backwardJumpInterval = Utils.getInt(options, "backwardJumpInterval", 15)

        // Update the rating type
        ratingType = Utils.getInt(options, "ratingType", RatingCompat.RATING_NONE)
        session.setRatingType(ratingType)
        updateNotification()
    }

    fun removeNotifications() {
        val ns = Context.NOTIFICATION_SERVICE
        val context = service.applicationContext
        val manager = context.getSystemService(ns) as NotificationManager
        manager.cancelAll()
    }

    /**
     * Updates the artwork
     * @param bitmap The new artwork
     */
    protected fun updateArtwork(bitmap: Bitmap?) {
        val track = manager.playback?.currentTrack ?: return
        val metadata = track.toMediaMetadata()
        metadata!!.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
        builder.setLargeIcon(bitmap)
        session.setMetadata(metadata.build())
        updateNotification()
    }

    /**
     * Updates the current track
     * @param track The new track
     */
    fun updateMetadata(playback: ExoPlayback<*>?, track: TrackMetadata) {
        val metadata = track.toMediaMetadata()
        val rm = Glide.with(service.applicationContext)
        if (artworkTarget != null) rm.clear(artworkTarget)
        if (track.artwork != null) {
            artworkTarget = rm.asBitmap()
                .load(track.artwork)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        metadata!!.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                        builder.setLargeIcon(resource)
                        session.setMetadata(metadata.build())
                        updateNotification()
                        artworkTarget = null
                    }
                })
        }
        builder.setContentTitle(track.title)
        builder.setContentText(track.artist)
        builder.setSubText(track.album)
        session.setMetadata(metadata!!.build())
        updatePlaybackState(playback)
        updateNotification()
    }

    /**
     * Updates the playback state and notification buttons
     * @param playback The player
     */
    @SuppressLint("RestrictedApi")
    fun updatePlayback(playback: ExoPlayback<*>?) {
        val state = playback?.state
        val playing = Utils.isPlaying(state!!)
        val compact: MutableList<Int> = ArrayList()
        builder.mActions.clear()

        // Adds the media buttons to the notification
        addAction(previousAction, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, compact)
        addAction(rewindAction, PlaybackStateCompat.ACTION_REWIND, compact)
        if (playing) {
            addAction(pauseAction, PlaybackStateCompat.ACTION_PAUSE, compact)
        } else {
            addAction(playAction, PlaybackStateCompat.ACTION_PLAY, compact)
        }
        addAction(stopAction, PlaybackStateCompat.ACTION_STOP, compact)
        addAction(forwardAction, PlaybackStateCompat.ACTION_FAST_FORWARD, compact)
        addAction(nextAction, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, compact)

        // Prevent the media style from being used in older Huawei devices that don't support custom styles
        if (!Build.MANUFACTURER.toLowerCase()
                .contains("huawei") || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        ) {
            val style = androidx.media.app.NotificationCompat.MediaStyle()
            if (playing) {
                style.setShowCancelButton(false)
            } else {
                // Shows the cancel button on pre-lollipop versions due to a bug
                style.setShowCancelButton(true)
                style.setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        service,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
            }

            // Links the media session
            style.setMediaSession(session.sessionToken)

            // Updates the compact media buttons for the notification
            if (!compact.isEmpty()) {
                val compactIndexes = IntArray(compact.size)
                for (i in compact.indices) compactIndexes[i] = compact[i]
                style.setShowActionsInCompactView(*compactIndexes)
            }
            builder.setStyle(style)
        }
        updatePlaybackState(playback)
        updateNotification()
    }

    /**
     * Updates the playback state
     * @param playback The player
     */
    private fun updatePlaybackState(playback: ExoPlayback<*>?) {
        // Updates the media session state
        val pb = PlaybackStateCompat.Builder()
        pb.setActions(actions)
        pb.setState(playback?.state!!, playback.position, playback.rate)
        pb.setBufferedPosition(playback.bufferedPosition)
        session.setPlaybackState(pb.build())
    }

    fun setActive(active: Boolean) {
        session.isActive = active
        updateNotification()
    }

    fun destroy() {
        service.stopForeground(true)
        session.isActive = false
        session.release()
    }

    private fun updateNotification() {
        if (session.isActive) {
            service.startForeground(1, builder.build())
        } else {
            service.stopForeground(true)
        }
    }

    private fun getIcon(options: Bundle?, propertyName: String, defaultIcon: Int): Int {
        if (!options!!.containsKey(propertyName)) return defaultIcon
        val bundle = options.getBundle(propertyName) ?: return defaultIcon
        val helper = ResourceDrawableIdHelper.getInstance()
        val icon = helper.getResourceDrawableId(service, bundle.getString("uri"))
        return if (icon == 0) defaultIcon else icon
    }

    private fun createAction(
        caps: List<Int>,
        action: Long,
        title: String,
        icon: Int
    ): NotificationCompat.Action? {
        return if (!caps.contains(action.toInt())) null else NotificationCompat.Action(
            icon, title, MediaButtonReceiver.buildMediaButtonPendingIntent(
                service, action
            )
        )
    }

    @SuppressLint("RestrictedApi")
    private fun addAction(action: NotificationCompat.Action?, id: Long, compact: MutableList<Int>) {
        if (action == null) return
        if (compactActions and id != 0L) compact.add(builder.mActions.size)
        builder.mActions.add(action)
    }

    init {
        val channel = Utils.getNotificationChannel(
            service as Context
        )
        builder = NotificationCompat.Builder(service, channel!!)
        session = MediaSessionCompat(service, "TrackPlayer", null, null)
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
        session.setCallback(ButtonEvents(service, manager))
        val context = service.applicationContext
        val packageName = context.packageName
        var openApp = context.packageManager.getLaunchIntentForPackage(packageName)
        if (openApp == null) {
            openApp = Intent()
            openApp.setPackage(packageName)
            openApp.addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Prevent the app from launching a new instance
        openApp.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // Add the Uri data so apps can identify that it was a notification click
        openApp.action = Intent.ACTION_VIEW
        openApp.data = Uri.parse("trackplayer://notification.click")
        builder.setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        )
        builder.setSmallIcon(R.drawable.play)
        builder.setCategory(NotificationCompat.CATEGORY_TRANSPORT)

        // Stops the playback when the notification is swiped away
        builder.setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service,
                PlaybackStateCompat.ACTION_STOP
            )
        )

        // Make it visible in the lockscreen
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }
}