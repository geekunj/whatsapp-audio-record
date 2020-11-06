package com.prototype.whatsaudiorecord.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prototype.whatsaudiorecord.data.Repository

@Suppress("UNCHECKED_CAST")
class RecordingsViewModelFactory(private val repository: Repository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainActivityViewModel(repository) as T
    }

}