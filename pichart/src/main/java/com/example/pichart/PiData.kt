package com.example.pichart

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class PiData {
    val pieSlices = HashMap<String, PieSlice>()
    var totalValue = 0.0

    fun add (name: String, value: Double, color: String?){
        if (pieSlices.containsKey(name)) {
            pieSlices[name]?.let {
                it.value += value
            }
        } else {
            color?.let {
                pieSlices[name] = PieSlice(name, value, 0f, 0f, PointF(), createPaint(it))
            } ?: run {
                //TODO: add random assignment of paints
                pieSlices[name] = PieSlice(name, value, 0f,0f, PointF(), createPaint("#000000"))
            }
        }
        totalValue += value
    }

    /**
     * Dynamically create paints for a given project
     *
     * @param color the color of the paint to create
     */
    private fun createPaint(color: String): Paint {
        val newPaint = Paint()
        newPaint.color = Color.parseColor(color)
        newPaint.isAntiAlias = true
        return newPaint
    }


}