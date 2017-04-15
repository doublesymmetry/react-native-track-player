package guichaguri.trackplayer.logic;

import android.content.Intent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import guichaguri.trackplayer.logic.components.FocusManager;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.logic.workers.PlayerService;
import guichaguri.trackplayer.metadata.Metadata;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.player.Chromecast;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.RemotePlayer;
import guichaguri.trackplayer.player.players.AndroidPlayer;
import guichaguri.trackplayer.player.players.ExoPlayer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guilherme Chaguri
 */
public class MediaManager {

    private final PlayerService service;
    private final FocusManager focus;
    private final Metadata metadata;
    private final Map<Integer, Player> players = new HashMap<>();

    private int lastId = 0;
    private Player mainPlayer;
    private Chromecast cast;

    public MediaManager(PlayerService service) {
        this.service = service;
        this.metadata = new Metadata(service, this);
        this.focus = new FocusManager(service, metadata);
    }

    public void updateOptions(ReadableMap data) {
        metadata.updateOptions(data);
        metadata.updatePlayback(mainPlayer);

        ReadableMap castConfig = Utils.getMap(data, "cast");
        boolean castEnabled = castConfig != null && Utils.getBoolean(castConfig, "enabled", false);

        if(castEnabled && LibHelper.isChromecastAvailable(service)) {
            if(cast == null) cast = new Chromecast(service, this);
            cast.updateOptions(castConfig);
        } else {
            cast = null;
        }
    }

    public int createPlayer() {
        Player player;

        if(LibHelper.isExoPlayerAvailable()) {
            player = new ExoPlayer(service, this);
        } else {
            player = new AndroidPlayer(service, this);
        }

        return addPlayer(player);
    }

    public void destroyPlayer(int id) {
        if(id == -1) {
            // Destroys all players
            for(Player p : players.values())
                try {
                    p.destroy();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            players.clear();
        } else {
            try {
                players.remove(id).destroy();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Player getPlayer(int id) {
        if(id < 0 || !players.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        return players.get(id);
    }

    public int addPlayer(Player player) {
        int id = lastId++;
        players.put(id, player);
        return id;
    }

    public void removePlayer(Player player) {
        players.remove(getPlayerId(player));
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public Chromecast getCast() {
        return cast;
    }

    public int getRatingType() {
        return metadata.getRatingType();
    }

    public void setMainPlayer(Player player) {
        // Set the main player
        mainPlayer = player;

        // Update the metadata
        metadata.updatePlayback(mainPlayer);
        metadata.updateMetadata(mainPlayer);
    }

    public Player getMainPlayer() {
        return mainPlayer;
    }

    public int getPlayerId(Player player) {
        for(Integer id : players.keySet()) {
            if(players.get(id) == player) return id;
        }
        return -1;
    }

    public void onPlay(Player player) {
        if(mainPlayer == player) {
            onMainPlayerPlay();
        }
        onPlayerPlay(player);
    }

    public void onPause(Player player) {
        if(mainPlayer == player) {
            onMainPlayerPause();
        }
        onPlayerPause(player);
    }

    public void onStop(Player player) {
        if(mainPlayer == player) {
            onMainPlayerStop();
        }
        onPlayerStop(player);
    }

    public void onLoad(Player player, Track track) {
        WritableMap data = Arguments.createMap();
        data.putString("track", track.id);
        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_LOAD, data);
    }

    public void onEnd(Player player) {
        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_ENDED, null);
    }

    public void onUpdate(Player player) {
        if(mainPlayer == player) {
            metadata.updatePlayback(player);
            metadata.updateMetadata(player);
        }

        WritableMap data = Arguments.createMap();
        data.putInt("state", player.getState());
        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_STATE, data);
    }

    public void onError(Player player, Throwable error) {
        WritableMap data = Arguments.createMap();
        data.putString("error", error.getMessage());
        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_ERROR, data);
    }

    public void onCommand(Intent intent) {
        metadata.handleIntent(intent);
    }

    public void onServiceDestroy() {
        for(Player player : getPlayers()) {
            try {
                player.destroy();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        metadata.destroy();
    }

    private void onPlayerPlay(Player player) {
        if(!(player instanceof RemotePlayer)) {
            focus.enable();
        }

        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_PLAY, null);
    }

    private void onMainPlayerPlay() {
        MediaNotification notification = metadata.getNotification();

        // Set the service as foreground, updating and showing the notification
        service.startForeground(MediaNotification.NOTIFICATION_ID, notification.build());
        notification.setShowing(true);

        // Activate the session
        metadata.setEnabled(true);
    }

    private void onPlayerPause(Player player) {
        if(!isPlayingLocal()) {
            // When there are no more local players, we'll disable the audio focus
            focus.disable();
        }

        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_PAUSE, null);
    }

    private void onMainPlayerPause() {
        // Set the service as background, keeping the notification
        service.stopForeground(false);
    }

    private void onPlayerStop(Player player) {
        if(!isPlayingLocal()) {
            // When there are no more local players, we'll disable the audio focus
            focus.disable();
        }

        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_STOP, null);
    }

    private void onMainPlayerStop() {
        // Set the service as background, removing the notification
        metadata.getNotification().setShowing(false);
        service.stopForeground(true);

        // Deactivate the session
        metadata.setEnabled(false);
    }

    private boolean isPlayingLocal() {
        for(Player p : getPlayers()) {
            if(p instanceof RemotePlayer) continue;
            if(Utils.isPlaying(p.getState())) return true;
        }
        return false;
    }

}
