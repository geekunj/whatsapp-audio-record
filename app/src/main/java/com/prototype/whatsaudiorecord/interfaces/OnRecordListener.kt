package com.prototype.whatsaudiorecord.interfaces

interface OnRecordListener {
  fun onStart()
  fun onCancel()
  fun onFinish(recordTime:Long)
  fun onLessThanSecond()
}