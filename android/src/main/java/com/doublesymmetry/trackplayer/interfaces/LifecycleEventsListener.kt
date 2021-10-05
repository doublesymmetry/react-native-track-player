package com.doublesymmetry.trackplayer.interfaces

import com.facebook.react.bridge.LifecycleEventListener

interface LifecycleEventsListener: LifecycleEventListener {
    override fun onHostResume() { /* Default implementation */ }

    override fun onHostPause() { /* Default implementation */ }

    override fun onHostDestroy() { /* Default implementation */ }
}