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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.components.PlayerView;
import java.io.IOException;

/**
 * @author Guilherme Chaguri
 */
public class ExoPlayer extends Player implements EventListener {

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
    public void update(ReadableMap data, Callback updateCallback) {

        updateCallback.invoke();
    }

    @Override
    public void load(ReadableMap data, Callback loadCallback) throws IOException {
        Uri url = Utils.getUri(context, data, "url");
        String type = Utils.getString(data, "type", "default").toLowerCase();
        String useragent = Utils.getString(data, "useragent", Util.getUserAgent(context, "react-native-track-player"));

        DefaultDataSourceFactory factory = new DefaultDataSourceFactory(context, useragent);
        MediaSource source;

        if(type.equals("dash")) {
            source = new DashMediaSource(url, factory, new DefaultDashChunkSource.Factory(factory), null, null);
        } else if(type.equals("hls")) {
            source = new HlsMediaSource(url, factory, null, null);
        } else if(type.equals("smoothstreaming")) {
            source = new SsMediaSource(url, factory, new DefaultSsChunkSource.Factory(factory), null, null);
        } else {
            source = new ExtractorMediaSource(url, factory, new DefaultExtractorsFactory(), null, null);
        }

        this.loadCallback = loadCallback;

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
    public void destroy() {
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
