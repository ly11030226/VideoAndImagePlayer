package com.jzl.videoandimageplayer

import android.app.Application
import com.jzl.viewcycle.VideoPlayerManager

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VideoPlayerManager.init()
    }
}