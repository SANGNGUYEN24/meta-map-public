package com.sang.metamap.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

object ViewUtil {
    fun View.gone() {
        this.visibility = View.GONE
    }

    fun View.visible() {
        this.visibility = View.VISIBLE
    }

    fun View.fadeIn(duration: Long = 200L) {
        apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).duration = duration
        }
    }

    fun View.fadeOut(duration: Long = 200L) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                }
            })
    }

    fun View.fadeInUp(duration: Long = 200L, height: Float = 32F) {
        apply {
            alpha = 0f
            translationY = height
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setListener(null)
        }
    }

    fun View.fadeInDown(duration: Long, height: Float) {
        apply {
            alpha = 0f
            translationY = -height
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setListener(null)
        }
    }

    fun View.fadeInDown(duration: Long) {
        apply {
            alpha = 0f
            translationY = -24F
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setListener(null)
        }
    }

    fun View.fadeOutUp(duration: Long = 200L, height: Float = 32F) {
        apply {
            animate()
                .alpha(0f)
                .translationY(-height)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = View.GONE
                    }
                })
        }
    }

    fun View.fadeOutDown(duration: Long = 200L, height: Float = 32F) {
        apply {
            animate()
                .alpha(0f)
                .translationY(height)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = View.GONE
                    }
                })
        }
    }

    fun View.animFadeInFadeOut() {
        this.animate()
            .alpha(0.5f)
            .setDuration(500)
            .withEndAction {
                this.alpha = 1f
                this.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }
            .start()
    }
}