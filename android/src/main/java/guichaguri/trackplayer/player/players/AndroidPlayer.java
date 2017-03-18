package guichaguri.trackplayer.player.players;

import android.content.Context;
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
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.Player;
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

    private boolean buffering = false;
    private boolean paused = false;

    private float buffered = 0;

    public AndroidPlayer(Context context) {
        super(context);
        player = new MediaPlayer();
        player.setOnInfoListener(this);
        player.setOnCompletionListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnPreparedListener(this);
        player.setOnBufferingUpdateListener(this);
    }

    @Override
    public void load(ReadableMap data, Callback callback) throws IOException {
        Uri url = Utils.getUri(context, data, "url");
        buffering = true;
        loadCallback = callback;

        player.setDataSource(context, url);
        player.prepareAsync();

        updateMetadata();
    }

    @Override
    public void play() {
        player.start();
        buffering = false;
        paused = false;
        updateMetadata();
    }

    @Override
    public void pause() {
        player.pause();
        paused = true;
        updateMetadata();
    }

    @Override
    public void stop() {
        player.stop();
        buffering = false;
        paused = false;
        updateMetadata();
    }

    @Override
    public int getState() {
        if(buffering) return PlaybackStateCompat.STATE_BUFFERING;
        if(paused) return PlaybackStateCompat.STATE_PAUSED;
        if(player.isPlaying()) return PlaybackStateCompat.STATE_PLAYING;
        return PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public long getPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getBufferedPosition() {
        return (long)(buffered * player.getDuration());
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public void seekTo(int ms) {
        buffering = true;
        player.seekTo(ms);
        updateMetadata();
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
    public void destroy() {
        player.release();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            buffering = true;
            updateMetadata();
        } else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            buffering = false;
            updateMetadata();
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateMetadata();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        buffering = false;
        updateMetadata();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(loadCallback != null) {
            loadCallback.invoke();
            loadCallback = null;
        }

        buffering = false;
        updateMetadata();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        buffered = percent / 100F;
        updateMetadata();
    }
}
