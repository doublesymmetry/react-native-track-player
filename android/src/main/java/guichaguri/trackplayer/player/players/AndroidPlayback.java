package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.LibHelper;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.player.Playback;
import guichaguri.trackplayer.player.components.ProxyCache;
import java.io.IOException;

/**
 * Basic player using Android's {@link MediaPlayer}
 *
 * @author Guilherme Chaguri
 */
public class AndroidPlayback extends Playback implements OnInfoListener, OnCompletionListener,
        OnSeekCompleteListener, OnPreparedListener, OnBufferingUpdateListener, OnErrorListener {

    private final MediaPlayer player;
    private final ProxyCache cache;

    private Promise loadCallback;

    private boolean loaded = false;
    private boolean buffering = false;
    private boolean ended = false;
    private boolean started = false;

    private float buffered = 0;
    private float volume = 1;

    public AndroidPlayback(Context context, MediaManager manager, ReadableMap map) {
        super(context, manager);

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnInfoListener(this);
        player.setOnCompletionListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnPreparedListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnErrorListener(this);

        ReadableMap cacheMap = Utils.getMap(map, "cache");

        if(LibHelper.isProxyCacheAvailable() && cacheMap != null) {
            int maxFiles = Utils.getInt(cacheMap, "maxFiles", 0);
            long maxSize = (long)(Utils.getDouble(cacheMap, "maxSize", 0) * 1024);

            cache = new ProxyCache(context, maxFiles, maxSize);
        } else {
            cache = null;
        }
    }

    @Override
    public void load(Track track, Promise callback) {
        Uri url = track.url;

        // Resets the player to update its state to idle
        player.reset();

        // Prepares the caching
        if(cache != null && !track.urlLocal) {
            url = cache.getURL(url, track.id);
        }

        // Updates the state
        buffering = true;
        ended = false;
        loaded = false;

        try {
            // Loads the uri
            loadCallback = callback;
            player.setDataSource(context, url);
            player.prepareAsync();
        } catch(IOException ex) {
            loadCallback = null;
            Utils.rejectCallback(callback, ex);
            manager.onError(ex);
        }

        updateState();
    }

    @Override
    public void reset() {
        super.reset();

        // Release the playback resources
        player.reset();

        // Update the state
        buffering = false;
        ended = false;
        loaded = false;
        updateState();
    }

    @Override
    public void play() {
        started = true;

        if(!loaded) return;

        player.start();

        buffering = false;
        ended = false;
        updateState();
    }

    @Override
    public void pause() {
        started = false;

        if(!loaded) return;

        player.pause();

        updateState();
    }

    @Override
    public void stop() {
        started = false;

        if(!loaded) return;

        player.stop();

        ended = true;
        updateState();
    }

    @Override
    public int getState() {
        if(ended) return PlaybackStateCompat.STATE_STOPPED;
        if(buffering) return PlaybackStateCompat.STATE_BUFFERING;
        if(!loaded) return PlaybackStateCompat.STATE_NONE;
        if(!player.isPlaying()) return PlaybackStateCompat.STATE_PAUSED;
        return PlaybackStateCompat.STATE_PLAYING;
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
        if(VERSION.SDK_INT >= VERSION_CODES.M) {
            return player.getPlaybackParams().getSpeed();
        } else {
            return 1;
        }
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setVolume(float vol) {
        volume = vol;
        player.setVolume(vol, vol);
        manager.onPlaybackUpdate();
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public void destroy() {
        player.release();

        if(cache != null) {
            cache.destroy();
        }
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
        ended = true;
        updateState();

        manager.onEnd();

        skipToNext(null);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        buffering = false;
        updateState();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(started) player.start();

        Utils.resolveCallback(loadCallback);
        loadCallback = null;

        loaded = true;
        buffering = false;
        updateState();

        manager.onLoad(getCurrentTrack());
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        buffered = percent / 100F;
        manager.onPlaybackUpdate();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Exception ex;
        if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            ex = new IOException("Server died");
        } else {
            ex = new RuntimeException("Unknown error");
        }

        Utils.rejectCallback(loadCallback, ex);
        loadCallback = null;

        manager.onError(ex);
        return true;
    }

    private void updateState() {
        updateState(getState());
    }
}
