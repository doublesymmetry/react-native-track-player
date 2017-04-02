package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.LibHelper;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.components.PlayerView;
import guichaguri.trackplayer.player.components.ProxyCache;
import java.io.IOException;

/**
 * Basic player using Android's {@link MediaPlayer}
 *
 * @author Guilherme Chaguri
 */
public class AndroidPlayer extends Player implements OnInfoListener, OnCompletionListener,
        OnSeekCompleteListener, OnPreparedListener, OnBufferingUpdateListener {

    private final MediaPlayer player;
    private ProxyCache cache;

    private Callback loadCallback;

    private boolean loaded = false;
    private boolean buffering = false;
    private boolean ended = false;

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
        boolean local = Utils.isUrlLocal(data, "url");
        String url = Utils.getUrl(data, "url", local);

        if(LibHelper.PROXY_CACHE_AVAILABLE && !local) {
            ReadableMap cacheInfo = Utils.getMap(data, "cache");

            if(cacheInfo != null) {
                String id = Utils.getString(cacheInfo, "id");
                int maxFiles = Utils.getInt(cacheInfo, "maxFiles", 0);
                long maxSize = (long)(Utils.getDouble(cacheInfo, "maxSize", 0) * 1024);

                cache = new ProxyCache(context, maxFiles, maxSize);
                url = cache.getURL(url, id);
            }
        }

        buffering = true;
        loadCallback = callback;

        player.setDataSource(context, Utils.toUri(context, url, local));
        player.prepareAsync();

        updateState();
    }

    @Override
    public void reset() {
        player.reset();

        if(cache != null) {
            cache.destroy();
            cache = null;
        }

        buffering = false;
        ended = false;
        loaded = false;
        updateState();
    }

    @Override
    public void play() {
        player.start();

        buffering = false;
        ended = false;
        updateState();
    }

    @Override
    public void pause() {
        player.pause();

        updateState();
    }

    @Override
    public void stop() {
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
    public void destroy() throws Exception {
        player.release();

        if(cache != null) {
            cache.destroy();
            cache = null;
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
