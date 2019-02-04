package com.example.sidbola.piechart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pichart.PiChart
import com.example.pichart.PieSlice
import kotlinx.android.synthetic.main.view_pie_detail.view.*
import kotlin.collections.HashMap

// TODO: Move data input logic to be handled by this adapter instead of PiData object
class PeopleChartDetailsAdapter(val pieSlices: HashMap<String, PieSlice>) :
    PiChart.Adapter<PeopleChartDetailsAdapter.PeopleViewHolder>() {

    inner class PeopleViewHolder(detailView: View) : PiChart.ViewHolder(detailView) {
        val nameLabel: TextView = detailView.name
    }

    override fun createViewHolder(parent: ViewGroup): PeopleViewHolder {
        val detailsView = LayoutInflater.from(parent.context).inflate(
            R.layout.view_pie_detail,
            parent,
            false
        )
        return PeopleViewHolder(detailsView)
    }

    override fun bindPieViewHolder(viewHolder: PeopleViewHolder, key: String) {
        viewHolder.nameLabel.text = pieSlices[key]?.name
    }
}