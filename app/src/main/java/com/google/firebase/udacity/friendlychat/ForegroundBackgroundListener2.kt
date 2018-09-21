package com.google.firebase.udacity.friendlychat

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner

class ForegroundBackgroundListener2 : DefaultLifecycleObserver {

    /*
    * This method will be called after the LifecycleOwner's onCreate method returns.
    * */
    override fun onCreate(owner: LifecycleOwner) {
        // compile with -jvm-target 1.8
       // super.onCreate(owner)
    }

    /*
    * This method will be called before the LifecycleOwner's onPause method is called.
    * */
    override fun onPause(owner: LifecycleOwner) {
        // compile with -jvm-target 1.8 no super call allowed
        //super.onPause(owner)
    }

    /*
    *
    * This method will be called after the LifecycleOwner's onStart method returns.
    * */
    override fun onStart(owner: LifecycleOwner) {
        // super.onStart(owner)
    }


    /*
    * This method will be called after the LifecycleOwner's onResume method returns
    * */
    override fun onResume(owner: LifecycleOwner) {
        // super.onResume(owner)
    }

}