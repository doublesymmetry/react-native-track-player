package guichaguri.trackplayer.metadata.components;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import guichaguri.trackplayer.R;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.services.PlayerService;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class MediaNotification {

    public static final int NOTIFICATION_ID = 6402;

    private final Context context;

    private NotificationCompat.Builder nb;
    private MediaStyle style;

    private int color = NotificationCompat.COLOR_DEFAULT;
    private int smallIcon, playIcon, pauseIcon, stopIcon, previousIcon, nextIcon, rewindIcon, forwardIcon;
    private Action play, pause, stop, previous, next, rewind, forward;

    private int notificationCapabilities = -1;
    private int compactCapabilities = 0;

    private boolean showing = false;

    public MediaNotification(Context context, MediaSessionCompat session) {
        this.context = context;

        if(VERSION.SDK_INT >= VERSION_CODES.O) createChannel();

        this.nb = new Builder(context, "trackplayer");
        this.style = new MediaStyle().setMediaSession(session.getSessionToken());

        nb.setStyle(style);
        nb.setCategory(NotificationCompat.CATEGORY_TRANSPORT);
        nb.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        nb.setDeleteIntent(createActionIntent(PlaybackStateCompat.ACTION_STOP));

        String packageName = context.getPackageName();
        Intent openApp = context.getPackageManager().getLaunchIntentForPackage(packageName);
        nb.setContentIntent(PendingIntent.getActivity(context, 0, openApp, 0));

        smallIcon = R.drawable.play;
        playIcon = R.drawable.play;
        pauseIcon = R.drawable.pause;
        stopIcon = R.drawable.stop;
        previousIcon = R.drawable.previous;
        nextIcon = R.drawable.next;
        rewindIcon = R.drawable.rewind;
        forwardIcon = R.drawable.forward;

        nb.setSmallIcon(playIcon);
    }

    @RequiresApi(VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel("trackplayer", "Media controls", NotificationManager.IMPORTANCE_LOW);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        channel.setDescription("Media player controls");
        channel.setShowBadge(true);
        mNotificationManager.createNotificationChannel(channel);
    }

    @SuppressWarnings("ResourceAsColor")
    public void updateOptions(Bundle data) {
        // Load the icons
        playIcon = loadIcon(data, "playIcon", playIcon);
        pauseIcon = loadIcon(data, "pauseIcon", pauseIcon);
        stopIcon = loadIcon(data, "stopIcon", stopIcon);
        previousIcon = loadIcon(data, "previousIcon", previousIcon);
        nextIcon = loadIcon(data, "nextIcon", nextIcon);
        rewindIcon = loadIcon(data, "rewindIcon", rewindIcon);
        forwardIcon = loadIcon(data, "forwardIcon", forwardIcon);

        // Load the color and the small icon
        color = (int)data.getDouble("color", color);
        smallIcon = data.containsKey("icon") ? Utils.getResourceId(context, data.getBundle("icon")) : smallIcon;

        // Update properties
        nb.setColor(color);
        nb.setLights(color, 250, 250);
        nb.setSmallIcon(smallIcon != 0 ? smallIcon : playIcon);

        // Update notification capabilities
        List<Integer> notification = data.getIntegerArrayList("notificationCapabilities");

        if(notification != null) {
            notificationCapabilities = 0;
            for(int cap : notification) {
                notificationCapabilities |= cap;
            }
        } else {
            notificationCapabilities = -1;
        }

        // Update compact capabilities
        List<Integer> compact = data.getIntegerArrayList("compactCapabilities");

        if(compact != null) {
            compactCapabilities = 0;
            for(int cap : compact) {
                compactCapabilities |= cap;
            }
        }

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

        ArrayList<Action> actions = new ArrayList<>();
        ArrayList<Action> compact = new ArrayList<>();

        // Check and update action buttons
        long mask = playback.getActions();
        rewind = addAction(rewind, mask, PlaybackStateCompat.ACTION_REWIND, "Rewind", rewindIcon, actions, compact, playing);
        previous = addAction(previous, mask, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, "Previous", previousIcon, actions, compact, playing);
        play = addAction(play, mask, PlaybackStateCompat.ACTION_PLAY, "Play", playIcon, actions, compact, playing);
        pause = addAction(pause, mask, PlaybackStateCompat.ACTION_PAUSE, "Pause", pauseIcon, actions, compact, playing);
        stop = addAction(stop, mask, PlaybackStateCompat.ACTION_STOP, "Stop", stopIcon, actions, compact, playing);
        next = addAction(next, mask, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, "Next", nextIcon, actions, compact, playing);
        forward = addAction(forward, mask, PlaybackStateCompat.ACTION_FAST_FORWARD, "Forward", forwardIcon, actions, compact, playing);

        // Create the compact indexes array
        int[] compactIndexes = new int[compact.size()];
        for(int i = 0; i < compact.size(); i++) {
            compactIndexes[i] = actions.indexOf(compact.get(i));
        }

        // Update the action buttons list and the compact indexes
        nb.mActions = actions;
        style.setShowActionsInCompactView(compactIndexes);

        // Update the notification
        nb.setStyle(style);
        update();
    }

    private int loadIcon(Bundle data, String key, int icon) {
        if(data.containsKey(key)) {
            // Load icon from option value
            int id = Utils.getResourceId(context, data.getBundle(key));
            if(id != 0) return id;
        }
        return icon;
    }

    private int loadIcon(String iconName) {
        // Load icon resource from name
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    private Action addAction(Action instance, long mask, long action, String title, int icon,
                             List<Action> list, List<Action> compactView, boolean playing) {
        // Update the action
        instance = updateAction(instance, mask, action, title, icon);

        // Check if it's disabled
        if(instance == null) return null;

        if(action == PlaybackStateCompat.ACTION_PLAY && playing) return instance;
        if(action == PlaybackStateCompat.ACTION_PAUSE && !playing) return instance;

        // Add it to the compact view if it's allowed to
        if((compactCapabilities & action) != 0) {
            compactView.add(instance);
        }

        // Add to the action list
        list.add(instance);

        return instance;
    }

    private Action updateAction(Action instance, long mask, long action, String title, int icon) {
        // The action is disabled, we'll not create it
        if((mask & action) == 0) return null;
        if(notificationCapabilities != -1 && (notificationCapabilities & action) == 0) return null;

        // The action is already created
        if(instance != null) return instance;

        // Create the action
        return new Action(icon, title, createActionIntent(action));
    }

    /**
     * TODO
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
        if(!showing) return;

        // Updates the notification it
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, nb.build());
        } catch(Exception ex) {
            Log.w(Utils.TAG, "Something went wrong while updating the notification", ex);
        }
    }

    public Notification build() {
        return nb.build();
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
    }

}
