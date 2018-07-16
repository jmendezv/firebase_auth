package com.google.firebase.udacity.friendlychat

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log

class ForegroundBackgroundListener: LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun createSomething() {
        Log.v("ProcessLog", "Lifecycle.Event.ON_CREATE")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startSomething() {
        Log.v("ProcessLog", "Lifecycle.Event.ON_START")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopSomething() {
        Log.v("ProcessLog", "Lifecycle.Event.ON_STOP")
    }
}