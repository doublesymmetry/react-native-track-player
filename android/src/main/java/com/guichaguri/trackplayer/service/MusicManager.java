package com.guichaguri.trackplayer.service;

import android.os.Bundle;
import android.util.Log;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.metadata.MetadataManager;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

/**
 * @author Guichaguri
 */
public class MusicManager {

    private final MusicService service;

    private MetadataManager metadata;
    private ExoPlayback playback;

    public MusicManager(MusicService service) {
        this.service = service;
    }

    public ExoPlayback getPlayback() {
        return playback;
    }

    public void onTrackUpdate(Track previous, long prevPos, Track next) {
        Log.d(Utils.LOG, "onTrackUpdate");

        metadata.updateMetadata(next);

        Bundle bundle = new Bundle();
        bundle.putString("track", previous != null ? previous.id : null);
        bundle.putDouble("position", Utils.toSeconds(prevPos));
        bundle.putString("nextTrack", next != null ? next.id : null);
        service.emit(MusicEvents.PLAYBACK_TRACK_CHANGED, bundle);
    }

    public void onEnd(Track previous, long prevPos) {
        Log.d(Utils.LOG, "onEnd");

        Bundle bundle = new Bundle();
        bundle.putString("track", previous != null ? previous.id : null);
        bundle.putDouble("position", Utils.toSeconds(prevPos));
        service.emit(MusicEvents.PLAYBACK_QUEUE_ENDED, bundle);
    }

    public void onError(String code, String error) {
        Bundle bundle = new Bundle();
        bundle.putString("code", code);
        bundle.putString("message", error);
        service.emit(MusicEvents.PLAYBACK_ERROR, bundle);
    }

    public void destroy() {
        playback.destroy();
        metadata.destroy();
    }
}
