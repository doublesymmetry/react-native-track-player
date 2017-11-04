package guichaguri.trackplayer.cast;

import android.app.MediaRouteButton;
import android.graphics.Color;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.yoga.YogaMeasureFunction;
import com.facebook.yoga.YogaMeasureMode;
import com.facebook.yoga.YogaMeasureOutput;
import com.facebook.yoga.YogaNode;
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
    public LayoutShadowNode createShadowNodeInstance() {
        return new CastButtonShadowNode();
    }

    @Override
    public Class getShadowNodeClass() {
        return CastButtonShadowNode.class;
    }

    @ReactProp(name = ViewProps.COLOR, defaultDouble = Color.TRANSPARENT)
    public void setColor(CastButton view, double color) {
        view.setColor((int)color);
    }

    @Override
    public void receiveCommand(CastButton view, int commandId, @Nullable ReadableArray args) {
        if(commandId == SHOW_DIALOG) {
            view.showDialog();
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("showDialog", SHOW_DIALOG);
    }


    class CastButtonShadowNode extends LayoutShadowNode implements YogaMeasureFunction {
        private int buttonWidth, buttonHeight;
        private boolean measured = false;

        public CastButtonShadowNode() {
            setMeasureFunction(this);
        }

        @Override
        public long measure(YogaNode node, float width, YogaMeasureMode widthMode, float height, YogaMeasureMode heightMode) {
            if(!measured) {
                int spec = View.MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED);
                MediaRouteButton button = new MediaRouteButton(getThemedContext());
                button.measure(spec, spec);
                buttonWidth = button.getMeasuredWidth();
                buttonHeight = button.getMeasuredHeight();
                measured = true;
            }

            return YogaMeasureOutput.make(buttonWidth, buttonHeight);
        }
    }
}
