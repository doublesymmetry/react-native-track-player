package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer.EventListener;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.TrackType;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.components.PlayerView;
import guichaguri.trackplayer.player.track.ExoTrack;
import java.io.File;
import java.io.IOException;

/**
 * @author Guilherme Chaguri
 */
public class ExoPlayer extends Player<ExoTrack> implements EventListener {

    private final SimpleExoPlayer player;

    private Callback loadCallback = null;
    private boolean playing = false;

    public ExoPlayer(Context context, MediaManager manager) {
        super(context, manager);

        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector(), new DefaultLoadControl());
        player.setAudioStreamType(C.STREAM_TYPE_MUSIC);
        player.addListener(this);
    }

    @Override
    protected ExoTrack createTrack(ReadableMap data) {
        return new ExoTrack(context, manager, data);
    }

    @Override
    public void update(ReadableMap data, Callback updateCallback) {
        updateCallback.invoke();
    }

    @Override
    public void load(ExoTrack track, Callback loadCallback) throws IOException {
        this.loadCallback = loadCallback;

        boolean local = track.url.local;
        Uri url = Utils.toUri(context, track.url.url, local);
        long cacheMaxSize = track.cache.maxSize;

        DataSource.Factory factory = new DefaultDataSourceFactory(context, track.userAgent);
        MediaSource source;

        if(cacheMaxSize > 0 && !local) {
            File cacheDir = new File(context.getCacheDir(), "TrackPlayer");
            Cache cache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(cacheMaxSize));
            factory = new CacheDataSourceFactory(cache, factory, 0, cacheMaxSize);
        }

        if(track.type == TrackType.DASH) {
            source = new DashMediaSource(url, factory, new DefaultDashChunkSource.Factory(factory), null, null);
        } else if(track.type == TrackType.HLS) {
            source = new HlsMediaSource(url, factory, null, null);
        } else if(track.type == TrackType.SMOOTH_STREAMING) {
            source = new SsMediaSource(url, factory, new DefaultSsChunkSource.Factory(factory), null, null);
        } else {
            source = new ExtractorMediaSource(url, factory, new DefaultExtractorsFactory(), null, null);
        }

        player.prepare(source);
    }

    @Override
    public void reset() {
        player.stop();
    }

    @Override
    public void play() {
        player.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public int getState() {
        return getState(player.getPlaybackState());
    }

    private int getState(int playerState) {
        switch(playerState) {
            case SimpleExoPlayer.STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case SimpleExoPlayer.STATE_ENDED:
                return PlaybackStateCompat.STATE_STOPPED;
            case SimpleExoPlayer.STATE_IDLE:
                return PlaybackStateCompat.STATE_NONE;
            case SimpleExoPlayer.STATE_READY:
                return playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        }
        return PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public long getPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getBufferedPosition() {
        return player.getBufferedPosition();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public void seekTo(long ms) {
        player.seekTo(ms);
    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    public void setVolume(float volume) {
        player.setVolume(volume);
    }

    @Override
    public void bindView(PlayerView view) {
        player.setVideoSurfaceHolder(view != null ? view.getHolder() : null);
    }

    @Override
    public void destroy() throws Exception {
        player.release();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean loading) {
        updateState(loading ? PlaybackStateCompat.STATE_BUFFERING : getState());
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        playing = playWhenReady;
        updateState(getState(playbackState));

        if(playbackState == SimpleExoPlayer.STATE_READY && loadCallback != null) {
            loadCallback.invoke();
            loadCallback = null;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        // TODO
    }

    @Override
    public void onPositionDiscontinuity() {

    }

}
