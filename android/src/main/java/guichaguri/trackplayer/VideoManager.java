package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import guichaguri.trackplayer.logic.components.VideoWrapper;
import guichaguri.trackplayer.logic.workers.PlayerService;
import guichaguri.trackplayer.player.components.PlayerView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class VideoManager extends SimpleViewManager<PlayerView> implements ServiceConnection {

    private final ReactApplicationContext context;

    private boolean serviceEnabled = false;
    private VideoWrapper video;
    private List<PlayerView> views = new ArrayList<>();

    public VideoManager(ReactApplicationContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return "TrackPlayerView";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        setServiceEnabled(false);
    }

    @Override
    protected PlayerView createViewInstance(ThemedReactContext context) {
        setServiceEnabled(true);

        PlayerView view = new PlayerView(context);
        views.add(view);
        return view;
    }

    @Override
    public void onDropViewInstance(PlayerView view) {
        // Unbind the player
        view.bindPlayer(video, -1);
        views.remove(view);

        if(views.isEmpty()) setServiceEnabled(false);
    }

    @ReactProp(name = "player", defaultInt = -1)
    public void setPlayer(PlayerView view, int id) {
        // Bind the player
        view.bindPlayer(video, id);
    }

    @ReactProp(name = "keepScreenAwake", defaultBoolean = false)
    public void setKeepScreenAwake(PlayerView view, boolean awake) {
        // Keep the screen awake
        view.setKeepScreenOn(awake);
    }

    private void setServiceEnabled(boolean enabled) {
        if(serviceEnabled == enabled) return;

        if(enabled) {
            Intent intent = new Intent(context, PlayerService.class);
            intent.setAction(PlayerService.ACTION_VIDEO);
            context.bindService(intent, this, Service.BIND_AUTO_CREATE);
        } else {
            context.unbindService(this);
        }
        serviceEnabled = enabled;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        video = (VideoWrapper)service;

        // Update the views
        for(PlayerView view : views) {
            view.updatePlayer(video);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        video = null;
    }
}
