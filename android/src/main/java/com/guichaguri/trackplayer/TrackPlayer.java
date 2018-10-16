package com.guichaguri.trackplayer;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.guichaguri.trackplayer.module.MusicModule;
import java.util.Collections;
import java.util.List;

/**
 * TrackPlayer
 * https://github.com/react-native-kit/react-native-track-player
 * @author Guichaguri
 */
public class TrackPlayer implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.singletonList(new MusicModule(reactContext));
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

}
