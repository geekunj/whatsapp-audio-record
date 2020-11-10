package com.prototype.whatsaudiorecord.ui.main.adapters

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prototype.whatsaudiorecord.R
import com.prototype.whatsaudiorecord.databinding.RecordingItemBinding
import com.prototype.whatsaudiorecord.models.Recording
import com.prototype.whatsaudiorecord.ui.main.MainActivity
import com.prototype.whatsaudiorecord.ui.main.MainActivityViewModel
import java.io.IOException

class RecordingsAdapter(private val viewModel: MainActivityViewModel, var mainActivity: MainActivity)
    : ListAdapter<Recording, RecordingsAdapter.RecordingViewHolder>(DiffCallback()) {

    private val TAG = "RecordingsAdapter"

    private var mPlayer : MediaPlayer? = null
    private var currentPlayingPosition:Int = 0
    private var seekBarUpdater:SeekBarUpdater? = null
    private var recordingHolder: RecordingViewHolder? = null
    /*var recordingsList = listOf<Recording>()

    fun setData(items:List<Recording>){
        recordingsList = items
    }*/

    init {
        currentPlayingPosition = -1
        seekBarUpdater = SeekBarUpdater()
    }



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

        holder.bind(viewModel, getItem(position), mainActivity)

        if (position == currentPlayingPosition){
            recordingHolder = holder
            updatePlayingView()
        }else{
            updateNonPlayingView(holder)
        }

        //holder.recordingItemBinding.recordingItem = recordingsList[position]

    }

    override fun onViewRecycled(holder: RecordingViewHolder) {
        super.onViewRecycled(holder)
        if (currentPlayingPosition == holder.adapterPosition)
        {
            recordingHolder?.let { updateNonPlayingView(it) }
            recordingHolder = null
        }
    }

    private fun updateNonPlayingView(holder:RecordingViewHolder) {
        holder.recordingItemBinding.seekbar.removeCallbacks(seekBarUpdater)
        holder.recordingItemBinding.seekbar.setEnabled(false)
        holder.recordingItemBinding.seekbar.setProgress(0)
        holder.recordingItemBinding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
    }


    private fun updatePlayingView() {
        recordingHolder!!.recordingItemBinding.seekbar.max = mPlayer!!.duration
        recordingHolder!!.recordingItemBinding.seekbar.progress = mPlayer!!.currentPosition
        recordingHolder!!.recordingItemBinding.seekbar.isEnabled = true
        if (mPlayer!!.isPlaying()) {
            recordingHolder!!.recordingItemBinding.seekbar.postDelayed(seekBarUpdater, 100)
            recordingHolder!!.recordingItemBinding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        }
        else {
            recordingHolder!!.recordingItemBinding.seekbar.removeCallbacks(seekBarUpdater)
            recordingHolder!!.recordingItemBinding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    internal fun stopPlayer() {
        if (null != mPlayer)
        {
            releaseMediaPlayer()
        }
    }



    inner class SeekBarUpdater:Runnable  {
        override fun run() {
            if (null != recordingHolder) {
                recordingHolder!!.recordingItemBinding.seekbar.setProgress(mPlayer!!.currentPosition)
                recordingHolder!!.recordingItemBinding.seekbar.postDelayed(this, 100)
            }
        }

    }
    inner class RecordingViewHolder
        (val recordingItemBinding: RecordingItemBinding) : RecyclerView.ViewHolder(recordingItemBinding.root){


        fun bind(viewModel: MainActivityViewModel, item: Recording?, mainActivity: MainActivity) {
            recordingItemBinding.viewModel = viewModel
            recordingItemBinding.recordingItem = item
            recordingItemBinding.executePendingBindings()

            recordingItemBinding.seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar:SeekBar) {
                }
                override fun onStartTrackingTouch(seekBar:SeekBar) {
                }
                override fun onProgressChanged(seekBar:SeekBar, progress:Int, fromUser:Boolean) {
                    if (mPlayer != null && fromUser) {
                        mPlayer!!.seekTo(progress)
                    }
                }
            })
            /*mPlayer?.setOnCompletionListener {
                stopPlaying()
            }*/




            recordingItemBinding.btnPlayPause.setOnClickListener {

                if (adapterPosition == currentPlayingPosition) {
                    if (mPlayer!!.isPlaying) {
                        mPlayer!!.pause()
                    } else {
                        mPlayer!!.start()
                    }
                } else {
                    currentPlayingPosition = adapterPosition
                    if (mPlayer != null) {
                        if (null != recordingHolder) {
                            updateNonPlayingView(recordingHolder!!)
                        }
                        mPlayer!!.release()
                    }
                    recordingHolder = this
                    startMediaPlayer(item?.fileName)
                }
                updatePlayingView()


                /*if (mPlayer != null && mPlayer!!.isPlaying()) {
                    
                    //mPlayer!!.pause()
                    mPlayer?.release()
                    mPlayer = null
                    mPlayer = MediaPlayer()
                    try {
                        mPlayer!!.setDataSource(item?.fileName)
                        mPlayer!!.prepare()
                        recordingItemBinding.seekbar.max = mPlayer!!.duration/1000
                        mPlayer!!.start()
                    } catch (e: IOException) {
                        Log.e(TAG, "prepare() failed")
                    }


                } *//*else if (mPlayer != null) {
                    mPlayer!!.start()
                } *//*else {
                    mPlayer = MediaPlayer()
                    try {
                        mPlayer!!.setDataSource(item?.fileName)
                        mPlayer!!.prepare()
                        recordingItemBinding.seekbar.max = mPlayer!!.duration/1000
                        mPlayer!!.start()
                    } catch (e: IOException) {
                        Log.e(TAG, "prepare() failed")
                    }
                }
                val mHandler = Handler(Looper.getMainLooper())
                mainActivity.runOnUiThread(object : Runnable {
                    public override fun run() {
                        if (mPlayer != null) {
                            val mCurrentPosition = mPlayer!!.getCurrentPosition() / 1000
                            recordingItemBinding.seekbar.setProgress(mCurrentPosition)
                        } else {
                            Log.d(TAG, "run: mPlays is null")
                        }
                        mHandler.postDelayed(this, 10)
                    }
                })
*/







            }


        }

        /*override fun onClick(v: View?) {
            if (adapterPosition == currentPlayingPosition) {
                if (mPlayer!!.isPlaying) {
                    mPlayer!!.pause()
                } else {
                    mPlayer!!.start()
                }
            } else {
                currentPlayingPosition = adapterPosition
                if (mPlayer != null) {
                    if (null != recordingHolder) {
                        updateNonPlayingView(recordingHolder!!)
                    }
                    mPlayer!!.release()
                }
                recordingHolder = this
                startMediaPlayer(audioItems.get(currentPlayingPosition).audioResId)
            }
            updatePlayingView()
        }*/


    }

    private fun startMediaPlayer(audioPath:String?) {
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(audioPath)
        mPlayer!!.setOnCompletionListener {
            releaseMediaPlayer()
        }
        mPlayer!!.prepare()
        mPlayer!!.start()
    }
    private fun releaseMediaPlayer() {
        if (null != recordingHolder) {
            updateNonPlayingView(recordingHolder!!)
        }
        mPlayer!!.release()
        mPlayer = null
        currentPlayingPosition = -1
    }

    /*inner class RecordingViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val image = view.findViewById<ImageView>(R.id.recording_item)


        fun bind(recording: Recording){
            image.setImageResource(homeSlide.image)
        }

    }*/

    class DiffCallback : DiffUtil.ItemCallback<Recording>() {
        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem == newItem
        }


    }

    private fun startPlaying(item: Recording?) {
        if (mPlayer != null && mPlayer!!.isPlaying()) {
            mPlayer!!.pause()
        } else if (mPlayer != null) {
            mPlayer!!.start()
        } else {
            mPlayer = MediaPlayer()
            try {
                mPlayer!!.setDataSource(item?.fileName)

                mPlayer!!.prepare()
                mPlayer!!.start()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
        //startPlaying.setText("Start playing")
    }

    private fun pausePlaying() {
        if (mPlayer?.isPlaying!!) {
            mPlayer?.pause()
        } else {
            mPlayer?.start()
        }
    }


}