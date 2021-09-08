package com.guichaguri.trackplayer.model

enum class State(val value: Int) {
    None(1),
    Ready(2),
    Playing(3),
    Paused(4),
    Stopped(5),
    Buffering(6),
    Connecting(7)
}