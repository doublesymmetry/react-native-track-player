package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.facebook.react.bridge.BaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.PlayerService;

/**
 * @author Guilherme Chaguri
 */
public class TrackModule extends BaseJavaModule implements ServiceConnection, LifecycleEventListener {

    private final ReactApplicationContext context;
    private MediaManager manager = null;

    public TrackModule(ReactApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return "TrackPlayer";
    }

    @Override
    public void initialize() {
        super.initialize();

        context.addLifecycleEventListener(this);

        Intent intent = new Intent(context, PlayerService.class);
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public int createPlayer() {
        return manager.createPlayer();
    }

    @ReactMethod
    public void destroyPlayer(int id) {
        manager.destroyPlayer(id);
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        context.removeLifecycleEventListener(this);
        context.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        manager = (MediaManager)service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        manager = null;
    }
}
