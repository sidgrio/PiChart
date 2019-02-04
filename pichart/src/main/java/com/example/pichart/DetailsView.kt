package com.example.pichart

import android.view.View
import android.view.ViewGroup

abstract class DetailsView {
    abstract class Adapter<PVH: DetailsView.ViewHolder> {
        abstract fun createViewHolder(parent: ViewGroup): PVH
        abstract fun bindPieViewHolder(viewHolder: PVH, key: String)
    }

    abstract class ViewHolder(val detailView: View) {
    }
}