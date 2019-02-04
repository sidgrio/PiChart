package com.example.sidbola.piechart

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
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

        val view = TextView(this)
        view.text = "Hellos"
        view.gravity = Gravity.CENTER

        pie_chart.setData(data)
        val adapter = PeopleChartDetailsAdapter(data.pieSlices)
        pie_chart.setAdapter(adapter)
    }
}

data class Person(
    val name: String,
    val value: Double,
    val color: String
)


open class Animal {}

class Dog : Animal() {}

class Other {
    fun something(animal: Animal) {
        val something = 5
    }

    init {
        something(Dog())
    }
}