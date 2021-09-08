package com.guichaguri.trackplayer

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class ExampleApplication: Application() {
    override fun onCreate() {
        Logger.addLogAdapter(AndroidLogAdapter())
        super.onCreate()
    }
}