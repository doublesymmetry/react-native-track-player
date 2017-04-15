package guichaguri.trackplayer.remote;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.Callback;
import android.support.v7.media.MediaRouter.RouteInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.CastOptions;
import com.google.android.gms.cast.Cast.Listener;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import guichaguri.trackplayer.logic.Events;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.players.CastPlayer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guilherme Chaguri
 */
public class Chromecast extends Callback implements ConnectionCallbacks,
        OnConnectionFailedListener, ResultCallback<ApplicationConnectionResult> {

    private final Context context;
    private final MediaManager manager;
    private final MediaRouter router;
    private final Map<String, CastDevice> devices = new HashMap<>();

    private MediaRouteSelector selector;
    private GoogleApiClient client;
    private String applicationId = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    private String sessionId;
    private CastPlayer activePlayer;
    private Promise connectCallback;

    private boolean scanning = false;
    private boolean reconnecting = false;
    private boolean activeScan = false;

    public Chromecast(Context context, MediaManager manager) {
        this(context, manager, null);
    }

    public Chromecast(Context context, MediaManager manager, String appId) {
        this.context = context;
        this.manager = manager;
        this.router = MediaRouter.getInstance(context);

        setApplicationId(appId);
    }

    public void setApplicationId(String appId) {
        boolean wasScaning = scanning;
        destroy();

        applicationId = appId != null ? appId : CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;

        selector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(applicationId))
                .build();

        if(wasScaning) startScan(activeScan);
    }

    public void startScan(boolean active) {
        if(scanning) return;

        int flags = activeScan ?
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN :
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY;

        router.addCallback(selector, this, flags);
        activeScan = active;
        scanning = true;
    }

    public void stopScan() {
        if(!scanning) return;
        router.removeCallback(this);
        scanning = false;
    }

    public void connect(String id, Promise callback) {
        disconnect();

        CastDevice device = devices.get(id);
        CastOptions options = new CastOptions.Builder(device, new CastCallback()).build();

        connectCallback = callback;

        client = new GoogleApiClient.Builder(context)
                .addApi(Cast.API, options)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }

    public void disconnect() {
        if(client != null && (client.isConnecting() || client.isConnected())) {
            if(sessionId != null) {
                Cast.CastApi.stopApplication(client, sessionId);
                sessionId = null;
            }
            client.disconnect();
            client = null;
        }
        if(activePlayer != null) {
            Events.dispatchEvent(context, manager.getPlayerId(activePlayer), Events.REMOTE_DISCONNECTED, null);

            manager.removePlayer(activePlayer);
            activePlayer = null;
        }
        reconnecting = false;
    }

    public void destroy() {
        stopScan();
        disconnect();
        router.selectRoute(router.getDefaultRoute());
        devices.clear();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(reconnecting) {
            reconnecting = false;
            return;
        }
        Cast.CastApi.launchApplication(client, applicationId).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        reconnecting = true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        disconnect();
    }

    @Override
    public void onResult(@NonNull ApplicationConnectionResult result) {
        if(result.getStatus().isSuccess()) {
            sessionId = result.getSessionId();

            activePlayer = new CastPlayer(context, this, manager, client);
            int id = manager.addPlayer(activePlayer);
            Utils.resolveCallback(connectCallback, id);
            Events.dispatchEvent(context, id, Events.REMOTE_CONNECTED, null);
        } else {
            disconnect();

            Utils.rejectCallback(connectCallback, "disconnect", "Could not connect to the cast device");
        }
        connectCallback = null;
    }

    @Override
    public void onRouteAdded(MediaRouter router, RouteInfo route) {
        CastDevice device = CastDevice.getFromBundle(route.getExtras());
        if(device == null) return;

        devices.put(route.getId(), device);

        WritableMap data = Arguments.createMap();
        data.putString("id", route.getId());
        data.putString("deviceId", device.getDeviceId());
        data.putString("model", device.getModelName());
        data.putString("version", device.getDeviceVersion());
        data.putString("name", device.getFriendlyName());
        data.putString("ip", device.getIpAddress().getHostAddress());
        data.putInt("port", device.getServicePort());
        data.putBoolean("audio", device.hasCapability(CastDevice.CAPABILITY_AUDIO_OUT));
        data.putBoolean("video", device.hasCapability(CastDevice.CAPABILITY_VIDEO_OUT));

        Events.dispatchEvent(context, manager.getPlayerId(activePlayer), Events.REMOTE_ADDED, data);
    }

    @Override
    public void onRouteRemoved(MediaRouter router, RouteInfo route) {
        devices.remove(route.getId());

        WritableMap data = Arguments.createMap();
        data.putString("id", route.getId());
        Events.dispatchEvent(context, manager.getPlayerId(activePlayer), Events.REMOTE_REMOVED, data);
    }

    private class CastCallback extends Listener {

        @Override
        public void onApplicationDisconnected(int status) {
            disconnect();
        }

        @Override
        public void onVolumeChanged() {
            if(activePlayer != null) activePlayer.onVolumeChanged();
        }
    }

}
