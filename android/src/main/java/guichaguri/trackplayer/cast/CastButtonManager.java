package guichaguri.trackplayer.cast;

import android.support.v7.app.MediaRouteButton;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.gms.cast.framework.CastButtonFactory;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Guilherme Chaguri
 */
public class CastButtonManager extends SimpleViewManager<MediaRouteButton> {

    private final int SHOW_DIALOG = 1;

    @Override
    public String getName() {
        return "TrackPlayerCastButton";
    }

    @Override
    protected MediaRouteButton createViewInstance(ThemedReactContext reactContext) {
        MediaRouteButton button = new MediaRouteButton(reactContext);
        CastButtonFactory.setUpMediaRouteButton(reactContext, button);
        return button;
    }

    @Override
    public void receiveCommand(MediaRouteButton root, int commandId, @Nullable ReadableArray args) {
        if(commandId == SHOW_DIALOG) {
            root.showDialog();
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("showDialog", SHOW_DIALOG);
    }
}
