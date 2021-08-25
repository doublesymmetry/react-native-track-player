package com.doublesymmetry.kotlin_audio_sample

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class MainApplication: Application() {
    override fun onCreate() {
        Logger.addLogAdapter(AndroidLogAdapter())
        super.onCreate()
    }
}