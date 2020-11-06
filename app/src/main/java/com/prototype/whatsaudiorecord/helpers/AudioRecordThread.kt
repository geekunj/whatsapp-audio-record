package com.prototype.whatsaudiorecord.helpers

import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.experimental.or

class AudioRecordThread @Throws(IOException::class)
internal constructor(paramOutputStream: OutputStream, paramOnRecorderFailedListener:OnRecorderFailedListener):Runnable {
  private val audioRecord: AudioRecord
  private var bufferSize:Int = 0
  private val mediaCodec: MediaCodec
  private val onRecorderFailedListener:OnRecorderFailedListener
  private val outputStream:OutputStream
  init{
    val i = AudioRecord.getMinBufferSize(44100, 16, 2)
    this.bufferSize = i
    this.audioRecord = createAudioRecord(i)
    val mediaCodec = createMediaCodec(this.bufferSize)
    this.mediaCodec = mediaCodec
    this.outputStream = paramOutputStream
    this.onRecorderFailedListener = paramOnRecorderFailedListener
    mediaCodec.start()
    try
    {
      this.audioRecord.startRecording()

    }
    catch (exception:Exception) {
      Log.w(TAG, exception)
      this.mediaCodec.release()
      throw IOException(exception)
    }
  }
  private fun createAdtsHeader(paramInt:Int):ByteArray {
    var param = paramInt
    param += 7
    val arrayOfByte = ByteArray(7)
    arrayOfByte[0] = (-1).toByte()
    arrayOfByte[1] = (-15).toByte()
    arrayOfByte[2] = 64.toByte()
    arrayOfByte[2] = (arrayOfByte[2] or 0x10).toByte().toByte()
    arrayOfByte[2] = (0x0 or arrayOfByte[2].toInt()).toByte().toByte()
    arrayOfByte[3] = ((param shr 11) and 0x3 or 0x40).toByte().toByte()
    arrayOfByte[4] = ((param shr 3) and 0xFF).toByte().toByte()
    arrayOfByte[5] = (((param and 0x7) shl 5) or 0x1F).toByte().toByte()
    arrayOfByte[6] = (-4).toByte()
    return arrayOfByte
  }
  private fun createAudioRecord(paramInt:Int):AudioRecord {
    val audioRecord = AudioRecord(1, 44100, 16, 2, paramInt * 10)
    if (audioRecord.getState() === 1)
    {
      val noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId())
      if (noiseSuppressor != null)
      noiseSuppressor.setEnabled(true)
      val automaticGainControl = AutomaticGainControl.create(audioRecord.getAudioSessionId())
      if (automaticGainControl != null)
      automaticGainControl.setEnabled(true)
      return audioRecord
    }
    Log.d(TAG, "Unable to initialize AudioRecord")
    throw RuntimeException("Unable to initialize AudioRecord")
  }
  @SuppressLint("WrongConstant")
  @Throws(IOException::class)
  private fun createMediaCodec(paramInt:Int):MediaCodec {
    val mediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm")
    val mediaFormat = MediaFormat()
    mediaFormat.setString("mime", "audio/mp4a-latm")
    mediaFormat.setInteger("sample-rate", 44100)
    mediaFormat.setInteger("channel-count", 1)
    mediaFormat.setInteger("max-input-size", paramInt)
    mediaFormat.setInteger("bitrate", 32000)
    mediaFormat.setInteger("aac-profile", 2)
    try
    {
      mediaCodec.configure(mediaFormat, null, null, 1)
      return mediaCodec
    }
    catch (exception:Exception) {
      Log.w(TAG, exception)
      mediaCodec.release()
      throw IOException(exception)
    }
  }
  @Throws(IOException::class)
  private fun handleCodecInput(paramAudioRecord:AudioRecord, paramMediaCodec:MediaCodec, paramArrayOfByteBuffer:Array<ByteBuffer>, paramBoolean:Boolean):Boolean {
    var i = this.bufferSize
    val arrayOfByte = ByteArray(i)
    val j = paramAudioRecord.read(arrayOfByte, 0, i)
    if ((j == -2 || j == -3 || j != this.bufferSize) && j != this.bufferSize)
    {
      if (this.onRecorderFailedListener != null)
      {
        Log.d(TAG, "length != BufferSize calling onRecordFailed")
        this.onRecorderFailedListener.onRecorderFailed()
      }
      return false
    }
    val k = paramMediaCodec.dequeueInputBuffer(10000L)
    if (k >= 0)
    {
      val byteBuffer = paramArrayOfByteBuffer[k]
      byteBuffer.clear()
      byteBuffer.put(arrayOfByte)
      if (paramBoolean)
      {
        i = 0
      }
      else
      {
        i = 4
      }
      paramMediaCodec.queueInputBuffer(k, 0, j, 0L, i)
    }
    return true
  }
  @Throws(IOException::class)
  private fun handleCodecOutput(paramMediaCodec:MediaCodec, paramArrayOfByteBuffer:Array<ByteBuffer>, paramBufferInfo:MediaCodec.BufferInfo, paramOutputStream:OutputStream) {
    var mArray = paramArrayOfByteBuffer
    var i:Int
    i = paramMediaCodec.dequeueOutputBuffer(paramBufferInfo, 0L)
    while (i != -1)
    {
      if (i >= 0)
      {
        val byteBuffer = paramArrayOfByteBuffer[i]
        byteBuffer.position(paramBufferInfo.offset)
        byteBuffer.limit(paramBufferInfo.offset + paramBufferInfo.size)
        if ((paramBufferInfo.flags and 0x2) !== 2)
        {
          paramOutputStream.write(createAdtsHeader(paramBufferInfo.size - paramBufferInfo.offset))
          val arrayOfByte = ByteArray(byteBuffer.remaining())
          byteBuffer.get(arrayOfByte)
          paramOutputStream.write(arrayOfByte)
        }
        byteBuffer.clear()
        paramMediaCodec.releaseOutputBuffer(i, false)
      }
      else if (i == -3)
      {
        mArray = paramMediaCodec.getOutputBuffers()
      }
      i = paramMediaCodec.dequeueOutputBuffer(paramBufferInfo, 0L)
    }
  }
  public override fun run() {
    if (this.onRecorderFailedListener != null)
    {
      Log.d(TAG, "onRecorderStarted")
      this.onRecorderFailedListener.onRecorderStarted()
    }
    val bufferInfo = MediaCodec.BufferInfo()
    val arrayOfByteBuffer1 = this.mediaCodec.getInputBuffers()
    val arrayOfByteBuffer2 = this.mediaCodec.getOutputBuffers()
    try
    {
      while (!Thread.interrupted())
      {
        if (handleCodecInput(this.audioRecord, this.mediaCodec, arrayOfByteBuffer1, Thread.currentThread().isAlive()))
        handleCodecOutput(this.mediaCodec, arrayOfByteBuffer2, bufferInfo, this.outputStream)
      }
      this.mediaCodec.stop()
      this.audioRecord.stop()
      this.mediaCodec.release()
      this.audioRecord.release()
      try
      {
        this.outputStream.close()
      }
      catch (iOException:IOException) {
        iOException.printStackTrace()
      }
    }
    catch (iOException:IOException) {
      Log.w(TAG, iOException)
      this.mediaCodec.stop()
      this.audioRecord.stop()
      this.mediaCodec.release()
      this.audioRecord.release()
      this.outputStream.close()
    }
    finally
    {}
  }
  internal interface OnRecorderFailedListener {
    fun onRecorderFailed()
    fun onRecorderStarted()
  }
  companion object {
    private val BIT_RATE = 32000
    private val CHANNELS = 1
    private val SAMPLE_RATE = 44100
    private val SAMPLE_RATE_INDEX = 4
    private val TAG = AudioRecordThread::class.java.getSimpleName()
  }
}