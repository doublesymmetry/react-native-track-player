package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.SystemClock;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import java.io.IOException;
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

    public T getCurrentTrack() {
        return queue.get(currentTrack);
    }

    public List<T> getQueue() {
        return queue;
    }

    public void add(int index, T track, Callback callback) throws Exception {
        if(index < 0) {
            queue.add(track);
        } else {
            queue.add(index, track);
        }
        Utils.triggerCallback(callback);
    }

    public  void add(int index, ReadableMap data, Callback callback) throws Exception {
        add(index, createTrack(data), callback);
    }

    public void remove(String[] ids, Callback callback) throws Exception {
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
            Utils.triggerCallback(callback);
        }
    }

    public void skip(String id, Callback callback) throws Exception {
        for(int i = 0; i < queue.size(); i++) {
            T track = queue.get(i);
            if(track.id.equals(id)) {
                currentTrack = i;
                updateCurrentTrack(callback);
                return;
            }
        }

        Utils.triggerCallback(callback);
    }

    public void skipToNext(Callback callback) throws Exception {
        if(currentTrack < queue.size() - 1) {
            currentTrack++;
            updateCurrentTrack(callback);
        } else {
            Utils.triggerCallback(callback);
        }
    }

    public void skipToPrevious(Callback callback) throws Exception {
        if(currentTrack > 0) {
            currentTrack--;
            updateCurrentTrack(callback);
        } else {
            Utils.triggerCallback(callback);
        }
    }

    public abstract void load(T track, Callback callback) throws IOException;

    public void load(ReadableMap data, Callback callback) throws IOException {
        load(createTrack(data), callback);
    }

    public abstract void reset();

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

    protected void updateCurrentTrack(Callback callback) throws Exception {
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
