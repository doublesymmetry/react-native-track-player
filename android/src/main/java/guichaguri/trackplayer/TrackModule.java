package guichaguri.trackplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.cast.framework.CastState;
import guichaguri.trackplayer.logic.LibHelper;
import guichaguri.trackplayer.logic.Temp;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import guichaguri.trackplayer.logic.services.PlayerService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Guilherme Chaguri
 */
public class TrackModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private MediaWrapper binder;
    private boolean connecting = false;
    private ArrayDeque<Runnable> initCallbacks = new ArrayDeque<>();

    public TrackModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "TrackPlayerModule";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();

        // Unbinds the service
        if(binder != null || connecting) {
            getReactApplicationContext().unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MediaWrapper)service;
        connecting = false;

        // Triggers all callbacks
        while(!initCallbacks.isEmpty()) {
            initCallbacks.remove().run();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
        connecting = false;
    }

    private void waitForConnection(Runnable r) {
        // TODO find a "fancier" way to do this, removing the amount of boilerplate code for the runnable classes

        if(binder != null) {
            r.run();
        } else {
            initCallbacks.add(r);
        }

        if(connecting) return;

        ReactApplicationContext context = getReactApplicationContext();

        // Binds the service to get a MediaWrapper instance
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_CONNECT);
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);

        connecting = true;
    }

    /* ****************************** API ****************************** */

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();

        // Capabilities
        constants.put("CAPABILITY_PLAY", PlaybackStateCompat.ACTION_PLAY);
        constants.put("CAPABILITY_PLAY_FROM_ID", PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
        constants.put("CAPABILITY_PLAY_FROM_SEARCH", PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH);
        constants.put("CAPABILITY_PAUSE", PlaybackStateCompat.ACTION_PAUSE);
        constants.put("CAPABILITY_STOP", PlaybackStateCompat.ACTION_STOP);
        constants.put("CAPABILITY_SEEK_TO", PlaybackStateCompat.ACTION_SEEK_TO);
        constants.put("CAPABILITY_SKIP", PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM);
        constants.put("CAPABILITY_SKIP_TO_NEXT", PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        constants.put("CAPABILITY_SKIP_TO_PREVIOUS", PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        constants.put("CAPABILITY_SET_RATING", PlaybackStateCompat.ACTION_SET_RATING);

        // States
        constants.put("STATE_NONE", PlaybackStateCompat.STATE_NONE);
        constants.put("STATE_PLAYING", PlaybackStateCompat.STATE_PLAYING);
        constants.put("STATE_PAUSED", PlaybackStateCompat.STATE_PAUSED);
        constants.put("STATE_STOPPED", PlaybackStateCompat.STATE_STOPPED);
        constants.put("STATE_BUFFERING", PlaybackStateCompat.STATE_BUFFERING);

        // Rating Types
        constants.put("RATING_HEART", RatingCompat.RATING_HEART);
        constants.put("RATING_THUMBS_UP_DOWN", RatingCompat.RATING_THUMB_UP_DOWN);
        constants.put("RATING_3_STARS", RatingCompat.RATING_3_STARS);
        constants.put("RATING_4_STARS", RatingCompat.RATING_4_STARS);
        constants.put("RATING_5_STARS", RatingCompat.RATING_5_STARS);
        constants.put("RATING_PERCENTAGE", RatingCompat.RATING_PERCENTAGE);

        // Cast States
        constants.put("CAST_NO_DEVICES_AVAILABLE", CastState.NO_DEVICES_AVAILABLE);
        constants.put("CAST_NOT_CONNECTED", CastState.NOT_CONNECTED);
        constants.put("CAST_CONNECTING", CastState.CONNECTING);
        constants.put("CAST_CONNECTED", CastState.CONNECTED);

        // Not actual an API constant
        // Only used internally
        constants.put("CAST_SUPPORT_AVAILABLE", LibHelper.isChromecastAvailable(getReactApplicationContext()));

        return constants;
    }

    @ReactMethod
    public void setupPlayer(ReadableMap data, final Promise promise) {
        if(binder != null) {
            // The module is already connected to the service
            Utils.resolveCallback(promise);
            return;
        }

        initCallbacks.add(new Runnable() {
            @Override
            public void run() {
                Utils.resolveCallback(promise);
            }
        });

        if(connecting) return;

        ReactApplicationContext context = getReactApplicationContext();

        // Binds the service to get a MediaWrapper instance
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_SETUP_PLAYER);
        intent.putExtra("data", Temp.toBundle(data));
        context.bindService(intent, this, Service.BIND_AUTO_CREATE);

        connecting = true;
    }

    @ReactMethod
    public void destroy() {
        if(binder != null) binder.destroy();
        getReactApplicationContext().unbindService(this);
    }

    @ReactMethod
    public void updateOptions(ReadableMap data) {
        final Bundle options = Temp.toBundle(data);

        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.updateOptions(options);
            }
        });
    }

    @ReactMethod
    public void add(ReadableArray tracks, final String insertBeforeId, final Promise callback) {
        final ArrayList trackList = Temp.toList(tracks);

        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.add(trackList, insertBeforeId, callback);
            }
        });
    }

    @ReactMethod
    public void remove(ReadableArray tracks, final Promise callback) {
        final ArrayList trackList = Temp.toList(tracks);

        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.remove(trackList, callback);
            }
        });
    }

    @ReactMethod
    public void skip(final String track, final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.skip(track, callback);
            }
        });
    }

    @ReactMethod
    public void skipToNext(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.skipToNext(callback);
            }
        });
    }

    @ReactMethod
    public void skipToPrevious(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.skipToPrevious(callback);
            }
        });
    }

    @ReactMethod
    public void reset() {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.reset();
            }
        });
    }

    @ReactMethod
    public void play() {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.play();
            }
        });
    }

    @ReactMethod
    public void pause() {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.pause();
            }
        });
    }

    @ReactMethod
    public void stop() {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.stop();
            }
        });
    }

    @ReactMethod
    public void seekTo(final double seconds) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.seekTo(Utils.toMillis(seconds));
            }
        });
    }

    @ReactMethod
    public void setVolume(final float volume) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.setVolume(volume);
            }
        });
    }

    @ReactMethod
    public void getVolume(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getVolume(callback);
            }
        });
    }

    @ReactMethod
    public void getTrack(final String id, final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getTrack(id, callback);
            }
        });
    }

    @ReactMethod
    public void getCurrentTrack(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getCurrentTrack(callback);
            }
        });
    }

    @ReactMethod
    public void getDuration(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getDuration(callback);
            }
        });
    }

    @ReactMethod
    public void getBufferedPosition(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getBufferedPosition(callback);
            }
        });
    }

    @ReactMethod
    public void getPosition(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getPosition(callback);
            }
        });
    }

    @ReactMethod
    public void getState(final Promise callback) {
        waitForConnection(new Runnable() {
            @Override
            public void run() {
                binder.getState(callback);
            }
        });
    }

    @ReactMethod
    public void getCastState(Promise callback) {
        Context context = getReactApplicationContext().getApplicationContext();

        if(!LibHelper.isChromecastAvailable(context) || binder == null) {
            Utils.resolveCallback(callback, CastState.NO_DEVICES_AVAILABLE);
            return;
        }

        binder.getCastState(callback);

        // Use the code below when React Native updates the support library
        // and we'll be able to update the Cast SDK too
        /*CastContext cast = CastContext.getSharedInstance(context);
        Utils.triggerCallback(callback, cast.getCastState());*/
    }
}
