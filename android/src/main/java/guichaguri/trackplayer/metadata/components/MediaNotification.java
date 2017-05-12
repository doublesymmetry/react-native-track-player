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
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.workers.PlayerService;
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

    private int playIcon, pauseIcon, stopIcon, previousIcon, nextIcon;
    private Action play, pause, stop, previous, next;

    private int compactCapabilities = 0;

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

        // Update compact capabilities
        compactCapabilities = 0;

        ReadableArray array = Utils.getArray(data, "compactCapabilities", null);
        if(array == null) return;

        for(int i = 0; i < array.size(); i++) {
            if(array.getType(i) == ReadableType.Number) {
                compactCapabilities |= array.getInt(i);
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

        List<Action> actions = new ArrayList<>();
        List<Action> compact = new ArrayList<>();

        // Check and update action buttons
        long mask = playback.getActions();
        previous = addAction(previous, mask, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS, "Previous", previousIcon, actions, compact, playing);
        play = addAction(play, mask, PlaybackStateCompat.ACTION_PLAY, "Play", playIcon, actions, compact, playing);
        pause = addAction(pause, mask, PlaybackStateCompat.ACTION_PAUSE, "Pause", pauseIcon, actions, compact, playing);
        stop = addAction(stop, mask, PlaybackStateCompat.ACTION_STOP, "Stop", stopIcon, actions, compact, playing);
        next = addAction(next, mask, PlaybackStateCompat.ACTION_SKIP_TO_NEXT, "Next", nextIcon, actions, compact, playing);

        // Add the action buttons
        nb.mActions.clear();
        nb.mActions.addAll(actions);

        // Add the compact actions
        int[] compactIndexes = new int[compact.size()];
        for(int i = 0; i < compact.size(); i++) {
            compactIndexes[i] = actions.indexOf(compact.get(i));
        }
        style.setShowActionsInCompactView(compactIndexes);

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

    private Action addAction(Action instance, long mask, long action, String title, int icon,
                             List<Action> list, List<Action> compactView, boolean playing) {
        // Update the action
        instance = updateAction(instance, mask, action, title, icon);

        // Check if it's disabled
        if(instance == null) return null;

        if(action == PlaybackStateCompat.ACTION_PLAY && playing) return instance;
        if(action == PlaybackStateCompat.ACTION_PAUSE && !playing) return instance;

        // Add it to the compact view if it's allowed to
        if((compactCapabilities & action) == 0) {
            compactView.add(instance);
        }

        // Add to the action list
        list.add(instance);

        return instance;
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
