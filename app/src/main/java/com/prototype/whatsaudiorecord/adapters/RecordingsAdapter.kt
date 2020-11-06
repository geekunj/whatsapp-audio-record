package com.prototype.whatsaudiorecord.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.prototype.whatsaudiorecord.R
import com.prototype.whatsaudiorecord.databinding.RecordingItemBinding
import com.prototype.whatsaudiorecord.models.Recording

class RecordingsAdapter(private val recordings: List<Recording>)
    : RecyclerView.Adapter<RecordingsAdapter.RecordingViewHolder>() {

    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeSliderViewHolder {

        return HomeSliderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.home_slider_item,
                parent,
                false
            )
        )
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecordingViewHolder(
            DataBindingUtil.inflate<RecordingItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.recording_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {

        holder.recordingItemBinding.recordingItem = recordings[position]

    }

    override fun getItemCount(): Int {

        return recordings.size
    }

    inner class RecordingViewHolder
        (val recordingItemBinding: RecordingItemBinding) : RecyclerView.ViewHolder(recordingItemBinding.root)

    /*inner class HomeSliderViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val image = view.findViewById<ImageView>(R.id.iv_slider_image)


        fun bind(homeSlide: HomeSlide){
            image.setImageResource(homeSlide.image)
        }

    }*/


}