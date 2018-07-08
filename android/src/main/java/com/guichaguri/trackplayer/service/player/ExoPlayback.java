package com.guichaguri.trackplayer.service.player;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guichaguri
 */
public class ExoPlayback implements EventListener {

    private final Context context;
    private final MusicManager manager;
    private final ExoPlayer player;

    private DynamicConcatenatingMediaSource source;
    private List<Track> queue = Collections.synchronizedList(new ArrayList<>());

    public ExoPlayback(Context context, MusicManager manager, ExoPlayer player) {
        this.context = context;
        this.manager = manager;
        this.player = player;

        player.addListener(this);
        resetQueue();
    }

    private void resetQueue() {
        queue.clear();

        source = new DynamicConcatenatingMediaSource();
        player.prepare(source);
    }

    public List<Track> getQueue() {
        return queue;
    }

    public void add(Track track, int index, Promise promise) {
        queue.add(index, track);
        source.addMediaSource(index, track.toMediaSource(context), Utils.toRunnable(promise));
    }

    public void remove(int index, Promise promise) {
        queue.remove(index);
        source.removeMediaSource(index, Utils.toRunnable(promise));
    }

    public void move(int fromIndex, int toIndex, Promise promise) {
        queue.add(toIndex, queue.remove(fromIndex));
        source.moveMediaSource(fromIndex, toIndex, Utils.toRunnable(promise));
    }

    public Track getCurrentTrack() {
        return queue.get(player.getCurrentWindowIndex());
    }

    public void skip(String id, Promise promise) {
        for(int i = 0; i < queue.size(); i++) {
            if(id.equals(queue.get(i).id)) {
                player.seekToDefaultPosition(i);
                promise.resolve(null); // TODO check
                return;
            }
        }

        promise.reject("queue", "Could'nt find the track");
    }

    public void skipToPrevious(Promise promise) {
        int prev = player.getPreviousWindowIndex();

        if(prev == C.INDEX_UNSET) {
            promise.reject("queue", "Couldn't skip to previous");
            return;
        }

        player.seekToDefaultPosition(prev);
        promise.resolve(null); // TODO check
    }

    public void skipToNext(Promise promise) {
        int next = player.getNextWindowIndex();

        if(next == C.INDEX_UNSET) {
            promise.reject("queue", "Couldn't skip to previous");
            return;
        }

        player.seekToDefaultPosition(next);
        promise.resolve(null); // TODO check
    }

    public void play() {
        player.setPlayWhenReady(true);
    }

    public void pause() {
        player.setPlayWhenReady(false);
    }

    public void stop() {
        player.stop(false);
    }

    public void reset() {
        player.stop(true);
        resetQueue();
    }

    public void destroy() {
        player.release();
    }

    public long getPosition() {
        return player.getCurrentPosition();
    }

    public long getBufferedPosition() {
        return player.getBufferedPosition();
    }

    public float getRate() {
        return player.getPlaybackParameters().speed;
    }

    public void setRate(float rate) {
        player.setPlaybackParameters(new PlaybackParameters(rate, player.getPlaybackParameters().pitch));
    }

    public int getState() {
        switch(player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case Player.STATE_ENDED:
                return PlaybackStateCompat.STATE_STOPPED;
            case Player.STATE_IDLE:
                return PlaybackStateCompat.STATE_NONE;
            case Player.STATE_READY:
                return player.getPlayWhenReady() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        }
        return PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        // on queue changed
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // on state changed

        if(playbackState == Player.STATE_ENDED) {
            manager.onEnd(getCurrentTrack(), getPosition());
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        manager.onError("exoplayer", error.getCause().getMessage());
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        // on track changed
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {
        // on seek
    }
}
