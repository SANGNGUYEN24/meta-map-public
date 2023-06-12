package com.sang.metamap.presentation.indoor.record.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sang.metamap.domain.model.FirebaseRecordItem
import com.sang.metamap.domain.model.RecordIndoorItem

class RecordIndoorRecyclerViewAdapter : RecyclerView.Adapter<RecordIndoorItemViewHolder>() {
    val recordList = mutableListOf<RecordIndoorItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordIndoorItemViewHolder {
        return RecordIndoorItemViewHolder.create(parent)
    }

    override fun getItemCount(): Int = recordList.size

    override fun onBindViewHolder(holder: RecordIndoorItemViewHolder, position: Int) {
        holder.bind(recordList[position])
    }
}

