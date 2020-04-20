package com.guichaguri.trackplayer;

import androidx.annotation.NonNull;
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
    @NonNull
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Collections.singletonList(new MusicModule(reactContext));
    }

    @Override
    @NonNull
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

}
