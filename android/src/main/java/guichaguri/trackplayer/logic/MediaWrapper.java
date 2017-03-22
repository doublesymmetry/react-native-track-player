package guichaguri.trackplayer.logic;

import android.os.Binder;
import android.os.SystemClock;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.view.PlayerView;
import java.io.IOException;

/**
 * A wrapper of {@link MediaManager} to be used as a lightweight {@link android.os.IBinder}
 * @author Guilherme Chaguri
 */
public class MediaWrapper extends Binder {

    private final MediaManager manager;

    public MediaWrapper(MediaManager manager) {
        this.manager = manager;
    }

    public void setOptions(ReadableMap data) {
        manager.updateOptions(data);
    }

    public void setMetadata(ReadableMap data) {
        manager.updateMetadata(data);
    }

    public void resetMetadata() {
        manager.resetMetadata();
    }

    public int createPlayer() {
        return manager.createPlayer();
    }

    public void destroy(int id) {
        manager.destroyPlayer(id);
    }

    public void setMain(int id) {
        if(id != -1) {
            manager.setMainPlayer(manager.getPlayer(id));
        } else {
            manager.setMainPlayer(null);
        }
    }

    public void update(int id, ReadableMap data, Callback callback) {
        if(id != -1) {
            manager.getPlayer(id).update(data, callback);
        } else {
            for(Player p : manager.getPlayers()) p.update(data, callback);
        }
    }

    public void load(int id, ReadableMap data, Callback callback) throws IOException {
        if(id != -1) {
            manager.getPlayer(id).load(data, callback);
        } else {
            for(Player p : manager.getPlayers()) p.load(data, callback);
        }
    }

    public void play(int id) {
        if(id != -1) {
            manager.getPlayer(id).play();
        } else {
            for(Player p : manager.getPlayers()) p.play();
        }
    }

    public void pause(int id) {
        if(id != -1) {
            manager.getPlayer(id).pause();
        } else {
            for(Player p : manager.getPlayers()) p.pause();
        }
    }

    public void stop(int id) {
        if(id != -1) {
            manager.getPlayer(id).stop();
        } else {
            for(Player p : manager.getPlayers()) p.stop();
        }
    }

    public void seekTo(int id, double seconds) {
        long ms = Utils.toMillis(seconds);

        if(id != -1) {
            manager.getPlayer(id).seekTo(ms);
        } else {
            for(Player p : manager.getPlayers()) p.seekTo(ms);
        }
    }

    public void setVolume(int id, float volume) {
        if(id != -1) {
            manager.getPlayer(id).setVolume(volume);
        } else {
            for(Player p : manager.getPlayers()) p.setVolume(volume);
        }
    }

    public void setView(int id, PlayerView view) {
        manager.getPlayer(id).bindView(view);
    }

    public double getDuration(int id) {
        return Utils.toSeconds(manager.getPlayer(id).getDuration());
    }

    public double getBufferedPosition(int id) {
        return Utils.toSeconds(manager.getPlayer(id).getBufferedPosition());
    }

    public double getPosition(int id) {
        Player p = manager.getPlayer(id);

        // Calculate the current position
        long deltaTime = SystemClock.elapsedRealtime() - p.getPositionUpdateTime();
        double speed = p.getSpeed();
        long position = p.getPosition() + (long)(deltaTime * speed);

        return Utils.toSeconds(position);
    }

    public int getState(int id) {
        return manager.getPlayer(id).getState();
    }

}
