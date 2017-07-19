package guichaguri.trackplayer;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import guichaguri.trackplayer.cast.CastButtonManager;
import guichaguri.trackplayer.logic.LibHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class TrackPlayer implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext context) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new TrackModule(context));
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext context) {
        List<ViewManager> views = new ArrayList<>();
        if(LibHelper.isChromecastAvailable(context)) {
            views.add(new CastButtonManager());
        }
        return views;
    }
}
