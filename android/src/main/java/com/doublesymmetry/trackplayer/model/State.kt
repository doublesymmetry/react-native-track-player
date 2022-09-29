package com.doublesymmetry.trackplayer.model

enum class State(val state: String) {
    Idle("idle"),
    Ready("ready"),
    Playing("playing"),
    Paused("paused"),
    Stopped("stopped"),
    Buffering("buffering"),
    Loading("loading"),
    Error("error")
}