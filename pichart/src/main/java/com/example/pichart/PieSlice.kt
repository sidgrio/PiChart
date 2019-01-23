package com.example.pichart

import android.graphics.Paint
import android.graphics.PointF

// Model for a single Pie Slice
data class PieSlice(
    val name: String,
    val paint: Paint,
    var hours: Double,
    var startAngle: Float,
    var sweepAngle: Float,
    var indicatorCircle: PointF,
    var deliverable: String?,
    var clientName: String?,
    var projectName: String?,
    var role: String
)