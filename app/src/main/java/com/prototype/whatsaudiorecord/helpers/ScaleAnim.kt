package com.prototype.whatsaudiorecord.helpers

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class ScaleAnim(view: View) {

    private val view: View

    init {
        this.view = view
    }

    fun start() {
        val set = AnimatorSet()
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 2.0f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 2.0f)
        set.setDuration(150)
        set.setInterpolator(AccelerateDecelerateInterpolator())
        set.playTogether(scaleY, scaleX)
        set.start()
    }
    fun stop() {
        val set = AnimatorSet()
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
        // scaleY.setDuration(250);
        // scaleY.setInterpolator(new DecelerateInterpolator());
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
        // scaleX.setDuration(250);
        // scaleX.setInterpolator(new DecelerateInterpolator());
        set.setDuration(150)
        set.setInterpolator(AccelerateDecelerateInterpolator())
        set.playTogether(scaleY, scaleX)
        set.start()
    }

}