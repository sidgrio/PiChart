package com.example.pichart

import android.graphics.Paint
import android.graphics.PointF

// Model for a single Pie Slice
data class PieSlice(
    val name: String,
    var value: Double,
    var startAngle: Float,
    var sweepAngle: Float,
    var indicatorCircleLocation: PointF,
    val paint: Paint
)