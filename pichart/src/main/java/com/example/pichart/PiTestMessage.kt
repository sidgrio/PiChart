package com.example.pichart

import android.content.Context
import android.widget.Toast

class PiTestMessage {
    fun toastThis(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}