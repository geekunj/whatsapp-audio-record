package com.prototype.whatsaudiorecord.data

import androidx.lifecycle.LiveData
import com.prototype.whatsaudiorecord.data.local.dao.RecordDao
import com.prototype.whatsaudiorecord.models.Recording


class Repository(private val recordingDao: RecordDao) {

    val allRecordings: List<Recording> = recordingDao.getAllAudioRecords()
}