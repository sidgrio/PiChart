package com.example.sidbola.piechart

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.pichart.PiData
import com.example.pichart.PiTestMessage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test_button.setOnClickListener {
            PiTestMessage().toastThis(this, "hi")
        }

        val data = PiData()
        data.add("Sid", 18.0, "#4286f4")
        data.add("Nick", 4.0, "#44a837")
        data.add("Nick", 6.0, "#44a837")
        data.add("Dave", 10.0, "#8e4f1c")

        pie_chart.setData(data)
    }

}
