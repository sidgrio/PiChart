package com.example.sidbola.piechart

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.pichart.PiTestMessage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test_button.setOnClickListener {
            PiTestMessage().toastThis(this, "hi")
        }
    }
}
