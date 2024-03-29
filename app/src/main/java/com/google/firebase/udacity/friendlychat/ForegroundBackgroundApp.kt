package com.google.firebase.udacity.friendlychat

import android.app.Application
import android.arch.lifecycle.ProcessLifecycleOwner

class ForegroundBackgroundApp : Application() {

    private lateinit var appObserver: ForegroundBackgroundListener2

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner
                .get()
                .lifecycle
                .addObserver(
                        ForegroundBackgroundListener2()
                                .also {
                                    appObserver = it
                                }
                )
    }
}