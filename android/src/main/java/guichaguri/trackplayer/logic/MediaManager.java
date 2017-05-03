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
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.RemotePlayer;
import guichaguri.trackplayer.player.players.AndroidPlayer;
import guichaguri.trackplayer.player.players.ExoPlayer;
import guichaguri.trackplayer.remote.Remote;
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
    private final Remote remote;
    private final Map<Integer, Player<? extends Track>> players = new HashMap<>();

    private int lastId = 0;
    private Player<? extends Track> mainPlayer;
    private boolean serviceStarted = false;

    public MediaManager(PlayerService service) {
        this.service = service;
        this.metadata = new Metadata(service, this);
        this.remote = new Remote(service.getApplicationContext(), this);
        this.focus = new FocusManager(service, metadata);
    }

    public void updateOptions(ReadableMap data) {
        remote.updateOptions(data);
        metadata.updateOptions(data);
        metadata.updatePlayback(mainPlayer);
    }

    public int createPlayer() {
        Player<? extends Track> player;

        if(LibHelper.isExoPlayerAvailable()) {
            Utils.log("Creating an ExoPlayer instance...");
            player = new ExoPlayer(service, this);
        } else {
            Utils.log("Creating a MediaPlayer instance...");
            player = new AndroidPlayer(service, this);
        }

        return addPlayer(player);
    }

    public int addPlayer(Player<? extends Track> player) {
        int id = lastId++;
        players.put(id, player);
        return id;
    }

    public void destroyPlayer(int id) {
        if(id == -1) {
            Utils.log("Destroying all players...");

            // Destroys all players
            for(Player p : players.values()) p.destroy();
            players.clear();

            setMainPlayer(null);
        } else {
            Utils.log("Destroying player %d...", id);

            Player player = players.remove(id);
            player.destroy();

            if(player == mainPlayer) setMainPlayer(null);
        }

        if(serviceStarted && players.isEmpty() && !remote.isScanning()) {
            Utils.log("Marking the service as stopped, as there are no more players playing");
            service.stopSelf();
            serviceStarted = false;
        }
    }

    public Player<? extends Track> getPlayer(int id) {
        return id >= 0 ? players.get(id) : null;
    }

    public Collection<Player<? extends Track>> getPlayers() {
        return players.values();
    }

    public Remote getRemote() {
        return remote;
    }

    public int getRatingType() {
        return metadata.getRatingType();
    }

    public void setMainPlayer(Player<? extends Track> player) {
        if(player == mainPlayer) return;

        // Set the main player
        mainPlayer = player;

        // Update the metadata
        metadata.updatePlayback(mainPlayer);
        metadata.updateMetadata(mainPlayer);
    }

    public Player<? extends Track> getMainPlayer() {
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

    public void onStateChange(Player player, int state) {
        WritableMap data = Arguments.createMap();
        data.putInt("state", state);
        Events.dispatchEvent(service, getPlayerId(player), Events.PLAYER_STATE, data);
    }

    public void onUpdate(Player player) {
        if(mainPlayer == player) {
            metadata.updatePlayback(player);
            metadata.updateMetadata(player);
        }
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
        Utils.log("Destroying resources");
        for(Player player : getPlayers()) {
            player.destroy();
        }
        focus.disable();
        metadata.destroy();
    }

    public void onScanningStart() {
        if(!serviceStarted) {
            Utils.log("Marking the service as started, as we are now searching for remote devices");
            service.startService(new Intent(service, PlayerService.class));
            serviceStarted = true;
        }
    }

    public void onScanningStop() {
        if(serviceStarted && players.isEmpty() && !remote.isScanning()) {
            Utils.log("Marking the service as stopped, as we are not searching for remote devices anymore");
            service.stopSelf();
            serviceStarted = false;
        }
    }

    private void onPlayerPlay(Player player) {
        if(!(player instanceof RemotePlayer)) {
            focus.enable();
        }

        if(!serviceStarted) {
            Utils.log("Marking the service as started, as a player is now playing");
            service.startService(new Intent(service, PlayerService.class));
            serviceStarted = true;
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
