package com.prototype.whatsaudiorecord.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prototype.whatsaudiorecord.data.AppDatabase
import com.prototype.whatsaudiorecord.data.Repository
import com.prototype.whatsaudiorecord.models.Recording

class MainActivityViewModel(val repository: Repository):ViewModel() {




    private val _recordings = MutableLiveData<List<Recording>>()
    val recordings: LiveData<List<Recording>>
        get() =_recordings


    fun getRecordings(){
        _recordings.value = repository.allRecordings
    }



}