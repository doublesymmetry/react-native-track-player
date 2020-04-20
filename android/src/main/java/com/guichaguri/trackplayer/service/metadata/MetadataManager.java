package com.guichaguri.trackplayer.service.metadata;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import com.guichaguri.trackplayer.R;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.models.TrackMetadata;
import com.guichaguri.trackplayer.service.player.ExoPlayback;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guichaguri
 */
public class MetadataManager {

    private final Context context;
    private final MusicManager manager;
    private final MediaSessionCompat session;
    private final NotificationCompat.Builder builder;
    private final ButtonReceiver receiver;

    private int ratingType = RatingCompat.RATING_NONE;
    private int jumpInterval = 15;
    private long actions = 0;
    private long compactActions = 0;
    private SimpleTarget<Bitmap> artworkTarget;
    private boolean receiverRegistered = false;

    private Action previousAction, rewindAction, playAction, pauseAction, stopAction, forwardAction, nextAction;

    public MetadataManager(Context context, MusicManager manager) {
        this.context = context;
        this.manager = manager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Utils.NOTIFICATION_CHANNEL,
                    "TrackPlayer",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setShowBadge(false);
            channel.setSound(null, null);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        this.builder = new NotificationCompat.Builder(context, Utils.NOTIFICATION_CHANNEL);
        this.session = new MediaSessionCompat(context, "TrackPlayer", null, null);
        this.receiver = new ButtonReceiver(this);

        session.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        session.setCallback(new ButtonEvents(manager));

        Context appContext = context.getApplicationContext();
        String packageName = appContext.getPackageName();
        Intent openApp = appContext.getPackageManager().getLaunchIntentForPackage(packageName);

        if (openApp == null) {
            openApp = new Intent();
            openApp.setPackage(packageName);
            openApp.addCategory(Intent.CATEGORY_LAUNCHER);
        }

        // Prevent the app from launching a new instance
        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Add the Uri data so apps can identify that it was a notification click
        openApp.setAction(Intent.ACTION_VIEW);
        openApp.setData(Uri.parse("trackplayer://notification.click"));

        builder.setContentIntent(PendingIntent.getActivity(appContext, 0, openApp, PendingIntent.FLAG_CANCEL_CURRENT));

        builder.setSmallIcon(R.drawable.play);
        builder.setCategory(NotificationCompat.CATEGORY_TRANSPORT);

        // Stops the playback when the notification is swiped away
        builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP));

        // Make it visible in the lockscreen
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public MediaSessionCompat getSession() {
        return session;
    }

    /**
     * Updates the metadata options
     * @param options The options
     */
    public void updateOptions(ReadableMap options) {
        List<Integer> capabilities = Utils.getIntegerList(options, "capabilities", null);
        List<Integer> notification = Utils.getIntegerList(options, "notificationCapabilities", capabilities);
        List<Integer> compact = Utils.getIntegerList(options, "compactCapabilities", null);

        actions = 0;
        compactActions = 0;

        if(capabilities != null) {
            // Create the actions mask
            for(int cap : capabilities) actions |= cap;

            // Initialize all actions based on the options

            previousAction = createAction(notification, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, "Previous",
                    getIcon(options, "previousIcon", R.drawable.previous));
            rewindAction = createAction(notification, PlaybackStateCompat.ACTION_REWIND, "Rewind",
                    getIcon(options, "rewindIcon", R.drawable.rewind));
            playAction = createAction(notification, PlaybackStateCompat.ACTION_PLAY, "Play",
                    getIcon(options, "playIcon", R.drawable.play));
            pauseAction = createAction(notification, PlaybackStateCompat.ACTION_PAUSE, "Pause",
                    getIcon(options, "pauseIcon", R.drawable.pause));
            stopAction = createAction(notification, PlaybackStateCompat.ACTION_STOP, "Stop",
                    getIcon(options, "stopIcon", R.drawable.stop));
            forwardAction = createAction(notification, PlaybackStateCompat.ACTION_FAST_FORWARD, "Forward",
                    getIcon(options, "forwardIcon", R.drawable.forward));
            nextAction = createAction(notification, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, "Next",
                    getIcon(options, "nextIcon", R.drawable.next));

            // Update the action mask for the compact view
            if(compact != null) {
                for(int cap : compact) compactActions |= cap;
            }
        }

        // Update the color
        builder.setColor(Utils.getInt(options, "color", NotificationCompat.COLOR_DEFAULT));

        // Update the icon
        builder.setSmallIcon(getIcon(options, "icon", R.drawable.play));

        // Update the jump interval
        jumpInterval = Utils.getInt(options, "jumpInterval", 15);

        // Update the rating type
        ratingType = Utils.getInt(options, "ratingType", RatingCompat.RATING_NONE);
        session.setRatingType(ratingType);

        updateNotification();
    }

    public int getRatingType() {
        return ratingType;
    }

    public int getJumpInterval() {
        return jumpInterval;
    }

    public void removeNotifications() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager manager = (NotificationManager) context.getApplicationContext().getSystemService(ns);
        manager.cancelAll();
    }

    /**
     * Updates the artwork
     * @param bitmap The new artwork
     */
    protected void updateArtwork(Bitmap bitmap) {
        Track track = manager.getPlayback().getCurrentTrack();
        if(track == null) return;

        MediaMetadataCompat.Builder metadata = track.toMediaMetadata();

        metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
        builder.setLargeIcon(bitmap);

        session.setMetadata(metadata.build());
        updateNotification();
    }

    /**
     * Updates the current track
     * @param track The new track
     */
    public void updateMetadata(TrackMetadata track) {
        MediaMetadataCompat.Builder metadata = track.toMediaMetadata();

        RequestManager rm = Glide.with(context.getApplicationContext());
        if(artworkTarget != null) rm.clear(artworkTarget);

        if(track.artwork != null) {
            artworkTarget = rm.asBitmap()
                    .load(track.artwork)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource);
                            builder.setLargeIcon(resource);

                            session.setMetadata(metadata.build());
                            updateNotification();
                            artworkTarget = null;
                        }
                    });
        }

        builder.setContentTitle(track.title);
        builder.setContentText(track.artist);
        builder.setSubText(track.album);

        session.setMetadata(metadata.build());
        updateNotification();
    }

    /**
     * Updates the playback state
     * @param playback The player
     */
    public void updatePlayback(ExoPlayback playback) {
        int state = playback.getState();
        boolean playing = Utils.isPlaying(state);
        List<Integer> compact = new ArrayList<>();

        builder.setOngoing(playing);
        builder.mActions.clear();

        // Adds the media buttons to the notification

        addAction(previousAction, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, compact);
        addAction(rewindAction, PlaybackStateCompat.ACTION_REWIND, compact);

        if(playing) {
            addAction(pauseAction, PlaybackStateCompat.ACTION_PAUSE, compact);
        } else {
            addAction(playAction, PlaybackStateCompat.ACTION_PLAY, compact);
        }

        addAction(stopAction, PlaybackStateCompat.ACTION_STOP, compact);
        addAction(forwardAction, PlaybackStateCompat.ACTION_FAST_FORWARD, compact);
        addAction(nextAction, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, compact);

        // Prevent the media style from being used in older Huawei devices that don't support custom styles
        if(!Build.MANUFACTURER.toLowerCase().contains("huawei") || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            MediaStyle style = new MediaStyle();

            if(playing) {
                style.setShowCancelButton(false);
            } else {
                // Shows the cancel button on pre-lollipop versions due to a bug
                style.setShowCancelButton(true);
                style.setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP));
            }

            // Links the media session
            style.setMediaSession(session.getSessionToken());

            // Updates the compact media buttons for the notification
            if (!compact.isEmpty()) {
                int[] compactIndexes = new int[compact.size()];

                for (int i = 0; i < compact.size(); i++) compactIndexes[i] = compact.get(i);

                style.setShowActionsInCompactView(compactIndexes);
            }

            builder.setStyle(style);

        }

        // Updates the media session state
        PlaybackStateCompat.Builder pb = new PlaybackStateCompat.Builder();
        pb.setActions(actions);
        pb.setState(state, playback.getPosition(), playback.getRate());
        pb.setBufferedPosition(playback.getBufferedPosition());

        session.setPlaybackState(pb.build());
        updateNotification();
    }

    public void setActive(boolean active) {
        this.session.setActive(active);

        if (active) {
            if (!receiverRegistered) {
                context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
                receiverRegistered = true;
            }
        } else {
            if (receiverRegistered) {
                context.unregisterReceiver(receiver);
                receiverRegistered = false;
            }
        }

        updateNotification();
    }

    public void destroy() {
        NotificationManagerCompat.from(context).cancel(Utils.NOTIFICATION_ID);

        session.setActive(false);
        session.release();
    }

    private void updateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if(session.isActive()) {
            notificationManager.notify(Utils.NOTIFICATION_ID, builder.build());
        } else {
            notificationManager.cancel(Utils.NOTIFICATION_ID);
        }
    }

    private int getIcon(ReadableMap options, String key, int defaultIcon) {
        if(!options.hasKey(key) || options.getType(key) != ReadableType.Map) return defaultIcon;

        ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
        int icon = helper.getResourceDrawableId(context, options.getMap(key).getString("uri"));
        if(icon == 0) return defaultIcon;

        return icon;
    }

    private Action createAction(List<Integer> caps, long action, String title, int icon) {
        if(!caps.contains((int)action)) return null;

        return new Action(icon, title, MediaButtonReceiver.buildMediaButtonPendingIntent(context, action));
    }

    private void addAction(Action action, long id, List<Integer> compact) {
        if(action == null) return;

        if((compactActions & id) != 0) compact.add(builder.mActions.size());
        builder.mActions.add(action);
    }

}
