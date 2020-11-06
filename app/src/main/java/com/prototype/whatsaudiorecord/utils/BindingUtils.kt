package com.prototype.whatsaudiorecord.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("text")
fun setfileNumber(fileNumText: TextView, fileId: Int) {
    fileNumText.text = fileId.toString()
}

@BindingAdapter("text")
fun setfileName(fileNameText: TextView, fileName: String) {
    fileNameText.text = fileName
}

@BindingAdapter("text")
fun setTimeStamp(timeText: TextView, time: String) {
    timeText.text = time
}
