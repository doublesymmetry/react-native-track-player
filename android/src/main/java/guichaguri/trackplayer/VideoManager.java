package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import guichaguri.trackplayer.logic.PlayerService;
import guichaguri.trackplayer.logic.components.VideoWrapper;
import guichaguri.trackplayer.player.view.PlayerView;

/**
 * @author Guilherme Chaguri
 */
public class VideoManager extends SimpleViewManager<PlayerView> implements ServiceConnection, LifecycleEventListener {

    private final ReactApplicationContext context;

    private VideoWrapper video;
    private int boundPlayer = -1;

    public VideoManager(ReactApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return "PlayerView";
    }

    @Override
    public void initialize() {
        super.initialize();

        context.addLifecycleEventListener(this);

        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_VIDEO);
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected PlayerView createViewInstance(ThemedReactContext context) {
        return new PlayerView(context);
    }

    @Override
    public void onDropViewInstance(PlayerView view) {
        // Unbind the player
        if(boundPlayer != -1) {
            video.setView(boundPlayer, null);
        }
    }

    @ReactProp(name = "player", defaultInt = -1)
    public void setPlayer(PlayerView view, int id) {
        // Unbind the old player
        if(boundPlayer != -1) {
            video.setView(boundPlayer, null);
        }

        // Bind the new player
        if(id != -1) {
            video.setView(id, view);
        }

        boundPlayer = id;
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
        video = (VideoWrapper)service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        video = null;
    }
}
