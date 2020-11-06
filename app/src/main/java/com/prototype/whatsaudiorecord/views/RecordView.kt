package com.prototype.whatsaudiorecord.views

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.facebook.shimmer.ShimmerFrameLayout
import com.prototype.whatsaudiorecord.R
import com.prototype.whatsaudiorecord.helpers.AnimationHelper
import com.prototype.whatsaudiorecord.interfaces.OnBasketAnimationEnd
import com.prototype.whatsaudiorecord.interfaces.OnRecordListener
import com.prototype.whatsaudiorecord.utils.DpUtil
import java.io.IOException

class RecordView: RelativeLayout {

    companion object {
        val DEFAULT_CANCEL_BOUNDS = 8 //8dp
    }

    private var RECORD_START = R.raw.record_start
    private var RECORD_FINISHED = R.raw.record_finished
    private var RECORD_ERROR = R.raw.record_error
    private lateinit var player: MediaPlayer

    private lateinit var smallBlinkingMic:ImageView
    private lateinit var basketImg:ImageView
    private lateinit var counterTime: Chronometer
    private lateinit var slideToCancel: TextView
    private lateinit var slideToCancelLayout:ShimmerFrameLayout
    private lateinit var arrow: ImageView

    private var initialX:Float = 0.toFloat()
    private var basketInitialY:Float = 0.toFloat()
    private var difX = 0f

    var cancelBounds = DEFAULT_CANCEL_BOUNDS.toFloat()

    private var startTime:Long = 0
    private var elapsedTime:Long = 0
    private lateinit var mContext: Context
    private lateinit var recordListener: OnRecordListener
    private var isSwiped:Boolean = false
    private var isLessThanSecondAllowed = false
    private var isSoundEnabled = true

    private lateinit var animationHelper: AnimationHelper


    constructor(context: Context) : super(context) {

        mContext = context
        init(context, null!!, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        mContext = context
        init(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        mContext = context
        init(context, attrs, defStyleAttr, -1)
    }

    private fun init(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val view = View.inflate(context, R.layout.record_view, null)
        addView(view)
        val viewGroup = view.getParent() as ViewGroup
        viewGroup.setClipChildren(false)
        arrow = view.findViewById(R.id.arrow)
        slideToCancel = view.findViewById(R.id.slide_to_cancel)
        smallBlinkingMic = view.findViewById(R.id.glowing_mic)
        counterTime = view.findViewById(R.id.counter_tv)
        basketImg = view.findViewById(R.id.basket_img)
        slideToCancelLayout = view.findViewById(R.id.shimmer_layout)
        hideViews(true)

        if (defStyleAttr == -1 && defStyleRes == -1) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes)
            val slideArrowResource = typedArray.getResourceId(R.styleable.RecordView_slide_to_cancel_arrow, -1)
            val slideToCancelText = typedArray.getString(R.styleable.RecordView_slide_to_cancel_text)
            val slideMarginRight = typedArray.getDimension(R.styleable.RecordView_slide_to_cancel_margin_right, 30F).toInt()
            val counterTimeColor = typedArray.getColor(R.styleable.RecordView_counter_time_color, -1)
            val arrowColor = typedArray.getColor(R.styleable.RecordView_slide_to_cancel_arrow_color, -1)
            val cancelBounds = typedArray.getDimensionPixelSize(R.styleable.RecordView_slide_to_cancel_bounds, -1)

            if (cancelBounds != -1)
                setCancelBounds(cancelBounds.toFloat(), false)//don't convert it to pixels since it's already in pixels

            if (slideArrowResource != -1) {
                val slideArrow = AppCompatResources.getDrawable(getContext(), slideArrowResource)
                arrow.setImageDrawable(slideArrow)
            }

            if (slideToCancelText != null)
                slideToCancel.setText(slideToCancelText)

            if (counterTimeColor != -1)
                setCounterTimeColor(counterTimeColor)

            if (arrowColor != -1)
                setSlideToCancelArrowColor(arrowColor)

            setMarginRight(slideMarginRight, true)
            typedArray.recycle()
        }
        animationHelper = AnimationHelper(context, basketImg, smallBlinkingMic)
    }

    private fun hideViews(hideSmallMic: Boolean) {
        slideToCancelLayout.setVisibility(GONE)
        counterTime.setVisibility(GONE)
        if (hideSmallMic)
            smallBlinkingMic.setVisibility(GONE)
    }

    private fun showViews() {
        slideToCancelLayout.setVisibility(VISIBLE)
        smallBlinkingMic.setVisibility(VISIBLE)
        counterTime.setVisibility(VISIBLE)
    }

    private fun isLessThanOneSecond(time: Long):Boolean {
        return time <= 1000
    }

