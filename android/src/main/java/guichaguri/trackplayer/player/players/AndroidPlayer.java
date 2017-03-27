package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.components.PlayerView;
import java.io.IOException;

/**
 * Basic player using Android's {@link MediaPlayer}
 *
 * @author Guilherme Chaguri
 */
public class AndroidPlayer extends Player implements OnInfoListener, OnCompletionListener,
        OnSeekCompleteListener, OnPreparedListener, OnBufferingUpdateListener {

    private final MediaPlayer player;

    private Callback loadCallback;

    private boolean loaded = false;
    private boolean buffering = false;
    private boolean paused = false;

    private float buffered = 0;

    public AndroidPlayer(Context context, MediaManager manager) {
        super(context, manager);

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnInfoListener(this);
        player.setOnCompletionListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnPreparedListener(this);
        player.setOnBufferingUpdateListener(this);
    }

    @Override
    public void update(ReadableMap data, Callback updateCallback) {
        player.setScreenOnWhilePlaying(Utils.getBoolean(data, "keepScreenActive", false));

        updateCallback.invoke();
    }

    @Override
    public void load(ReadableMap data, Callback callback) throws IOException {
        Uri url = Utils.getUri(context, data, "url");
        buffering = true;
        loadCallback = callback;

        player.setDataSource(context, url);
        player.prepareAsync();

        updateState();
    }

    @Override
    public void reset() {
        player.reset();

        buffering = false;
        paused = false;
        loaded = false;

        updateState();
    }

    @Override
    public void play() {
        player.start();
        buffering = false;
        paused = false;
        updateState();
    }

    @Override
    public void pause() {
        player.pause();
        paused = true;
        updateState();
    }

    @Override
    public void stop() {
        player.stop();
        buffering = false;
        paused = false;
        updateState();
    }

    @Override
    public int getState() {
        if(buffering) return PlaybackStateCompat.STATE_BUFFERING;
        if(!loaded) return PlaybackStateCompat.STATE_NONE;
        if(paused) return PlaybackStateCompat.STATE_PAUSED;
        if(player.isPlaying()) return PlaybackStateCompat.STATE_PLAYING;
        return PlaybackStateCompat.STATE_STOPPED;
    }

    @Override
    public long getPosition() {
        return loaded ? player.getCurrentPosition() : 0;
    }

    @Override
    public long getBufferedPosition() {
        return (long)(buffered * getDuration());
    }

    @Override
    public long getDuration() {
        return loaded ? player.getDuration() : 0;
    }

    @Override
    public void seekTo(long ms) {
        buffering = true;
        player.seekTo((int)ms);
        updateState();
    }

    @Override
    public float getSpeed() {
        return 1; // player.getPlaybackParams().getSpeed();
    }

    @Override
    public void setVolume(float volume) {
        player.setVolume(volume, volume);
        updateMetadata();
    }

    @Override
    public void bindView(PlayerView view) {
        player.setDisplay(view != null ? view.getHolder() : null);
    }

    @Override
    public void destroy() {
        player.release();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            buffering = true;
            updateState();
        } else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            buffering = false;
            updateState();
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        buffering = false;
        updateState();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(loadCallback != null) {
            loadCallback.invoke();
            loadCallback = null;
        }

        loaded = true;
        buffering = false;
        updateState();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        buffered = percent / 100F;
        updateMetadata();
    }

    private void updateState() {
        updateState(getState());
    }
}
