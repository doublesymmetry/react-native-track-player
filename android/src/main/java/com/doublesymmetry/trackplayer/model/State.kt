package com.doublesymmetry.trackplayer.model

enum class State(val state: String) {
    Buffering("buffering"),
    Idle("idle"),
    Ready("ready"),
    Paused("paused"),
    Stopped("stopped"),
    Playing("playing"),
    Loading("loading"),
    Error("error"),
    Ended("ended"),
}