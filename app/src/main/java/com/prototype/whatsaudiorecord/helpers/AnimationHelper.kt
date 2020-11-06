package com.prototype.whatsaudiorecord.helpers

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat
import com.prototype.whatsaudiorecord.R
import com.prototype.whatsaudiorecord.interfaces.OnBasketAnimationEnd
import com.prototype.whatsaudiorecord.views.RecordButton

class AnimationHelper(context: Context, basketImg: ImageView, smallBlinkingMic:ImageView) {

    private val context:Context
    private val animatedVectorDrawable: AnimatedVectorDrawableCompat
    private val basketImg:ImageView
    private val smallBlinkingMic:ImageView
    private lateinit var alphaAnimation: AlphaAnimation
    private lateinit var onBasketAnimationEndListener: OnBasketAnimationEnd
    private var isBasketAnimating:Boolean = false
    private var isStartRecorded = false
    private var micX:Float = 0.toFloat()
    private var micY = 0f
    private lateinit var micAnimation: AnimatorSet
    private lateinit var translateAnimation1: TranslateAnimation
    private lateinit var translateAnimation2:TranslateAnimation
    private lateinit var handler1: Handler
    private lateinit var handler2: Handler


    init{
        this.context = context
        this.smallBlinkingMic = smallBlinkingMic
        this.basketImg = basketImg
        animatedVectorDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.recv_basket_animated)!!
    }

    @SuppressLint("RestrictedApi")
    fun animateBasket(basketInitialY:Float) {
        isBasketAnimating = true
        clearAlphaAnimation(false)
        //save initial x,y values for mic icon
        if (micX == 0f)
        {
            micX = smallBlinkingMic.getX()
            micY = smallBlinkingMic.getY()
        }
        micAnimation = AnimatorInflaterCompat.loadAnimator(context, R.animator.delete_mic_animation) as AnimatorSet
        micAnimation.setTarget(smallBlinkingMic) // set the view you want to animate
        translateAnimation1 = TranslateAnimation(0F, 0F, basketInitialY, basketInitialY - 90)
        translateAnimation1.setDuration(250)
        translateAnimation2 = TranslateAnimation(0F, 0F, basketInitialY - 130, basketInitialY)
        translateAnimation2.setDuration(350)
        micAnimation.start()
        basketImg.setImageDrawable(animatedVectorDrawable)
        handler1 = Handler()
        handler1.postDelayed(object:Runnable {
            public override fun run() {
                basketImg.setVisibility(VISIBLE)
                basketImg.startAnimation(translateAnimation1)
            }
        }, 350)

        translateAnimation1.setAnimationListener(object: AnimationListener {

            override fun onAnimationStart(animation:Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {

                animatedVectorDrawable.start()
                handler2 = Handler()
                handler2.postDelayed(object:Runnable {
                    public override fun run() {
                        basketImg.startAnimation(translateAnimation2)
                        smallBlinkingMic.setVisibility(INVISIBLE)
                        basketImg.setVisibility(INVISIBLE)
                    }
                }, 450)
            }

            override fun onAnimationRepeat(animation:Animation) {

            }
        })

        translateAnimation2.setAnimationListener(object: AnimationListener {

            override fun onAnimationStart(animation:Animation) {

            }

            override fun onAnimationEnd(animation:Animation) {

                basketImg.setVisibility(INVISIBLE)
                isBasketAnimating = false
                //if the user pressed the record button while the animation is running
                // then do NOT call on Animation end
                if (!isStartRecorded) {
                    onBasketAnimationEndListener.onAnimationEnd()
                }

            }

            override fun onAnimationRepeat(animation:Animation) {

            }
        })
    }
    //if the user started a new Record while the Animation is running
    // then we want to stop the current animation and revert views back to default state
    fun resetBasketAnimation() {

        if (isBasketAnimating) {

            translateAnimation1.reset()
            translateAnimation1.cancel()
            translateAnimation2.reset()
            translateAnimation2.cancel()
            micAnimation.cancel()
            smallBlinkingMic.clearAnimation()
            basketImg.clearAnimation()

            handler1.removeCallbacksAndMessages(null)
            handler2.removeCallbacksAndMessages(null)

            basketImg.setVisibility(INVISIBLE)

            smallBlinkingMic.setX(micX)
            smallBlinkingMic.setY(micY)

            smallBlinkingMic.setVisibility(View.GONE)

            isBasketAnimating = false
        }
    }

    fun clearAlphaAnimation(hideView:Boolean) {

        alphaAnimation.cancel()
        alphaAnimation.reset()
        smallBlinkingMic.clearAnimation()

        if (hideView) {
            smallBlinkingMic.setVisibility(View.GONE)
        }
    }

    fun animateSmallMicAlpha() {

        alphaAnimation = AlphaAnimation(0.0f, 1.0f)
        alphaAnimation.setDuration(500)
        alphaAnimation.setRepeatMode(Animation.REVERSE)
        alphaAnimation.setRepeatCount(Animation.INFINITE)
        smallBlinkingMic.startAnimation(alphaAnimation)
    }

    fun moveRecordButtonAndSlideToCancelBack(recordBtn: RecordButton, slideToCancelLayout: FrameLayout, initialX:Float, difX:Float) {

        val positionAnimator = ValueAnimator.ofFloat(recordBtn.getX(), initialX)
        positionAnimator.setInterpolator(AccelerateDecelerateInterpolator())

        positionAnimator.addUpdateListener(object: ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation:ValueAnimator) {
                val x = animation.getAnimatedValue() as Float
                recordBtn.setX(x)
            }
        })

        recordBtn.stopScale()
        positionAnimator.setDuration(0)
        positionAnimator.start()
        // if the move event was not called ,then the difX will still 0 and there is no need to move it back
        if (difX != 0f) {

            val x = initialX - difX
            slideToCancelLayout.animate()
                .x(x)
                .setDuration(0)
                .start()
        }
    }

    fun resetSmallMic() {
        smallBlinkingMic.setAlpha(1.0f)
        smallBlinkingMic.setScaleX(1.0f)
        smallBlinkingMic.setScaleY(1.0f)
    }

    fun setOnBasketAnimationEndListener(onBasketAnimationEndListener:OnBasketAnimationEnd) {

        this.onBasketAnimationEndListener = onBasketAnimationEndListener
    }

    fun onAnimationEnd() {

        onBasketAnimationEndListener.onAnimationEnd()

    }
    //check if the user started a new Record by pressing the RecordButton
    fun setStartRecorded(startRecorded:Boolean) {

        isStartRecorded = startRecorded
    }
}