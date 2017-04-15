package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.SystemClock;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Guilherme Chaguri
 */
public abstract class Player<T extends Track> {

    protected final Context context;
    protected final MediaManager manager;
    protected LinkedList<T> queue = new LinkedList<>();
    protected int currentTrack = 0;

    private int prevState = 0;

    protected Player(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    protected abstract T createTrack(ReadableMap data);

    protected abstract T createTrack(Track track);

    public T getCurrentTrack() {
        return currentTrack < queue.size() && currentTrack >= 0 ? queue.get(currentTrack) : null;
    }

    public List<T> getQueue() {
        return queue;
    }

    public <F extends Track> void copyQueue(Player<F> to, String insertBeforeId, Promise promise) {
        List<F> tracks = new ArrayList<>();
        for(T track : getQueue()) {
            tracks.add(to.createTrack(track));
        }
        to.add(insertBeforeId, tracks, promise);
    }

    public void add(String insertBeforeId, List<T> tracks, Promise callback) {
        if(insertBeforeId == null) {
            queue.addAll(tracks);
        } else {
            int index = 0;
            for(int i = 0; i < queue.size(); i++) {
                if(queue.get(i).id.equals(insertBeforeId)) break;
                index = i;
            }
            queue.addAll(index, tracks);
        }
        Utils.resolveCallback(callback);
    }

    public void add(String insertBeforeId, ReadableArray tracks, Promise callback) {
        List<T> list = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++) {
            list.add(createTrack(tracks.getMap(i)));
        }
        add(insertBeforeId, list, callback);
    }

    public void remove(String[] ids, Promise callback) {
        ListIterator<T> i = queue.listIterator();
        boolean trackChanged = false;

        while(i.hasNext()) {
            int index = i.nextIndex();
            T track = i.next();
            for(String id : ids) {
                if(track.id.equals(id)) {
                    i.remove();
                    if(currentTrack == index) {
                        currentTrack = i.nextIndex();
                        trackChanged = true;
                    }
                    break;
                }
            }
        }

        if(trackChanged) {
            updateCurrentTrack(callback);
        } else {
            Utils.resolveCallback(callback);
        }
    }

    public void skip(String id, Promise callback) {
        for(int i = 0; i < queue.size(); i++) {
            T track = queue.get(i);
            if(track.id.equals(id)) {
                currentTrack = i;
                updateCurrentTrack(callback);
                return;
            }
        }

        Utils.rejectCallback(callback, "skip", "The track was not found");
    }

    public void skipToNext(Promise callback) {
        if(currentTrack < queue.size() - 1) {
            currentTrack++;
            updateCurrentTrack(callback);
        } else {
            Utils.rejectCallback(callback, "skip", "There is no next tracks");
        }
    }

    public void skipToPrevious(Promise callback) {
        if(currentTrack > 0) {
            currentTrack--;
            updateCurrentTrack(callback);
        } else {
            Utils.rejectCallback(callback, "skip", "There is no previous tracks");
        }
    }

    public abstract void load(T track, Promise callback);

    public void load(ReadableMap data, Promise callback) {
        load(createTrack(data), callback);
    }

    public void reset() {
        queue.clear();
    }

    public abstract void play();

    public abstract void pause();

    public abstract void stop();

    /**
     * State from {@link android.support.v4.media.session.PlaybackStateCompat}
     */
    public abstract int getState();

    public abstract long getPosition();

    public long getPositionUpdateTime() {
        return SystemClock.elapsedRealtime();
    }

    public abstract long getBufferedPosition();

    public abstract long getDuration();

    public abstract void seekTo(long ms);

    public abstract float getSpeed();

    public abstract void setVolume(float volume);

    public abstract void destroy();

    protected final void updateState(int state) {
        updateMetadata();

        if(Utils.isPlaying(state) && !Utils.isPlaying(prevState)) {
            manager.onPlay(this);
        } else if(Utils.isPaused(state) && !Utils.isPaused(prevState)) {
            manager.onPause(this);
        } else if(Utils.isStopped(state) && !Utils.isStopped(prevState)) {
            manager.onStop(this);
        }

        prevState = state;
    }

    protected final void updateMetadata() {
        manager.onUpdate(this);
    }

    protected void updateCurrentTrack(Promise callback) {
        int state = getState();
        T track = queue.get(currentTrack);

        load(track, callback);

        if(Utils.isPlaying(state)) {
            play();
        } else if(Utils.isPaused(state)) {
            pause();
        }

        updateMetadata();
    }
}
