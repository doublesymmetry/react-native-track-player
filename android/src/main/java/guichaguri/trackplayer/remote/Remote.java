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
        // Updates the Chromecast application id
        castAppId = Utils.getString(data, "castAppId");

        if(cast != null) cast.setApplicationId(castAppId);
    }

    public boolean isScanning() {
        return cast != null && cast.isScanning();
    }

    public void startScan(boolean active, Promise callback) {
        if(cast == null) {

            // Reject when Chromecast support is not available
            if(!LibHelper.isChromecastAvailable(context)) {
                Utils.rejectCallback(callback, "GMS", "Google Play Services is not available");
                return;
            }

            // Create a new Chromecast manager
            cast = new Chromecast(context, manager, castAppId);
        }

        // Start scanning
        cast.startScan(active);
        manager.onScanningStart();
        Utils.resolveCallback(callback);
    }

    public void stopScan() {
        // Stop scanning if Chromecast is available
        if(cast != null) {
            cast.stopScan();
            manager.onScanningStop();
        }
    }

    public void connect(String deviceId, Promise callback) {
        if(cast == null) {
            // Reject when Chromecast is not available
            Utils.rejectCallback(callback, "CAST", "Chromecast is not available");
            return;
        }

        // Connect to the device
        cast.connect(deviceId, callback);
    }

}
