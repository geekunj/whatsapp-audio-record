package com.prototype.whatsaudiorecord.utils

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prototype.whatsaudiorecord.models.Recording
import com.prototype.whatsaudiorecord.ui.main.adapters.RecordingsAdapter


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

@BindingAdapter("data")
fun setRecyclerViewProperties(recyclerView: RecyclerView, items: List<Recording>?) {
    if (recyclerView.adapter is RecordingsAdapter) {

        if (items != null) {
            (recyclerView.adapter as RecordingsAdapter).setData(items)
        }
    }
}


