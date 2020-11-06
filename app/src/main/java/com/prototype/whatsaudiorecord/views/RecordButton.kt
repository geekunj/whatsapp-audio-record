package com.prototype.whatsaudiorecord.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import com.prototype.whatsaudiorecord.R
import com.prototype.whatsaudiorecord.helpers.ScaleAnim
import com.prototype.whatsaudiorecord.interfaces.OnRecordClickListener

class RecordButton:AppCompatImageView, View.OnTouchListener, View.OnClickListener {

    private lateinit var onRecordClickListener:OnRecordClickListener
    private lateinit var scaleAnim: ScaleAnim
    private lateinit var recordView:RecordView

    var isListenForRecord = true

    fun setRecordView(recordView:RecordView) {
        this.recordView = recordView
    }

    constructor(context: Context) : super(context) {
        init(context, null!!)
    }

    constructor(context:Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context:Context, attrs:AttributeSet, defStyleAttr:Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context:Context, attrs:AttributeSet) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordButton)
        val imageResource = typedArray.getResourceId(R.styleable.RecordButton_mic_icon, -1)

        if (imageResource != -1) {

            setTheImageResource(imageResource)

        }

        typedArray.recycle()

        scaleAnim = ScaleAnim(this)
        this.setOnTouchListener(this)
        this.setOnClickListener(this)
    }

    override fun onAttachedToWindow() {

        super.onAttachedToWindow()
        setClip(this)
    }

    fun setClip(v:View) {

        if (v.getParent() == null) {
            return
        }

        if (v is ViewGroup) {
            (v as ViewGroup).setClipChildren(false)
            (v as ViewGroup).setClipToPadding(false)
        }

        if (v.getParent() is View) {
            setClip(v.getParent() as View)
        }

    }

    private fun setTheImageResource(imageResource:Int) {

        val image = AppCompatResources.getDrawable(getContext(), imageResource)
        setImageDrawable(image)
    }

    override fun onTouch(v:View, event:MotionEvent):Boolean {

        if (isListenForRecord) {
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> recordView.onActionDown(v as RecordButton, event)
                MotionEvent.ACTION_MOVE -> recordView.onActionMove(v as RecordButton, event)
                MotionEvent.ACTION_UP -> recordView.onActionUp(v as RecordButton)
            }
        }

        return isListenForRecord

    }

    fun startScale() {

        scaleAnim.start()

    }

    fun stopScale() {

        scaleAnim.stop()

    }

    fun setOnRecordClickListener(onRecordClickListener: OnRecordClickListener) {

        this.onRecordClickListener = onRecordClickListener

    }

    override fun onClick(v:View) {

        onRecordClickListener.onClick(v)

    }
}