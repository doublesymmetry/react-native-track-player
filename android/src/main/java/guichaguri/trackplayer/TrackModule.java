package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
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
public class TrackModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private MediaWrapper manager = null;
    private Callback[] initCallbacks = null;

    public TrackModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "TrackPlayerModule";
    }

    @Override
    public void initialize() {
        super.initialize();

        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_MEDIA);
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        getReactApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        manager = (MediaWrapper)service;

        if(initCallbacks != null) {
            for(Callback cb : initCallbacks) {
                Utils.triggerCallback(cb);
            }
            initCallbacks = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        manager = null;
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
        if(manager != null) {
            Utils.triggerCallback(callback);
            return;
        }

        if(initCallbacks == null) {
            initCallbacks = new Callback[]{callback};
        } else {
            int index = initCallbacks.length;
            initCallbacks = Arrays.copyOf(initCallbacks, index + 1);
            initCallbacks[index] = callback;
        }
    }

    @ReactMethod
    public void setOptions(ReadableMap data) {
        manager.setOptions(data);
    }

    @ReactMethod
    public void createPlayer(Promise callback) {
        Utils.resolveCallback(callback, manager.createPlayer());
    }

    @ReactMethod
    public void setMain(int id) {
        manager.setMain(id);
    }

    @ReactMethod
    public void destroy(int id) {
        manager.destroy(id);
    }

    @ReactMethod
    public void add(int id, String insertBeforeId, ReadableArray data, Promise callback) {
        manager.add(id, insertBeforeId, data, callback);
    }

    @ReactMethod
    public void remove(int id, String[] tracks, Promise callback) {
        manager.remove(id, tracks, callback);
    }

    @ReactMethod
    public void skip(int id, String track, Promise callback) {
        manager.skip(id, track, callback);
    }

    @ReactMethod
    public void skipToNext(int id, Promise callback) {
        manager.skipToNext(id, callback);
    }

    @ReactMethod
    public void skipToPrevious(int id, Promise callback) {
        manager.skipToPrevious(id, callback);
    }

    @ReactMethod
    public void load(int id, ReadableMap data, Promise callback) {
        manager.load(id, data, callback);
    }

    @ReactMethod
    public void reset(int id) {
        manager.reset(id);
    }

    @ReactMethod
    public void play(int id) {
        manager.play(id);
    }

    @ReactMethod
    public void pause(int id) {
        manager.pause(id);
    }

    @ReactMethod
    public void stop(int id) {
        manager.stop(id);
    }

    @ReactMethod
    public void seekTo(int id, double seconds) {
        manager.seekTo(id, seconds);
    }

    @ReactMethod
    public void setVolume(int id, float volume) {
        manager.setVolume(id, volume);
    }

    @ReactMethod
    public void startScan(boolean active, Promise callback) {
        manager.startScan(active, callback);
    }

    @ReactMethod
    public void stopScan() {
        manager.stopScan();
    }

    @ReactMethod
    public void connect(String deviceId, Promise callback) {
        manager.connect(deviceId, callback);
    }

    @ReactMethod
    public void copyQueue(int fromId, int toId, String insertBeforeId, Promise promise) {
        manager.copyQueue(fromId, toId, insertBeforeId, promise);
    }

    @ReactMethod
    public void getCurrentTrack(int id, Callback callback) {
        Utils.triggerCallback(callback, manager.getCurrentTrack(id));
    }

    @ReactMethod
    public void getDuration(int id, Callback callback) {
        Utils.triggerCallback(callback, manager.getDuration(id));
    }

    @ReactMethod
    public void getBufferedPosition(int id, Callback callback) {
        Utils.triggerCallback(callback, manager.getBufferedPosition(id));
    }

    @ReactMethod
    public void getPosition(int id, Callback callback) {
        Utils.triggerCallback(callback, manager.getPosition(id));
    }

    @ReactMethod
    public void getState(int id, Callback callback) {
        Utils.triggerCallback(callback, manager.getState(id));
    }
}
