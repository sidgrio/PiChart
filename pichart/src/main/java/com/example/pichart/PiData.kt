package com.example.pichart

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import java.lang.IllegalArgumentException
import java.util.*

class PiData {
    var pieSlices = HashMap<String, PieSlice>()
    var totalValue = 0.0

    /**
     * Adds data to the pieSlices hashmap
     *
     * @param name the name of the item being added
     * @param value the value of the item being added
     * @param color the color the item should be represented as (if not already in the map)
     */
    fun add(name: String, value: Double, color: String? = null) {
        if (pieSlices.containsKey(name)) {
            pieSlices[name]?.let {
                it.value += value
            }
        } else {
            color?.let {
                pieSlices[name] = PieSlice(name, value, 0f, 0f, PointF(), createPaint(it))
            } ?: run {
                pieSlices[name] = PieSlice(name, value, 0f, 0f, PointF(), createPaint(null))
            }
        }
        totalValue += value
    }

    /**
     * Dynamically create paints for a given project
     * If no color is passed, we assign a random color
     *
     * @param color the color of the paint to create
     */
    private fun createPaint(color: String?): Paint {
        val newPaint = Paint()
        color?.let {
            newPaint.color = Color.parseColor(color)
        } ?: run {
            val randomValue = Random()
            newPaint.color = Color.argb(255, randomValue.nextInt(255),
                randomValue.nextInt(255), randomValue.nextInt(255))
        }
        newPaint.isAntiAlias = true
        return newPaint
    }
}