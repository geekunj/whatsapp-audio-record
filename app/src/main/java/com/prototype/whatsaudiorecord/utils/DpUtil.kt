package com.prototype.whatsaudiorecord.utils

import android.content.Context
import android.util.DisplayMetrics

object DpUtil {
  fun toPixel(dp:Float, context: Context):Float {
    val resources = context.getResources()
    val metrics = resources.getDisplayMetrics()
    val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    return px
  }
  fun toDp(px:Float, context:Context):Float {
    val resources = context.getResources()
    val metrics = resources.getDisplayMetrics()
    val dp = px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    return dp
  }
}