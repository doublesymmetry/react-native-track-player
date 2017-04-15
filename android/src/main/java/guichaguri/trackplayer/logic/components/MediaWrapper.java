package guichaguri.trackplayer.logic.components;

import android.os.Binder;
import android.os.SystemClock;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.player.Player;

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

    public void add(int id, String insertBeforeId, ReadableArray data, Promise callback) {
        if(id != -1) {
            manager.getPlayer(id).add(insertBeforeId, data, callback);
        } else {
            for(Player p : manager.getPlayers()) p.add(insertBeforeId, data, callback);
        }
    }

    public void remove(int id, String[] tracks, Promise callback) {
        if(id != -1) {
            manager.getPlayer(id).remove(tracks, callback);
        } else {
            for(Player p : manager.getPlayers()) p.remove(tracks, callback);
        }
    }

    public void skip(int id, String track, Promise callback) {
        if(id != -1) {
            manager.getPlayer(id).skip(track, callback);
        } else {
            for(Player p : manager.getPlayers()) p.skip(track, callback);
        }
    }

    public void skipToNext(int id, Promise callback) {
        if(id != -1) {
            manager.getPlayer(id).skipToNext(callback);
        } else {
            for(Player p : manager.getPlayers()) p.skipToNext(callback);
        }
    }

    public void skipToPrevious(int id, Promise callback) {
        if(id != -1) {
            manager.getPlayer(id).skipToPrevious(callback);
        } else {
            for(Player p : manager.getPlayers()) p.skipToPrevious(callback);
        }
    }

    public void load(int id, ReadableMap data, Promise callback) {
        if(id != -1) {
            manager.getPlayer(id).load(data, callback);
        } else {
            for(Player p : manager.getPlayers()) p.load(data, callback);
        }
    }

    public void reset(int id) {
        if(id != -1) {
            manager.getPlayer(id).reset();
        } else {
            for(Player p : manager.getPlayers()) p.reset();
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

    public void startScan(boolean active, Promise callback) {
        manager.getRemote().startScan(active, callback);
    }

    public void stopScan() {
        manager.getRemote().stopScan();
    }

    public void connect(String id, Promise callback) {
        manager.getRemote().connect(id, callback);
    }

    public void copyQueue(int fromId, int toId, String insertBeforeId, Promise promise) {
        Player fromPlayer = manager.getPlayer(fromId);
        Player toPlayer = manager.getPlayer(toId);

        fromPlayer.copyQueue(toPlayer, insertBeforeId, promise);
    }

    public String getCurrentTrack(int id) {
        Track track = manager.getPlayer(id).getCurrentTrack();
        return track != null ? track.id : null;
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
