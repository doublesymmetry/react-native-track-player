package guichaguri.trackplayer.player.components;

import android.support.annotation.NonNull;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.cast.framework.media.RemoteMediaClient.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import guichaguri.trackplayer.logic.Utils;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guilherme Chaguri
 */
public class CastCallbackTrigger implements ResultCallback<MediaChannelResult> {

    private final Promise callback;

    public CastCallbackTrigger(Promise callback) {
        this.callback = callback;
    }

    @Override
    public void onResult(@NonNull MediaChannelResult result) {
        Status status = result.getStatus();
        if(status.isSuccess()) {
            JSONObject obj = result.getCustomData();
            if(obj != null) {
                try {
                    Utils.resolveCallback(callback, toMap(obj));
                    return;
                } catch(JSONException ex) {}
            }
            Utils.resolveCallback(callback);
        } else {
            Utils.rejectCallback(callback, Integer.toString(status.getStatusCode()), status.getStatusMessage());
        }
    }

    private WritableMap toMap(JSONObject obj) throws JSONException {
        WritableMap map = Arguments.createMap();

        Iterator<String> i = obj.keys();
        while(i.hasNext()) {
            String key = i.next();

            if(map.isNull(key)) {
                map.putNull(key);
                continue;
            }

            Object val = obj.get(key);

            if(val instanceof Boolean) {
                map.putBoolean(key, (Boolean)val);
            } else if(val instanceof Number) {
                map.putDouble(key, ((Number)val).doubleValue());
            } else if(val instanceof String) {
                map.putString(key, (String)val);
            } else if(val instanceof JSONObject) {
                map.putMap(key, toMap((JSONObject)val));
            } else if(val instanceof JSONArray) {
                map.putArray(key, toArray((JSONArray)val));
            }
        }

        return map;
    }

    private WritableArray toArray(JSONArray arr) throws JSONException {
        WritableArray array = Arguments.createArray();

        for(int i = 0; i < arr.length(); i++) {
            if(arr.isNull(i)) {
                array.pushNull();
                continue;
            }

            Object val = arr.get(i);

            if(val instanceof Boolean) {
                array.pushBoolean((Boolean)val);
            } else if(val instanceof Number) {
                array.pushDouble(((Number)val).doubleValue());
            } else if(val instanceof String) {
                array.pushString((String)val);
            } else if(val instanceof JSONObject) {
                array.pushMap(toMap((JSONObject)val));
            } else if(val instanceof JSONArray) {
                array.pushArray(toArray((JSONArray)val));
            }
        }

        return array;
    }

}
