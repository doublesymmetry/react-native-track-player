package guichaguri.trackplayer.metadata;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaReceiver;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.RemotePlayer;

/**
 * @author Guilherme Chaguri
 */
public class Metadata {

    private final MediaSessionCompat session;
    private final MediaNotification notification;
    private Player player;

    private CustomVolume volume = null;
    private MediaMetadataCompat.Builder md = new MediaMetadataCompat.Builder();
    private PlaybackStateCompat.Builder pb = new PlaybackStateCompat.Builder();

    public Metadata(Context context) {
        ComponentName comp = new ComponentName(context, MediaReceiver.class);
        session = new MediaSessionCompat(context, "TrackPlayer", comp, null);

        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new ButtonListener());

        notification = new MediaNotification(context, session);
    }

    public void setEnabled(boolean enabled) {
        session.setActive(enabled);
    }

    public void attachPlayer(Player player) {
        // Unbind the old player
        if(this.player != null) this.player.bindMetadata(null);

        // Set the player
        this.player = player;

        // Bind the new player
        if(this.player != null) this.player.bindMetadata(this);
    }

    public void updateMetadata(ReadableMap data) {
        //TODO
    }

    @SuppressWarnings("WrongConstant")
    public void updatePlayback() {
        if(player == null) return;

        pb.setState(player.getState(), player.getPosition(), player.getSpeed(), player.getPositionUpdateTime());
        pb.setBufferedPosition(player.getBufferedPosition());

        session.setPlaybackState(pb.build());

        if(player instanceof RemotePlayer) {
            RemotePlayer remote = (RemotePlayer)player;
            volume = CustomVolume.updateVolume(volume, remote.canChangeVolume(), (int)(remote.getVolume() * 100), 100);
            session.setPlaybackToRemote(volume);
        } else {
            session.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
            volume = null;
        }
    }

    public void reset() {
        md = new MediaMetadataCompat.Builder();
        pb = new PlaybackStateCompat.Builder();
        volume = null;

        session.setMetadata(md.build());
        session.setPlaybackState(pb.build());
    }

    public void destroy() {
        session.release();
    }

}
