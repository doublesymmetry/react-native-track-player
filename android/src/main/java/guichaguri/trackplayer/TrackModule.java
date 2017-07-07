package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.TransportControls;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.facebook.react.bridge.*;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import guichaguri.trackplayer.logic.Constants;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import guichaguri.trackplayer.logic.workers.PlayerService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Guilherme Chaguri
 */
public class TrackModule extends ReactContextBaseJavaModule {

    private MediaBrowserCompat browser;
    private MediaControllerCompat controller;
    private TransportControls controls;

    private Callback[] initCallbacks = null;

    public TrackModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "TrackPlayerModule";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();

        // Disconnects the media browser
        if(browser != null) {
            browser.disconnect();
            browser = null;
        }
    }

    /* ****************************** API ****************************** */

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();

        // Capabilities
        constants.put("CAPABILITY_PLAY", PlaybackStateCompat.ACTION_PLAY);
        constants.put("CAPABILITY_PAUSE", PlaybackStateCompat.ACTION_PAUSE);
        constants.put("CAPABILITY_STOP", PlaybackStateCompat.ACTION_STOP);
        constants.put("CAPABILITY_SEEK_TO", PlaybackStateCompat.ACTION_SEEK_TO);
        constants.put("CAPABILITY_SKIP_TO_NEXT", PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        constants.put("CAPABILITY_SKIP_TO_PREVIOUS", PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        constants.put("CAPABILITY_SET_RATING", PlaybackStateCompat.ACTION_SET_RATING);

        // States
        constants.put("STATE_NONE", PlaybackStateCompat.STATE_NONE);
        constants.put("STATE_PLAYING", PlaybackStateCompat.STATE_PLAYING);
        constants.put("STATE_PAUSED", PlaybackStateCompat.STATE_PAUSED);
        constants.put("STATE_STOPPED", PlaybackStateCompat.STATE_STOPPED);
        constants.put("STATE_BUFFERING", PlaybackStateCompat.STATE_BUFFERING);

        // Rating Types
        constants.put("RATING_HEART", RatingCompat.RATING_HEART);
        constants.put("RATING_THUMBS_UP_DOWN", RatingCompat.RATING_THUMB_UP_DOWN);
        constants.put("RATING_3_STARS", RatingCompat.RATING_3_STARS);
        constants.put("RATING_4_STARS", RatingCompat.RATING_4_STARS);
        constants.put("RATING_5_STARS", RatingCompat.RATING_5_STARS);
        constants.put("RATING_PERCENTAGE", RatingCompat.RATING_PERCENTAGE);

        return constants;
    }

    @ReactMethod
    public void onReady(Callback callback) {
        if(browser != null && browser.isConnected()) {
            // The module is already connected to the service
            Utils.triggerCallback(callback);
            return;
        }

        if(initCallbacks == null) {
            // Create a new array with our callback
            initCallbacks = new Callback[]{callback};
        } else {
            // Add to the existing array
            int index = initCallbacks.length;
            initCallbacks = Arrays.copyOf(initCallbacks, index + 1);
            initCallbacks[index] = callback;
        }

        // The browser is already connecting
        if(browser != null) return;

        ReactApplicationContext context = getReactApplicationContext();

        // Creates the media browser just to get the token
        ComponentName comp = new ComponentName(context, PlayerService.class);
        browser = new MediaBrowserCompat(context, comp, new MediaConnectionHandler(), null);
        browser.connect();
    }

    @ReactMethod
    public void setupPlayer(ReadableMap data, Promise promise) {
        if(Utils.isPlaying(controller.getPlaybackState().getState())) {
            // The player is already initialized
            Utils.rejectCallback(promise, "setup", "Couldn't setup the player, it is already initialized!");
            return;
        }
        //TODO
    }

    @ReactMethod
    public void destroy() {
        browser.disconnect();
    }

    @ReactMethod
    public void setOptions(ReadableMap data) {
        //manager.setOptions(data);//TODO
    }

    @ReactMethod
    public void add(String insertBeforeId, ReadableArray tracks, Promise callback) {
        Bundle bundle = new Bundle();
        bundle.putString("insertBeforeId", insertBeforeId);

        Parcelable[] array = new Parcelable[tracks.size()];
        for(int i = 0; i < tracks.size(); i++) {
            array[i] = Arguments.toBundle(tracks.getMap(i));
        }
        bundle.putParcelableArray("tracks", array);

        controls.sendCustomAction(Constants.ADD, bundle);//TODO
    }

    @ReactMethod
    public void remove(ReadableArray tracks, Promise callback) {
        Bundle bundle = new Bundle();

        String[] array = new String[tracks.size()];
        for(int i = 0; i < tracks.size(); i++) {
            array[i] = tracks.getString(i);
        }
        bundle.putStringArray("tracks", array);

        controls.sendCustomAction(Constants.REMOVE, bundle);//TODO
    }

    @ReactMethod
    public void skip(String track, Promise callback) {
        //TODO check if it will work for backwards queue
        for(QueueItem item : controller.getQueue()) {
            if(track.equals(item.getDescription().getMediaId())) {
                controls.skipToQueueItem(item.getQueueId());
                Utils.resolveCallback(callback);
                return;
            }
        }
        Utils.rejectCallback(callback, "skip", "The track was not found");
    }

    @ReactMethod
    public void skipToNext(Promise callback) {
        controls.skipToNext();//TODO keep callback?
    }

    @ReactMethod
    public void skipToPrevious(Promise callback) {
        controls.skipToPrevious();//TODO keep callback?
    }

    @ReactMethod
    public void load(ReadableMap data, Promise callback) {
        controls.playFromMediaId();//TODO
    }

    @ReactMethod
    public void reset() {
        controls.sendCustomAction(Constants.RESET, null);
    }

    @ReactMethod
    public void play() {
        controls.play();
    }

    @ReactMethod
    public void pause() {
        controls.pause();
    }

    @ReactMethod
    public void stop() {
        controls.stop();
    }

    @ReactMethod
    public void seekTo(double seconds) {
        controls.seekTo(Utils.toMillis(seconds));
    }

    @ReactMethod
    public void setVolume(float volume) {
        int vol = (int)(volume * controller.getPlaybackInfo().getMaxVolume());
        controller.setVolumeTo(vol, 0);
    }

    /*@ReactMethod
    public void startScan(boolean active, Promise callback) {
        //controls.sendCustomAction();//TODO
        //manager.startScan(active, callback);
    }

    @ReactMethod
    public void stopScan() {
        //controls.sendCustomAction();//TODO
        //manager.stopScan();
    }

    @ReactMethod
    public void connect(String deviceId, Promise callback) {
        //manager.connect(deviceId, callback);
    }*/

    @ReactMethod
    public void getCurrentTrack(Callback callback) {
        long id = controller.getPlaybackState().getActiveQueueItemId();
        for(QueueItem item : controller.getQueue()) {
            if(item.getQueueId() == id) {
                Utils.triggerCallback(callback, item.getDescription().getMediaId());
                return;
            }
        }

        // Nothing is playing?
        Utils.triggerCallback(callback);
    }

    @ReactMethod
    public void getDuration(Callback callback) {
        long duration = controller.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        Utils.triggerCallback(callback, Utils.toSeconds(duration));
    }

    @ReactMethod
    public void getBufferedPosition(Callback callback) {
        Utils.triggerCallback(callback, Utils.toSeconds(controller.getPlaybackState().getBufferedPosition()));
    }

    @ReactMethod
    public void getPosition(Callback callback) {
        Utils.triggerCallback(callback, Utils.toSeconds(controller.getPlaybackState().getPosition()));
    }

    @ReactMethod
    public void getState(Callback callback) {
        Utils.triggerCallback(callback, controller.getPlaybackState().getState());
    }

    /*@ReactMethod
    public void isRemote(Callback callback) {
        CastContext cast = CastContext.getSharedInstance(getReactApplicationContext());
        CastSession session = cast.getSessionManager().getCurrentCastSession();

        Utils.triggerCallback(callback, session != null && session.isConnected());//TODO
    }*/


    private class MediaConnectionHandler extends ConnectionCallback {
        @Override
        public void onConnected() {
            try {
                controller = new MediaControllerCompat(getReactApplicationContext(), browser.getSessionToken());
                controls = controller.getTransportControls();
            } catch(RemoteException ex) {
                Log.e("ReactNativeTrackPlayer", "An error occurred while creating the media controller.", ex);
            }

            if(initCallbacks != null) {
                // Triggers all callbacks from onReady
                for(Callback cb : initCallbacks) {
                    Utils.triggerCallback(cb);
                }
                initCallbacks = null;
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("ReactNativeTrackPlayer", "The connection to the media browser was suspended");
            browser = null;
            controller = null;
            controls = null;
        }

        @Override
        public void onConnectionFailed() {
            Log.e("ReactNativeTrackPlayer", "Couldn't connect to the media browser. Something went wrong");
            browser = null;
            controller = null;
            controls = null;
        }
    }
}
