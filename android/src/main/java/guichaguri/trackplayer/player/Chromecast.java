package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.Callback;
import android.support.v7.media.MediaRouter.RouteInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
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
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.players.CastPlayer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guilherme Chaguri
 */
public class Chromecast extends Callback implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<ApplicationConnectionResult> {

    private final Context context;
    private final MediaManager manager;
    private final MediaRouter router;
    private final Map<String, CastDevice> devices = new HashMap<>();

    private MediaRouteSelector selector;
    private GoogleApiClient client;
    private String applicationId;
    private String sessionId;
    private CastPlayer activePlayer;

    private boolean scanning = false;
    private boolean reconnecting = false;

    public Chromecast(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
        this.router = MediaRouter.getInstance(context);
    }

    private void init(ReadableMap cast) {
        destroy();

        applicationId = Utils.getString(cast, "id", CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID);

        selector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(applicationId))
                .build();
    }

    public void startScan() {
        if(scanning) return;
        router.addCallback(selector, this, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        scanning = true;
    }

    public void stopScan() {
        if(!scanning) return;
        router.removeCallback(this);
        scanning = false;
    }

    public CastPlayer connect(String id) {
        CastDevice device = devices.get(id);
        CastOptions options = new CastOptions.Builder(device, new CastCallback()).build();

        client = new GoogleApiClient.Builder(context)
                .addApi(Cast.API, options)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();

        activePlayer = new CastPlayer(context, this, manager, client);
        return activePlayer;
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
        activePlayer = null;
        reconnecting = false;
    }

    public void destroy() {
        stopScan();
        disconnect();
        router.selectRoute(router.getDefaultRoute());
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
        } else {
            disconnect();
        }
    }

    @Override
    public void onRouteAdded(MediaRouter router, RouteInfo route) {
        devices.put(route.getId(), CastDevice.getFromBundle(route.getExtras()));
        updateRoutes();
    }

    @Override
    public void onRouteRemoved(MediaRouter router, RouteInfo route) {
        devices.remove(route.getId());
        updateRoutes();
    }

    private void updateRoutes() {
        WritableArray array = Arguments.createArray();

        for(String id : devices.keySet()) {
            CastDevice device = devices.get(id);
            WritableMap data = Arguments.createMap();
            data.putString("id", id);
            data.putString("deviceId", device.getDeviceId());
            data.putString("name", device.getFriendlyName());
            data.putString("ip", device.getIpAddress().getHostAddress());
            data.putInt("port", device.getServicePort());
            array.pushMap(data);
        }

        WritableMap map = Arguments.createMap();
        map.putArray("devices", array);
        Utils.dispatchEvent(context, manager.getPlayerId(activePlayer), "cast-devices", map);
    }

    private class CastCallback extends Listener {

        @Override
        public void onApplicationDisconnected(int status) {
            Utils.dispatchEvent(context, manager.getPlayerId(activePlayer), "cast-disconnect", null);
            disconnect();
        }

        @Override
        public void onVolumeChanged() {
            activePlayer.onVolumeChanged();
        }
    }

}
