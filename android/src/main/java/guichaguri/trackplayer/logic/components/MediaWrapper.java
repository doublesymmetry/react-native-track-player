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
        Utils.log("Updating options...");
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
            Utils.log("Setting %d as the main player...", id);
            manager.setMainPlayer(manager.getPlayer(id));
        } else {
            Utils.log("Removing the main player...");
            manager.setMainPlayer(null);
        }
    }

    public void add(int id, String insertBeforeId, ReadableArray data, Promise callback) {
        if(id != -1) {
            Utils.log("Adding a track to %d...", id);
            manager.getPlayer(id).add(insertBeforeId, data, callback);
        } else {
            Utils.log("Adding a track to all players...");
            for(Player p : manager.getPlayers()) p.add(insertBeforeId, data, callback);
        }
    }

    public void remove(int id, ReadableArray tracks, Promise callback) {
        if(id != -1) {
            Utils.log("Removing tracks from %d...", id);
            manager.getPlayer(id).remove(tracks, callback);
        } else {
            Utils.log("Removing tracks from all players...");
            for(Player p : manager.getPlayers()) p.remove(tracks, callback);
        }
    }

    public void skip(int id, String track, Promise callback) {
        if(id != -1) {
            Utils.log("Skipping to %s in %d...", track, id);
            manager.getPlayer(id).skip(track, callback);
        } else {
            Utils.log("Skipping to %s in all players...", track);
            for(Player p : manager.getPlayers()) p.skip(track, callback);
        }
    }

    public void skipToNext(int id, Promise callback) {
        if(id != -1) {
            Utils.log("Skipping to next in %d...", id);
            manager.getPlayer(id).skipToNext(callback);
        } else {
            Utils.log("Skipping to next in all players...");
            for(Player p : manager.getPlayers()) p.skipToNext(callback);
        }
    }

    public void skipToPrevious(int id, Promise callback) {
        if(id != -1) {
            Utils.log("Skipping to previous in %d...", id);
            manager.getPlayer(id).skipToPrevious(callback);
        } else {
            Utils.log("Skipping to previous in all players...");
            for(Player p : manager.getPlayers()) p.skipToPrevious(callback);
        }
    }

    public void load(int id, ReadableMap data, Promise callback) {
        if(id != -1) {
            Utils.log("Loading a track in %d...", id);
            manager.getPlayer(id).load(data, callback);
        } else {
            Utils.log("Loading a track in all players...");
            for(Player p : manager.getPlayers()) p.load(data, callback);
        }
    }

    public void reset(int id) {
        if(id != -1) {
            Utils.log("Resetting %d...", id);
            manager.getPlayer(id).reset();
        } else {
            Utils.log("Resetting all players...", id);
            for(Player p : manager.getPlayers()) p.reset();
        }
    }

    public void play(int id) {
        if(id != -1) {
            Utils.log("Sending play command to %d...", id);
            manager.getPlayer(id).play();
        } else {
            Utils.log("Sending play command to all players...");
            for(Player p : manager.getPlayers()) p.play();
        }
    }

    public void pause(int id) {
        if(id != -1) {
            Utils.log("Sending pause command to %d...", id);
            manager.getPlayer(id).pause();
        } else {
            Utils.log("Sending pause command to all players...");
            for(Player p : manager.getPlayers()) p.pause();
        }
    }

    public void stop(int id) {
        if(id != -1) {
            Utils.log("Sending stop command to %d...", id);
            manager.getPlayer(id).stop();
        } else {
            Utils.log("Sending stop command to all players...");
            for(Player p : manager.getPlayers()) p.stop();
        }
    }

    public void seekTo(int id, double seconds) {
        long ms = Utils.toMillis(seconds);

        if(id != -1) {
            Utils.log("Seeking to %d seconds in %d...", seconds, id);
            manager.getPlayer(id).seekTo(ms);
        } else {
            Utils.log("Seeking to %d seconds in all players...", seconds);
            for(Player p : manager.getPlayers()) p.seekTo(ms);
        }
    }

    public void setVolume(int id, float volume) {
        if(id != -1) {
            Utils.log("Setting volume to %d in %d...", volume, id);
            manager.getPlayer(id).setVolume(volume);
        } else {
            Utils.log("Setting volume to %d in all players...", volume);
            for(Player p : manager.getPlayers()) p.setVolume(volume);
        }
    }

    public void startScan(boolean active, Promise callback) {
        Utils.log("Starting device scan... (active = %b)", active);
        manager.getRemote().startScan(active, callback);
    }

    public void stopScan() {
        Utils.log("Stopping device scan...");
        manager.getRemote().stopScan();
    }

    public void connect(String id, Promise callback) {
        Utils.log("Connecting to %s...", id);
        manager.getRemote().connect(id, callback);
    }

    public void copyQueue(int fromId, int toId, String insertBeforeId, Promise promise) {
        Utils.log("Copying queue from %d to %d...", fromId, toId);
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
