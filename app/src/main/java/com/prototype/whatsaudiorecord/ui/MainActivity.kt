package com.prototype.whatsaudiorecord.ui

import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prototype.whatsaudiorecord.R
import com.prototype.whatsaudiorecord.adapters.RecordingsAdapter
import com.prototype.whatsaudiorecord.data.AppDatabase
import com.prototype.whatsaudiorecord.data.Repository
import com.prototype.whatsaudiorecord.data.local.dao.RecordDao
import com.prototype.whatsaudiorecord.interfaces.OnBasketAnimationEnd
import com.prototype.whatsaudiorecord.interfaces.OnRecordClickListener
import com.prototype.whatsaudiorecord.interfaces.OnRecordListener
import com.prototype.whatsaudiorecord.models.Recording
import com.prototype.whatsaudiorecord.views.RecordButton
import com.prototype.whatsaudiorecord.views.RecordView
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException


class MainActivity : AppCompatActivity() {


    private val recordingDao: RecordDao? = null
    private lateinit var mediaPlayer: MediaPlayer
    private val REQUEST_STORAGE_STATE: Int = 1001
    private val REQUEST_AUDIO_STATE: Int = 1002
    private val TAG = "MainActivity"

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    private lateinit var currentDatetime:LocalDateTime
    private lateinit var dateTimeFormatter:DateTimeFormatter
    private lateinit var formattedDateTime:String

    private lateinit var mDb:AppDatabase

    lateinit var recordingList: List<Recording>

    lateinit var recyclerView:RecyclerView;
    lateinit var factory: RecordingsViewModelFactory
    lateinit var viewModel: MainActivityViewModel

    override fun onResume() {
        super.onResume()
        recordingList = mDb.RecordDao().getAllAudioRecords()

    }

    fun playAudio(){
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(output)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        AndroidThreeTen.init(this)
        mDb = AppDatabase.getInstance(applicationContext)

        val repository = Repository(mDb.RecordDao())
        factory = repository?.let { RecordingsViewModelFactory(it) }!!
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)
        viewModel.getRecordings()
        viewModel.recordings.observe(this@MainActivity, Observer { recordings->
            recyclerView.also {
                it.layoutManager = LinearLayoutManager(this@MainActivity)
                it.setHasFixedSize(true)
                it.adapter = RecordingsAdapter(recordings)
            }
        })








        checkPermissions()







        val recordView = findViewById(R.id.record_view) as RecordView
        val recordButton = findViewById(R.id.record_button) as RecordButton
        //IMPORTANT
        recordButton.setRecordView(recordView)
        // if you want to click the button (in case if you want to make the record button a Send Button for example..)
        // recordButton.setListenForRecord(false);

        //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordClickListener(object : OnRecordClickListener {
            override fun onClick(v: View) {
                Toast.makeText(this@MainActivity, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT)
                    .show()
                Log.d("RecordButton", "RECORD BUTTON CLICKED")
            }
        })
        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        recordView.setCancelBounds(8F)
        recordView.setSmallMicColor(Color.parseColor("#c2185b"))
        //prevent recording under one Second
        recordView.setLessThanSecondAllowed(false)
        recordView.setSlideToCancelText("Slide To Cancel")
        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0)
        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                Log.d("RecordView", "onStart")
                Toast.makeText(this@MainActivity, "OnStartRecord", Toast.LENGTH_SHORT).show()
                startRecording()

            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "onCancel", Toast.LENGTH_SHORT).show()
                Log.d("RecordView", "onCancel")
                stopRecording()
            }

            override fun onFinish(recordTime: Long) {
                val time = getHumanTimeText(recordTime)
                Toast.makeText(
                    this@MainActivity,
                    "onFinishRecord - Recorded Time is: " + time,
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("RecordView", "onFinish")
                Log.d("RecordTime", time)
                stopRecording()
            }

            override fun onLessThanSecond() {
                Toast.makeText(this@MainActivity, "OnLessThanSecond", Toast.LENGTH_SHORT).show()
                Log.d("RecordView", "onLessThanSecond")
                stopRecording()
            }
        })
        recordView.setOnBasketAnimationEndListener(object : OnBasketAnimationEnd {
            override fun onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startRecording() {
        try {
            state = true
            mediaRecorder = MediaRecorder()
            resetRecorder()
            mediaRecorder!!.start()

            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
        if(state){
            val recording = output?.let { Recording(it, formattedDateTime) }
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                recording?.let { mDb.RecordDao().insertRecording(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            state = false
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetRecorder(){
        currentDatetime = LocalDateTime.now()
        dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss")
        formattedDateTime = currentDatetime.format(dateTimeFormatter)
        try {
            output = Environment.getExternalStorageDirectory().absolutePath + "/recording-"+formattedDateTime+".mp3"
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder!!.setAudioEncodingBitRate(16)
            mediaRecorder!!.setAudioSamplingRate(44100)
            mediaRecorder!!.setOutputFile(output)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            mediaRecorder!!.prepare()
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getHumanTimeText(milliseconds: Long):String {
        return String.format(
            "%02d:%02d",
            java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            (java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(milliseconds) - java.util.concurrent.TimeUnit.MINUTES.toSeconds(
                java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            ))
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Check if permission is not granted
            Log.d(TAG, "Permissions not granted")

            // This condition only becomes true if the user has denied the permission previously
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRationaleDialog(
                    getString(R.string.rationale_title),
                    getString(R.string.rationale_desc),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    REQUEST_STORAGE_STATE
                )

            }
            else if(shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)){
                showRationaleDialog(
                    getString(R.string.rationale_title),
                    getString(R.string.rationale_desc),
                    android.Manifest.permission.RECORD_AUDIO,
                    REQUEST_AUDIO_STATE
                )
            } else {
                // Perform a permission check
                Log.d(TAG, "Checking permission")
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.RECORD_AUDIO
                    ),
                    REQUEST_STORAGE_STATE
                )
            }
        } else {

            output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"

            // Permission is already granted, do your magic here!
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showRationaleDialog(
        title: String,
        message: String,
        permission: String,
        requestCode: Int
    ) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok", { dialog, which ->
                requestPermissions(arrayOf(permission), requestCode)
            })
        builder.create().show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Storage Permission Permission granted",
                        Toast.LENGTH_SHORT
                    ).show()


                } else {
                    Toast.makeText(this, " Storage Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_AUDIO_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Recording Permission granted", Toast.LENGTH_SHORT).show()


                } else {
                    Toast.makeText(this, "Recording Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
