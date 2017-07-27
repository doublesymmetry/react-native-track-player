package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.cast.framework.CastState;
import guichaguri.trackplayer.logic.LibHelper;
import guichaguri.trackplayer.logic.Temp;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import guichaguri.trackplayer.logic.services.PlayerService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Guilherme Chaguri
 */
public class TrackModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private MediaWrapper binder;
    private boolean connecting = false;
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

        // Unbinds the service
        if(binder != null || connecting) {
            getReactApplicationContext().unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MediaWrapper)service;
        connecting = false;

        if(initCallbacks != null) {
            // Triggers all callbacks from onReady
            for(Callback cb : initCallbacks) {
                Utils.triggerCallback(cb);
            }
            initCallbacks = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
        connecting = false;
    }

    /* ****************************** API ****************************** */

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();

        // Capabilities
        constants.put("CAPABILITY_PLAY", PlaybackStateCompat.ACTION_PLAY);
        constants.put("CAPABILITY_PLAY_FROM_ID", PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
        constants.put("CAPABILITY_PLAY_FROM_SEARCH", PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH);
        constants.put("CAPABILITY_PAUSE", PlaybackStateCompat.ACTION_PAUSE);
        constants.put("CAPABILITY_STOP", PlaybackStateCompat.ACTION_STOP);
        constants.put("CAPABILITY_SEEK_TO", PlaybackStateCompat.ACTION_SEEK_TO);
        constants.put("CAPABILITY_SKIP", PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM);
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

        // Cast States
        constants.put("CAST_NO_DEVICES_AVAILABLE", CastState.NO_DEVICES_AVAILABLE);
        constants.put("CAST_NOT_CONNECTED", CastState.NOT_CONNECTED);
        constants.put("CAST_CONNECTING", CastState.CONNECTING);
        constants.put("CAST_CONNECTED", CastState.CONNECTED);

        // Not actual an API constant
        // Only used internally
        constants.put("CAST_SUPPORT_AVAILABLE", LibHelper.isChromecastAvailable(getReactApplicationContext()));

        return constants;
    }

    @ReactMethod
    public void onReady(Callback callback) {
        if(binder != null) {
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

        if(connecting) return;

        ReactApplicationContext context = getReactApplicationContext();

        // Binds the service to get a MediaWrapper instance
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_MEDIA_WRAPPER);
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);

        connecting = true;
    }

    @ReactMethod
    public void setupPlayer(ReadableMap data, Promise promise) {
        binder.setupPlayer(Temp.toBundle(data), promise); //TODO check if callback works properly
    }

    @ReactMethod
    public void destroy() {
        binder.destroy();
        //getReactApplicationContext().unbindService(this);
    }

    @ReactMethod
    public void updateOptions(ReadableMap data) {
        binder.updateOptions(Temp.toBundle(data));
    }

    @ReactMethod
    public void add(ReadableArray tracks, String insertBeforeId, Promise callback) {
        binder.add(Temp.toList(tracks), insertBeforeId, callback);//TODO check if callback works properly
    }

    @ReactMethod
    public void remove(ReadableArray tracks, Promise callback) {
        binder.remove(Temp.toList(tracks), callback);//TODO check if callback works properly
    }

    @ReactMethod
    public void skip(String track, Promise callback) {
        binder.skip(track, callback);//TODO check if callback works properly
    }

    @ReactMethod
    public void skipToNext(Promise callback) {
        binder.skipToNext(callback);//TODO check if callback works properly
    }

    @ReactMethod
    public void skipToPrevious(Promise callback) {
        binder.skipToPrevious(callback);//TODO check if callback works properly
    }

    @ReactMethod
    public void reset() {
        binder.reset();
    }

    @ReactMethod
    public void play() {
        binder.play();
    }

    @ReactMethod
    public void pause() {
        binder.pause();
    }

    @ReactMethod
    public void stop() {
        binder.stop();
    }

    @ReactMethod
    public void seekTo(double seconds) {
        binder.seekTo(Utils.toMillis(seconds));
    }

    @ReactMethod
    public void setVolume(float volume) {
        binder.setVolume(volume);
    }

    @ReactMethod
    public void getVolume(Callback callback) {
        binder.getVolume(callback);
    }

    @ReactMethod
    public void getTrack(String id, Callback callback) {
        binder.getTrack(id, callback);
    }

    @ReactMethod
    public void getCurrentTrack(Callback callback) {
        binder.getCurrentTrack(callback);
    }

    @ReactMethod
    public void getDuration(Callback callback) {
        binder.getDuration(callback);
    }

    @ReactMethod
    public void getBufferedPosition(Callback callback) {
        binder.getBufferedPosition(callback);
    }

    @ReactMethod
    public void getPosition(Callback callback) {
        binder.getPosition(callback);
    }

    @ReactMethod
    public void getState(Callback callback) {
        binder.getState(callback);
    }

    @ReactMethod
    public void getCastState(Callback callback) {
        Context context = getReactApplicationContext().getApplicationContext();

        if(!LibHelper.isChromecastAvailable(context)) {
            Utils.triggerCallback(callback, CastState.NO_DEVICES_AVAILABLE);
            return;
        }

        binder.getCastState(callback);

        // Use the code below when React Native updates the support library
        // and we'll be able to update the Cast SDK too
        /*CastContext cast = CastContext.getSharedInstance(context);
        Utils.triggerCallback(callback, cast.getCastState());*/
    }
}
