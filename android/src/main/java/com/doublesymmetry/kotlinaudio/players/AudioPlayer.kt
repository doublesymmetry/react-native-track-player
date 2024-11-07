package com.doublesymmetry.kotlinaudio.players

import android.content.Context
import com.doublesymmetry.kotlinaudio.models.BufferConfig
import com.doublesymmetry.kotlinaudio.models.CacheConfig
import com.doublesymmetry.kotlinaudio.models.PlayerConfig

class AudioPlayer(context: Context, playerConfig: PlayerConfig = PlayerConfig(), bufferConfig: BufferConfig? = null, cacheConfig: CacheConfig? = null): BaseAudioPlayer(context, playerConfig, bufferConfig, cacheConfig)