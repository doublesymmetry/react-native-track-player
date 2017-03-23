package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.PlayerService;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import java.io.IOException;

/**
 * @author Guilherme Chaguri
 */
public class TrackModule extends ReactContextBaseJavaModule implements ServiceConnection, LifecycleEventListener {

    private MediaWrapper manager = null;

    public TrackModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "TrackPlayer";
    }

    @Override
    public void initialize() {
        super.initialize();

        ReactApplicationContext context = getReactApplicationContext();

        context.addLifecycleEventListener(this);

        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_MEDIA);
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        ReactApplicationContext context = getReactApplicationContext();

        context.removeLifecycleEventListener(this);
        context.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        manager = (MediaWrapper)service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        manager = null;
    }

    /* ****************************** Native Functions ****************************** */

    @ReactMethod
    public void setOptions(ReadableMap data) {
        manager.setOptions(data);
    }

    @ReactMethod
    public void setMetadata(ReadableMap data) {
        manager.setMetadata(data);
    }

    @ReactMethod
    public void resetMetadata() {
        manager.resetMetadata();
    }

    @ReactMethod
    public int createPlayer() {
        return manager.createPlayer();
    }

    @ReactMethod
    public void destroy(int id) {
        manager.destroy(id);
    }

    @ReactMethod
    public void setMain(int id) {
        manager.setMain(id);
    }

    @ReactMethod
    public void load(int id, ReadableMap data, Callback callback) throws IOException {
        manager.load(id, data, callback);
    }

    @ReactMethod
    public void update(int id, ReadableMap data, Callback callback) {
        manager.update(id, data, callback);
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
    public double getDuration(int id) {
        return manager.getDuration(id);
    }

    @ReactMethod
    public double getBufferedPosition(int id) {
        return manager.getBufferedPosition(id);
    }

    @ReactMethod
    public double getPosition(int id) {
        return manager.getPosition(id);
    }

    @ReactMethod
    public int getState(int id) {
        return manager.getState(id);
    }
}
