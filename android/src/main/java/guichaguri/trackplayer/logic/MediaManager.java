package guichaguri.trackplayer.logic;

import android.content.Intent;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.components.FocusManager;
import guichaguri.trackplayer.metadata.Metadata;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.RemotePlayer;
import guichaguri.trackplayer.player.players.AndroidPlayer;
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

    public MediaManager(PlayerService service) {
        this.service = service;
        this.metadata = new Metadata(service);
        this.focus = new FocusManager(service, metadata);
    }

    public void updateOptions(ReadableMap data) {
        metadata.updateOptions(data);
        metadata.updatePlayback(mainPlayer);
    }

    public void updateMetadata(ReadableMap data) {
        metadata.updateMetadata(mainPlayer, data);
    }

    public void resetMetadata() {
        metadata.reset();
        mainPlayer = null;
    }

    public int createPlayer() {
        int id = lastId++;
        players.put(id, new AndroidPlayer(service, this)); // TODO type
        return id;
    }

    public void destroyPlayer(int id) {
        if(id == -1) {
            // Destroys all players
            for(Player p : players.values()) p.destroy();
            players.clear();
        } else {
            players.remove(id).destroy();
        }
    }

    public Player getPlayer(int id) {
        if(id < 0 || !players.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        return players.get(id);
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public void setMainPlayer(Player player) {
        // Set the main player
        mainPlayer = player;

        // Update the playback state
        metadata.updatePlayback(mainPlayer);
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

    public void onUpdate(Player player) {
        if(mainPlayer == player) {
            metadata.updatePlayback(player);
        }
    }

    public void onCommand(Intent intent) {
        metadata.handleIntent(intent);
    }

    public void onServiceDestroy() {
        for(Player player : getPlayers()) {
            player.destroy();
        }
        metadata.destroy();
    }

    private void onPlayerPlay(Player player) {
        if(!(player instanceof RemotePlayer)) {
            focus.enable();
        }
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
