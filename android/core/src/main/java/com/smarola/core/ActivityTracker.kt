package com.smarola.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentRef = WeakReference<Activity>(null)

    val currentActivity: Activity?
        get() = currentRef.get()

    override fun onActivityResumed(activity: Activity) {
        currentRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentRef.get() === activity) currentRef.clear()
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentRef.get() === activity) currentRef.clear()
    }

    override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
}
