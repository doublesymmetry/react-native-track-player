package guichaguri.trackplayer.metadata.components;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.MediaStyle;
import android.view.KeyEvent;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.PlayerService;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class MediaNotification {

    public static final int NOTIFICATION_ID = 6402;

    private final Context context;

    private NotificationCompat.Builder nb;
    private MediaStyle style;

    private int playIcon, pauseIcon, stopIcon, previousIcon, nextIcon;
    private Action play, pause, stop, previous, next;

    private boolean showing = false;

    public MediaNotification(Context context, MediaSessionCompat session) {
        this.context = context;

        this.nb = new NotificationCompat.Builder(context);
        this.style = new MediaStyle().setMediaSession(session.getSessionToken());

        nb.setStyle(style);
        nb.setCategory(NotificationCompat.CATEGORY_TRANSPORT);
        nb.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        nb.setDeleteIntent(createActionIntent(PlaybackStateCompat.ACTION_STOP));

        String packageName = context.getPackageName();
        Intent openApp = context.getPackageManager().getLaunchIntentForPackage(packageName);
        nb.setContentIntent(PendingIntent.getActivity(context, 0, openApp, 0));

        playIcon = loadIcon("play");
        pauseIcon = loadIcon("pause");
        stopIcon = loadIcon("stop");
        previousIcon = loadIcon("previous");
        nextIcon = loadIcon("next");

        nb.setSmallIcon(playIcon);
    }

    @SuppressWarnings("ResourceAsColor")
    public void updateOptions(ReadableMap data) {
        // Load the icons
        playIcon = loadIcon(data, "playIcon", "play");
        pauseIcon = loadIcon(data, "pauseIcon", "pause");
        stopIcon = loadIcon(data, "stopIcon", "stop");
        previousIcon = loadIcon(data, "previousIcon", "previous");
        nextIcon = loadIcon(data, "nextIcon", "next");

        // Load the color and the small icon
        int color = data.hasKey("color") ? data.getInt("color") : NotificationCompat.COLOR_DEFAULT;
        int icon = data.hasKey("icon") ? Utils.getLocalResourceId(context, data.getMap("icon")) : 0;

        // Update properties
        nb.setColor(color);
        nb.setLights(color, 250, 250);
        nb.setSmallIcon(icon != 0 ? icon : playIcon);

        // Update the notification
        update();
    }

    public void updateMetadata(MediaMetadataCompat metadata) {
        MediaDescriptionCompat description = metadata.getDescription();

        // Fill notification info
        nb.setContentTitle(description.getTitle());
        nb.setContentText(description.getSubtitle());
        nb.setSubText(description.getDescription());
        nb.setLargeIcon(description.getIconBitmap());

        // Update the notification
        update();
    }

    public void updatePlayback(PlaybackStateCompat playback) {
        // Check and update the state
        boolean playing = Utils.isPlaying(playback.getState());
        nb.setOngoing(playing);

        // For pre-lollipop devices, service notifications were ongoing even after foreground was disabled
        // To fix the issue, we'll add a cancel button for them
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(playing) {
                // Remove the cancel button when playing
                style.setShowCancelButton(false);
            } else {
                // Add the cancel button and set its action when not playing
                style.setCancelButtonIntent(createActionIntent(PlaybackStateCompat.ACTION_STOP));
                style.setShowCancelButton(true);
            }
        }

        // Check and update action buttons
        long actions = playback.getActions();
        play = updateAction(play, actions, PlaybackStateCompat.ACTION_PLAY, "Play", playIcon);
        pause = updateAction(pause, actions, PlaybackStateCompat.ACTION_PAUSE, "Pause", pauseIcon);
        stop = updateAction(stop, actions, PlaybackStateCompat.ACTION_STOP, "Stop", stopIcon);
        previous = updateAction(previous, actions, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, "Previous", previousIcon);
        next = updateAction(next, actions, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, "Next", nextIcon);

        // Add the action buttons
        nb.mActions.clear();
        if(previous != null) nb.mActions.add(previous);
        if(play != null && !playing) nb.mActions.add(play);
        if(pause != null && playing) nb.mActions.add(pause);
        if(stop != null) nb.mActions.add(stop);
        if(next != null) nb.mActions.add(next);

        // Add the play/pause button to the compact view
        if(play != null && pause != null) {
            style.setShowActionsInCompactView(nb.mActions.indexOf(playing ? pause : play));
        } else {
            style.setShowActionsInCompactView();
        }

        // Update the notification
        nb.setStyle(style);
        update();
    }

    private int loadIcon(ReadableMap data, String key, String iconName) {
        if(data.hasKey(key)) {
            // Load icon from option value
            int id = Utils.getLocalResourceId(context, data.getMap(key));
            if(id != 0) return id;
        }

        // Load default icon
        return loadIcon(iconName);
    }

    private int loadIcon(String iconName) {
        // Load icon resource from name
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    private Action updateAction(Action instance, long mask, long action, String title, int icon) {
        // The action is disabled, we'll not create it
        if((mask & action) == 0) return null;

        // The action is already created
        if(instance != null) return instance;

        // Create the action
        return new Action(icon, title, createActionIntent(action));
    }

    /**
     * We should take a look at MediaButtonReceiver.buildMediaButtonPendingIntent
     * when React Native updates to a newer support library version
     */
    private PendingIntent createActionIntent(long action) {
        // Create an intent for the service
        int keyCode = Utils.toKeyCode(action);
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);

        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, event);

        return PendingIntent.getService(context, keyCode, intent, 0);
    }

    private void update() {
        // Update the notification if it's showing
        if(showing) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, nb.build());
        }
    }

    public Notification build() {
        return nb.build();
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
    }

}
