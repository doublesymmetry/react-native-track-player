package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import com.facebook.react.bridge.Promise;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Temp;
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
                Playback pb = manager.getPlayback();
                if(pb == null) return;

                List<Track> list = new ArrayList<>();
                for(int i = 0; i < tracks.size(); i++) {
                    list.add(new Track(context, manager, tracks.get(i)));
                }

                pb.add(list, insertBeforeId, promise);
            }
        });
    }

    public void remove(final List<String> ids, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.remove(ids, promise);
            }
        });
    }

    public void skip(final String id, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.skip(id, promise);
            }
        });
    }

    public void skipToNext(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.skipToNext(promise);
            }
        });
    }

    public void skipToPrevious(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.skipToPrevious(promise);
            }
        });
    }

    public void reset() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.reset();
            }
        });
    }

    public void play() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.play();
            }
        });
    }

    public void pause() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.pause();
            }
        });
    }

    public void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.stop();
            }
        });
    }

    public void seekTo(final long ms) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.seekTo(ms);
            }
        });
    }

    public void setVolume(final float volume) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(pb == null) return;
                pb.setVolume(volume);
            }
        });
    }

    public void getVolume(final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;
                Utils.resolveCallback(callback, pb.getVolume());
            }
        });
    }

    public void getTrack(final String id, final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;

                for(Track track : pb.getQueue()) {
                    if(track.id.equals(id)) {
                        Utils.resolveCallback(callback, Temp.fromBundle(track.toBundle()));
                        return;
                    }
                }
                Utils.rejectCallback(callback, "track", "No track found");
            }
        });
    }

    public void getCurrentTrack(final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;

                Track track = pb.getCurrentTrack();

                if(track == null) {
                    Utils.rejectCallback(callback, "track", "No track playing");
                } else {
                    Utils.resolveCallback(callback, track.id);
                }
            }
        });
    }

    public void getDuration(final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;

                Utils.resolveCallback(callback, Utils.toSeconds(pb.getDuration()));
            }
        });
    }

    public void getPosition(final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;

                Utils.resolveCallback(callback, Utils.toSeconds(pb.getPosition()));
            }
        });
    }

    public void getBufferedPosition(final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;

                Utils.resolveCallback(callback, Utils.toSeconds(pb.getBufferedPosition()));
            }
        });
    }

    public void getState(final Promise callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Playback pb = manager.getPlayback();
                if(checkPlayback(pb, callback)) return;

                Utils.resolveCallback(callback, pb.getState());
            }
        });
    }

    public void getCastState(Promise callback) {
        Utils.resolveCallback(callback, manager.getCastState());
    }

    public void destroy() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.destroyPlayer();
            }
        });
    }

    private boolean checkPlayback(Playback pb, Promise callback) {
        if(pb == null) {
            Utils.rejectCallback(callback, "playback", "The playback is not initialized");
            return true;
        }
        return false;
    }
}
