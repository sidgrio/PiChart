package com.example.sidbola.piechart

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import com.example.pichart.PiData
import com.example.pichart.PiTestMessage
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.RelativeLayout



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

        val view = TextView(this)
        view.text = "Hellos"
        //val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        //view.layoutParams = layoutParams
        view.gravity = Gravity.CENTER

        //main_view.addView(view)

        pie_chart.setView(view)
        pie_chart.setData(data)
    }
}
