package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.OnStatusUpdatedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.WebImage;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.RemotePlayer;
import guichaguri.trackplayer.player.Chromecast;
import guichaguri.trackplayer.player.track.CastTrack;
import java.io.IOException;

/**
 * @author Guilherme Chaguri
 */
public class CastPlayer extends RemotePlayer<CastTrack> implements OnStatusUpdatedListener {

    private final Chromecast cast;
    private final GoogleApiClient client;
    private final RemoteMediaPlayer player;

    public CastPlayer(Context context, Chromecast cast, MediaManager manager, GoogleApiClient client) {
        super(context, manager);
        this.cast = cast;
        this.client = client;

        player = new RemoteMediaPlayer();
        player.setOnStatusUpdatedListener(this);
    }

    @Override
    protected CastTrack createTrack(ReadableMap data) {
        return new CastTrack(manager, data);
    }

    @Override
    public void update(ReadableMap data, Callback updateCallback) {
        updateCallback.invoke();
    }

    @Override
    public void load(CastTrack track, Callback loadCallback) throws IOException {
        MediaMetadata metadata = new MediaMetadata();
        metadata.putString(MediaMetadata.KEY_TITLE, track.title);
        metadata.putString(MediaMetadata.KEY_ARTIST, track.artist);
        metadata.putString(MediaMetadata.KEY_ALBUM_TITLE, track.album);
        metadata.putDate(MediaMetadata.KEY_RELEASE_DATE, metadata.getDate(track.date));
        metadata.addImage(new WebImage(Utils.toUri(context, track.artwork.url, track.artwork.local)));

        String id = !track.sendUrl || track.url.local ? track.id : track.url.url;

        MediaInfo media = new MediaInfo.Builder(id)
                .setStreamDuration(track.duration)
                .setContentType(track.contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_INVALID)
                .setMetadata(metadata)
                .setCustomData(track.customData)
                .build();

        player.load(client, media);
    }

    @Override
    public void reset() {
        player.stop(client);
    }

    @Override
    public void play() {
        player.play(client);
    }

    @Override
    public void pause() {
        player.pause(client);
    }

    @Override
    public void stop() {
        player.stop(client);
    }

    @Override
    public int getState() {
        switch(player.getMediaStatus().getPlayerState()) {
            case MediaStatus.PLAYER_STATE_UNKNOWN:
            case MediaStatus.PLAYER_STATE_IDLE:
                return PlaybackStateCompat.STATE_NONE;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case MediaStatus.PLAYER_STATE_PLAYING:
                return PlaybackStateCompat.STATE_PLAYING;
            case MediaStatus.PLAYER_STATE_PAUSED:
                return PlaybackStateCompat.STATE_PAUSED;
        }
        return PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public long getPosition() {
        return player.getApproximateStreamPosition();
    }

    @Override
    public long getBufferedPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
        return player.getStreamDuration();
    }

    @Override
    public void seekTo(long ms) {
        player.seek(client, ms);
    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    public void setVolume(float volume) {
        player.setStreamVolume(client, volume);
    }

    @Override
    public float getVolume() {
        MediaStatus status = player.getMediaStatus();
        if(status != null) {
            return (float)status.getStreamVolume();
        } else {
            return (float)Cast.CastApi.getVolume(client);
        }
    }

    @Override
    public boolean canChangeVolume() {
        return true;
    }

    @Override
    public void destroy() {
        cast.disconnect();
    }

    @Override
    public void onStatusUpdated() {
        updateState(getState());
    }

    public void onVolumeChanged() {
        updateMetadata();
    }
}
