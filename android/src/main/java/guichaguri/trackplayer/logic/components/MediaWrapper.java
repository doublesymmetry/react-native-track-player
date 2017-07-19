package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.track.Track;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class MediaWrapper extends Binder {

    private final Context context;
    private final MediaManager manager;

    public MediaWrapper(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    public void setupPlayer(Bundle options) {
        manager.setupPlayer(options);
    }

    public void updateOptions(Bundle bundle) {
        manager.updateOptions(bundle);
    }

    public void add(List<Bundle> tracks, String insertBeforeId) {
        List<Track> list = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++) {
            list.add(new Track(context, manager, tracks.get(i)));
        }
        manager.getPlayback().add(list, insertBeforeId);
    }

    public void remove(List<String> ids) {
        manager.getPlayback().remove(ids);
    }

    public void skip(String id) {
        manager.getPlayback().skip(id);
    }

    public void skipToNext() {
        manager.getPlayback().skipToNext();
    }

    public void skipToPrevious() {
        manager.getPlayback().skipToPrevious();
    }

    public void load() {
        manager.getPlayback().load();
    }

    public void reset() {
        manager.getPlayback().reset();
    }

    public void play() {
        manager.getPlayback().play();
    }

    public void pause() {
        manager.getPlayback().pause();
    }

    public void stop() {
        manager.getPlayback().stop();
    }

    public void seekTo(long ms) {
        manager.getPlayback().seekTo(ms);
    }

    public void setVolume(float volume) {
        manager.getPlayback().setVolume(volume);
    }

    public float getVolume() {
        return manager.getPlayback().getVolume();
    }

    public Bundle getTrack(String id) {
        for(Track track : manager.getPlayback().getQueue()) {
            if(track.id.equals(id)) return track.toBundle();
        }
        return null;
    }

    public String getCurrentTrack() {
        return manager.getPlayback().getCurrentTrack().id;
    }

    public long getDuration() {
        return manager.getPlayback().getDuration();
    }

    public long getPosition() {
        return manager.getPlayback().getPosition();
    }

    public long getBufferedPosition() {
        return manager.getPlayback().getBufferedPosition();
    }

    public int getState() {
        return manager.getPlayback().getState();
    }

    public void destroy() {
        manager.destroyPlayer();
    }
}
