package com.prototype.whatsaudiorecord.ui.main

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prototype.whatsaudiorecord.data.AppDatabase
import com.prototype.whatsaudiorecord.data.Repository
import com.prototype.whatsaudiorecord.models.Recording
import java.io.IOException

class MainActivityViewModel():ViewModel() {



    private val _recordings = MutableLiveData<List<Recording>>()
    val recordings: LiveData<List<Recording>>
        get() =_recordings


    fun setRecordings(context: Context){
        _recordings.value = AppDatabase.getInstance(context).RecordDao().getAllAudioRecords()
    }








}