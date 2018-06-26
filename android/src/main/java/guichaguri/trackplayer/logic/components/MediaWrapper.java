package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.player.Playback;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class MediaWrapper extends Binder {

    private final Context context;
    private final MediaManager manager;
    private final Handler handler;

    public MediaWrapper(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
        this.handler = new Handler();
    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void setupPlayer(final Bundle options, final Promise promise) {
        manager.setupPlayer(options);
        Utils.resolveCallback(promise);
    }

    public void updateOptions(final Bundle bundle) {
        manager.updateOptions(bundle);
    }

    public void add(final List<Bundle> tracks, final String insertBeforeId, final Promise promise) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        List<Track> list = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++) {
            list.add(new Track(context, manager, tracks.get(i)));
        }

        pb.add(list, insertBeforeId, promise);
    }

    public void remove(final List<String> ids, final Promise promise) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.remove(ids, promise);
    }

    public void removeUpcomingTracks() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.removeUpcomingTracks();
    }

    public void skip(final String id, final Promise promise) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.skip(id, promise);
    }

    public void skipToNext(final Promise promise) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.skipToNext(promise);
    }

    public void skipToPrevious(final Promise promise) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.skipToPrevious(promise);
    }

    public void reset() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.reset();
    }

    public void play() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.play();
    }

    public void pause() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.pause();
    }

    public void stop() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.stop();
    }

    public void seekTo(final long ms) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.seekTo(ms);
    }

    public void setVolume(final float volume) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.setVolume(volume);
    }

    public void getVolume(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;
        Utils.resolveCallback(callback, pb.getVolume());
    }

    public void setRate(final float rate) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;
        pb.setRate(rate);
    }

    public void getRate(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;
        Utils.resolveCallback(callback, pb.getRate());
    }

    public void getTrack(final String id, final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        for(Track track : pb.getQueue()) {
            if(track.id.equals(id)) {
                Utils.resolveCallback(callback, Arguments.fromBundle(track.originalItem));
                return;
            }
        }
        Utils.rejectCallback(callback, "track", "No track found");
    }

    public void getQueue(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        List queue = new ArrayList();
        for (Track track : pb.getQueue()) {
            queue.add(track.originalItem);
        }

        Utils.resolveCallback(callback, Arguments.fromList(queue));
    }

    public void getCurrentTrack(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        Track track = pb.getCurrentTrack();

        if(track == null) {
            Utils.resolveCallback(callback, null);
        } else {
            Utils.resolveCallback(callback, track.id);
        }
    }

    public void getDuration(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        Utils.resolveCallback(callback, Utils.toSeconds(pb.getDuration()));
    }

    public void getPosition(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        Utils.resolveCallback(callback, Utils.toSeconds(pb.getPosition()));
    }

    public void getBufferedPosition(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        Utils.resolveCallback(callback, Utils.toSeconds(pb.getBufferedPosition()));
    }

    public void getState(final Promise callback) {
        Playback pb = manager.getPlayback();
        if(checkPlayback(pb, callback)) return;

        Utils.resolveCallback(callback, pb.getState());
    }

    public void destroy() {
        manager.destroyPlayer();
    }

    private boolean checkPlayback(Playback pb, Promise callback) {
        if(pb == null) {
            Utils.rejectCallback(callback, "playback", "The playback is not initialized");
            return true;
        }
        return false;
    }
}
