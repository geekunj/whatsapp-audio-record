package com.prototype.whatsaudiorecord.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.prototype.whatsaudiorecord.models.Recording

@Dao
interface RecordDao {

  @Query("SELECT * FROM recordings")
  fun getAllAudioRecords(): List<Recording>

  @Insert
  fun insertRecording(recording:Recording)
}