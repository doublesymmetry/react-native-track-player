package com.guichaguri.trackplayer.models

enum class State(val value: Int) {
    None(1),
    Ready(2),
    Playing(3),
    Paused(4),
    Stopped(5),
    Buffering(6),
    Connecting(7)
}