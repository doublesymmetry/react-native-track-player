package guichaguri.trackplayer.player.components;

import android.support.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
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
        Utils.resolveCallback(callback, data);
    }

}