    private fun playSound(soundRes: Int) {
        if (isSoundEnabled) {
            if (soundRes == 0)
                return

            try {
                player = MediaPlayer()
                val afd = mContext.getResources().openRawResourceFd(soundRes)
                if (afd == null) return
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
                afd.close()
                player.prepare()
                player.start()

                player.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {

                    override fun onCompletion(mp: MediaPlayer) {
                        mp.release()
                    }
                })

                player.setLooping(false)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun onActionDown(recordBtn: RecordButton, motionEvent: MotionEvent) {

        recordListener.onStart()
        animationHelper.setStartRecorded(true)
        animationHelper.resetBasketAnimation()
        animationHelper.resetSmallMic()
        recordBtn.startScale()
        slideToCancelLayout.startShimmer()
        initialX = recordBtn.getX()
        basketInitialY = basketImg.getY() + 90
        playSound(RECORD_START)
        showViews()
        animationHelper.animateSmallMicAlpha()
        counterTime.setBase(SystemClock.elapsedRealtime())
        startTime = System.currentTimeMillis()
        counterTime.start()
        isSwiped = false
    }

    fun onActionMove(recordBtn: RecordButton, motionEvent: MotionEvent) {

        val time = System.currentTimeMillis() - startTime

        if (!isSwiped) {
            //Swipe To Cancel
            if (slideToCancelLayout.x.toInt() != 0 && slideToCancelLayout.x <= counterTime.right + cancelBounds) {
                //if the time was less than one second then do not start basket animation
                if (isLessThanOneSecond(time)) {
                    hideViews(true)
                    animationHelper.clearAlphaAnimation(false)
                    animationHelper.onAnimationEnd()

                } else {
                    hideViews(false)
                    animationHelper.animateBasket(basketInitialY)
                }

                animationHelper.moveRecordButtonAndSlideToCancelBack(
                    recordBtn,
                    slideToCancelLayout,
                    initialX,
                    difX
                )
                counterTime.stop()
                slideToCancelLayout.stopShimmer()
                isSwiped = true
                animationHelper.setStartRecorded(false)
                recordListener.onCancel()

            } else {
                //if statement is to Prevent Swiping out of bounds
                if (motionEvent.getRawX() < initialX) {

                    recordBtn.animate()
                        .x(motionEvent.getRawX())
                        .setDuration(0)
                        .start()
                    if (difX == 0f)
                        difX = (initialX - slideToCancelLayout.getX())

                    slideToCancelLayout.animate()
                        .x(motionEvent.getRawX() - difX)
                        .setDuration(0)
                        .start()
                }
            }
        }
    }
    fun onActionUp(recordBtn: RecordButton) {

        elapsedTime = System.currentTimeMillis() - startTime

        if (!isLessThanSecondAllowed && isLessThanOneSecond(elapsedTime) && !isSwiped) {

            recordListener.onLessThanSecond()
            animationHelper.setStartRecorded(false)
            playSound(RECORD_ERROR)
        } else {
            if (!isSwiped)
                recordListener.onFinish(elapsedTime)
            animationHelper.setStartRecorded(false)
            if (!isSwiped)
                playSound(RECORD_FINISHED)
        }
        //if user has swiped then do not hide SmallMic since it will be hidden after swipe Animation
        hideViews(!isSwiped)
        if (!isSwiped)
            animationHelper.clearAlphaAnimation(true)
        animationHelper.moveRecordButtonAndSlideToCancelBack(
            recordBtn,
            slideToCancelLayout,
            initialX,
            difX
        )
        counterTime.stop()
        slideToCancelLayout.stopShimmer()
    }
    private fun setMarginRight(marginRight: Int, convertToDp: Boolean) {

        val layoutParams = slideToCancelLayout.getLayoutParams() as RelativeLayout.LayoutParams

        if (convertToDp) {
            layoutParams.rightMargin = DpUtil.toPixel(marginRight.toFloat(), mContext).toInt()
        } else
            layoutParams.rightMargin = marginRight
        slideToCancelLayout.setLayoutParams(layoutParams)
    }

    fun setOnRecordListener(recrodListener: OnRecordListener) {

        this.recordListener = recrodListener

    }

    fun setOnBasketAnimationEndListener(onBasketAnimationEndListener: OnBasketAnimationEnd) {

        animationHelper.setOnBasketAnimationEndListener(onBasketAnimationEndListener)

    }

    fun setSoundEnabled(isEnabled: Boolean) {

        isSoundEnabled = isEnabled

    }

    fun setLessThanSecondAllowed(isAllowed: Boolean) {

        isLessThanSecondAllowed = isAllowed

    }

    fun setSlideToCancelText(text: String) {

        slideToCancel.setText(text)

    }

    fun setSlideToCancelTextColor(color: Int) {
        slideToCancel.setTextColor(color)
    }

    fun setSmallMicColor(color: Int) {

        smallBlinkingMic.setColorFilter(color)

    }

    fun setSmallMicIcon(icon: Int) {

        smallBlinkingMic.setImageResource(icon)

    }

    fun setSlideMarginRight(marginRight: Int) {

        setMarginRight(marginRight, true)

    }

    fun setCustomSounds(startSound: Int, finishedSound: Int, errorSound: Int) {
        //0 means do not play sound
        RECORD_START = startSound
        RECORD_FINISHED = finishedSound
        RECORD_ERROR = errorSound

    }
    //set Chronometer color
    fun setCounterTimeColor(color: Int) {

        counterTime.setTextColor(color)

    }

    fun setSlideToCancelArrowColor(color: Int) {

        arrow.setColorFilter(color)

    }

    @JvmName("setCancelBounds1")
    fun setCancelBounds(cancelBounds:Float) {
        setCancelBounds(cancelBounds, true)
    }

    private fun setCancelBounds(cancelBounds: Float, convertDpToPixel: Boolean) {

        val bounds = if (convertDpToPixel) DpUtil.toPixel(cancelBounds, mContext) else cancelBounds
        this.cancelBounds = bounds
    }

}