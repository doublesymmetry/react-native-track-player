package guichaguri.trackplayer.remote;

import android.content.Context;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.LibHelper;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class Remote {

    private final Context context;
    private final MediaManager manager;

    private String castAppId;
    private Chromecast cast;

    public Remote(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    public void updateOptions(ReadableMap data) {
        castAppId = Utils.getString(data, "castAppId");

        if(cast != null) cast.setApplicationId(castAppId);
    }

    public void startScan(boolean active, Promise callback) {
        if(cast == null) {
            if(!LibHelper.isChromecastAvailable(context)) {
                Utils.rejectCallback(callback, "error", "Google Play Services is not available");
                return;
            }
            cast = new Chromecast(context, manager, castAppId);
        }
        cast.startScan(active);
        Utils.resolveCallback(callback);
    }

    public void stopScan() {
        if(cast != null) cast.stopScan();
    }

    public void connect(String deviceId, Promise callback) {
        if(cast == null) {
            Utils.rejectCallback(callback, "error", "Chromecast is not available");
            return;
        }
        cast.connect(deviceId, callback);
    }

}
