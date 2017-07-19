package guichaguri.trackplayer.cast;

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
public class CastButtonManager extends SimpleViewManager<CastButton> {

    private final int SHOW_DIALOG = 1;

    @Override
    public String getName() {
        return "TrackPlayerCastButton";
    }

    @Override
    protected CastButton createViewInstance(ThemedReactContext reactContext) {
        CastButton button = new CastButton(reactContext);
        CastButtonFactory.setUpMediaRouteButton(reactContext.getApplicationContext(), button);
        return button;
    }

    @Override
    public void receiveCommand(CastButton root, int commandId, @Nullable ReadableArray args) {
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
