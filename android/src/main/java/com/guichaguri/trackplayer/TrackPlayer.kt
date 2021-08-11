package com.guichaguri.trackplayer

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.guichaguri.trackplayer.module.MusicModule

/**
 * TrackPlayer
 * https://github.com/react-native-kit/react-native-track-player
 * @author Guichaguri
 */
class TrackPlayer : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(MusicModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}