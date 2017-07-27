package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Temp;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
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

    public void setupPlayer(final Bundle options, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.setupPlayer(options, promise);
            }
        });
    }

    public void updateOptions(final Bundle bundle) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.updateOptions(bundle);
            }
        });
    }

    public void add(final List<Bundle> tracks, final String insertBeforeId, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<Track> list = new ArrayList<>();
                for(int i = 0; i < tracks.size(); i++) {
                    list.add(new Track(context, manager, tracks.get(i)));
                }
                manager.getPlayback().add(list, insertBeforeId, promise);
            }
        });
    }

    public void remove(final List<String> ids, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().remove(ids, promise);
            }
        });
    }

    public void skip(final String id, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().skip(id, promise);
            }
        });
    }

    public void skipToNext(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().skipToNext(promise);
            }
        });
    }

    public void skipToPrevious(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().skipToPrevious(promise);
            }
        });
    }

    public void reset() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().reset();
            }
        });
    }

    public void play() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().play();
            }
        });
    }

    public void pause() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().pause();
            }
        });
    }

    public void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().stop();
            }
        });
    }

    public void seekTo(final long ms) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().seekTo(ms);
            }
        });
    }

    public void setVolume(final float volume) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.getPlayback().setVolume(volume);
            }
        });
    }

    public void getVolume(final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.triggerCallback(callback, manager.getPlayback().getVolume());
            }
        });
    }

    public void getTrack(final String id, final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(Track track : manager.getPlayback().getQueue()) {
                    if(track.id.equals(id)) {
                        Utils.triggerCallback(callback, Temp.fromBundle(track.toBundle()));
                        return;
                    }
                }
                Utils.triggerCallback(callback);
            }
        });
    }

    public void getCurrentTrack(final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Track track = manager.getPlayback().getCurrentTrack();
                Utils.triggerCallback(callback, track != null ? track.id : null);
            }
        });
    }

    public void getDuration(final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.triggerCallback(callback, Utils.toSeconds(manager.getPlayback().getDuration()));
            }
        });
    }

    public void getPosition(final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.triggerCallback(callback, Utils.toSeconds(manager.getPlayback().getPosition()));
            }
        });
    }

    public void getBufferedPosition(final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.triggerCallback(callback, Utils.toSeconds(manager.getPlayback().getBufferedPosition()));
            }
        });
    }

    public void getState(final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.triggerCallback(callback, manager.getPlayback().getState());
            }
        });
    }

    public void getCastState(Callback callback) {
        Utils.triggerCallback(callback, manager.getCastState());
    }

    public void destroy() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.destroyPlayer();
            }
        });
    }
}
