package com.doublesymmetry.trackplayer

import com.doublesymmetry.trackplayer.module.MusicModule
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * TrackPlayer
 * https://github.com/react-native-kit/react-native-track-player
 * @author Milen Pivchev @mpivchev
 */
class TrackPlayer : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(MusicModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}