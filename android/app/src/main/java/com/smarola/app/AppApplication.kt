package com.smarola.app

import android.app.Application
import com.smarola.core.ActivityTracker

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ActivityTracker)
    }
}
