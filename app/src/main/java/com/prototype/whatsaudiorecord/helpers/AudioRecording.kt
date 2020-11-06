package com.prototype.whatsaudiorecord.helpers

import android.util.Log
import com.prototype.whatsaudiorecord.interfaces.OnRecordListener
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

class AudioRecording {
  private lateinit var file: File
  private lateinit var mRecordingThread:Thread
  private val mStartingTimeMillis = 0L
  private lateinit var onAudioRecordListener:OnRecordListener
  private fun deleteFile() {
    val file = this.file
    if (file != null && file.exists())
    Log.d("AudioRecording", String.format("deleting file success %b ", *arrayOf<Any>(java.lang.Boolean.valueOf(this.file.delete()))))
  }
  private fun outputStream(paramFile:File): OutputStream {
    if (paramFile != null)
    try
    {
      return FileOutputStream(paramFile)
    }
    catch (fileNotFoundException: FileNotFoundException) {
      val stringBuilder = StringBuilder()
      stringBuilder.append("could not build OutputStream from this file ")
      stringBuilder.append(paramFile.getName())
      throw RuntimeException(stringBuilder.toString(), fileNotFoundException)
    }
    throw RuntimeException("file is null !")
  }
  fun setFile(paramString:String) {
    this.file = File(paramString)
  }
  fun setOnAudioRecordListener(paramOnAudioRecordListener:OnRecordListener) {
    this.onAudioRecordListener = paramOnAudioRecordListener
  }
  fun startRecording() {
  }
  fun stopRecording(paramBoolean:Boolean) {
  }
  companion object {
    val FILE_NULL = 3
    private val IO_ERROR = 1
    private val RECORDER_ERROR = 2
    private val TAG = "AudioRecording"
  }
}