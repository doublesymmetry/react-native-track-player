package guichaguri.trackplayer.player.components;

import android.support.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.google.android.gms.cast.framework.media.RemoteMediaClient.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class CastCallbackTrigger implements ResultCallback<MediaChannelResult> {

    private final Promise callback;
    private final Object[] data;

    public CastCallbackTrigger(Promise callback, Object ... data) {
        this.callback = callback;
        this.data = data;
    }

    @Override
    public void onResult(@NonNull MediaChannelResult result) {
        Status status = result.getStatus();
        if(status.isSuccess()) {
            Utils.resolveCallback(callback, data);
        } else {
            Utils.rejectCallback(callback, Integer.toString(status.getStatusCode()), status.getStatusMessage());
        }
    }

}
