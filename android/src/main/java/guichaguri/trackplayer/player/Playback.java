package guichaguri.trackplayer.player;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.facebook.react.bridge.Promise;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Base player object
 *
 * @author Guilherme Chaguri
 */
public abstract class Playback {

    protected final Context context;
    protected final MediaManager manager;
    protected List<Track> queue = Collections.synchronizedList(new ArrayList<Track>());
    protected int currentTrack = -1;

    protected int prevState = PlaybackStateCompat.STATE_NONE;

    protected Playback(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    public List<Track> getQueue() {
        return queue;
    }

    public Track getCurrentTrack() {
        return currentTrack < queue.size() && currentTrack >= 0 ? queue.get(currentTrack) : null;
    }

    public void add(List<Track> tracks, String insertBeforeId, Promise callback) {
        if(insertBeforeId == null) {
            boolean empty = queue.isEmpty();
            queue.addAll(tracks);

            if(empty) updateCurrentTrack(0, null);
        } else {
            int index = queue.size();
            for(int i = 0; i < queue.size(); i++) {
                if(queue.get(i).id.equals(insertBeforeId)) break;
                index = i;
            }
            queue.addAll(index, tracks);

            if(currentTrack >= index) {
                currentTrack += tracks.size();
            }
        }

        Utils.resolveCallback(callback);
    }

    public void remove(List<String> ids, Promise callback) {
        ListIterator<Track> i = queue.listIterator();
        int currTrack = currentTrack;

        while(i.hasNext()) {
            int index = i.nextIndex();
            Track track = i.next();
            for(String id : ids) {
                if(track.id.equals(id)) {
                    i.remove();
                    if(currTrack == index) {
                        currTrack = i.nextIndex();
                    }
                    break;
                }
            }
        }

        if(currTrack != currentTrack) {
            updateCurrentTrack(currTrack, null);
        }

        Utils.resolveCallback(callback);
        manager.onQueueUpdate();
    }

    public void removeUpcomingTracks() {
        for(int i = currentTrack + 1; i < queue.size(); i++) {
            queue.remove(i);
        }

        manager.onQueueUpdate();
    }

    public void skip(String id, Promise callback) {
        for(int i = 0; i < queue.size(); i++) {
            Track track = queue.get(i);
            if(track.id.equals(id)) {
                updateCurrentTrack(i, callback);
                return;
            }
        }

        Utils.rejectCallback(callback, "skip", "The track was not found");
    }

    protected boolean hasNext() {
        return currentTrack < queue.size() - 1;
    }

    public void skipToNext(Promise callback) {
        if(hasNext()) {
            updateCurrentTrack(currentTrack + 1, callback);
        } else {
            Utils.rejectCallback(callback, "skip", "There is no next tracks");
        }
    }

    public void skipToPrevious(Promise callback) {
        if(currentTrack > 0) {
            updateCurrentTrack(currentTrack - 1, callback);
        } else {
            Utils.rejectCallback(callback, "skip", "There is no previous tracks");
        }
    }

    public abstract void load(Track track, Promise callback);

    public void reset() {
        Track prev = getCurrentTrack();
        long pos = getPosition();

        queue.clear();
        manager.onQueueUpdate();

        currentTrack = -1;
        manager.onTrackUpdate(prev, pos, null, true);
    }

    public abstract void play();

    public abstract void pause();

    public abstract void stop();

    /**
     * State from {@link android.support.v4.media.session.PlaybackStateCompat}
     */
    public abstract int getState();

    public abstract long getPosition();

    public abstract long getBufferedPosition();

    public abstract long getDuration();

    public abstract void seekTo(long ms);

    public abstract float getRate();

    public abstract void setRate(float rate);

    public abstract float getVolume();

    public abstract void setVolume(float volume);

    public abstract boolean isRemote();

    public void updateData() {
        // NOOP
    }

    public void copyPlayback(Playback playback) {
        // Copy everything to the new playback
        queue = playback.getQueue();
        currentTrack = playback.currentTrack;

        Track track = getCurrentTrack();
        if(track == null) return;

        load(track, null);
        seekTo(playback.getPosition());

        int state = playback.getState();
        if(Utils.isPlaying(state)) {
            play();
        } else if(Utils.isPaused(state)) {
            pause();
        }
    }

    public abstract void destroy();

    protected final void updateState(int state) {
        manager.onPlaybackUpdate();

        if(state == prevState) return;

        if(Utils.isPlaying(state) && !Utils.isPlaying(prevState)) {
            manager.onPlay();
        } else if(Utils.isPaused(state) && !Utils.isPaused(prevState)) {
            manager.onPause();
        } else if(Utils.isStopped(state) && !Utils.isStopped(prevState)) {
            manager.onStop();
        }

        manager.onStateChange(state);
        prevState = state;
    }

    protected void updateCurrentTrack(int track, Promise callback) {
        if(queue.isEmpty()) {
            reset();
            Utils.rejectCallback(callback, "queue", "The queue is empty");
            return;
        } else if(track >= queue.size()) {
            track = queue.size() - 1;
        } else if(track < 0) {
            track = 0;
        }

        Track previous = getCurrentTrack();
        long position = getPosition();
        int oldState = getState();

        Log.d(Utils.TAG, "Updating current track...");

        Track next = queue.get(track);
        currentTrack = track;

        load(next, callback);

        if(Utils.isPlaying(oldState)) {
            play();
        } else if(Utils.isPaused(oldState)) {
            pause();
        }

        manager.onTrackUpdate(previous, position, next, true);
    }
}
