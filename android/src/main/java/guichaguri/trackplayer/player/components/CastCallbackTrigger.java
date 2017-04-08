package guichaguri.trackplayer.player.components;

import android.support.annotation.NonNull;
import com.facebook.react.bridge.Callback;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class CastCallbackTrigger implements ResultCallback<MediaChannelResult> {

    private final Callback callback;
    private final Object[] data;

    public CastCallbackTrigger(Callback callback, Object ... data) {
        this.callback = callback;
        this.data = data;
    }

    @Override
    public void onResult(@NonNull MediaChannelResult result) {
        Utils.triggerCallback(callback, data);
    }

}
