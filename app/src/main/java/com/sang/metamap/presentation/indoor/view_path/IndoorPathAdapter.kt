package com.sang.metamap.presentation.indoor.view_path

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sang.metamap.databinding.ItemRecordIndoorBinding
import com.sang.metamap.domain.model.FirebaseRecordItem

class IndoorPathAdapter(
    private val itemList: List<FirebaseRecordItem>
) : RecyclerView.Adapter<FirebaseRecordItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FirebaseRecordItemViewHolder {
        return FirebaseRecordItemViewHolder(
            ItemRecordIndoorBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: FirebaseRecordItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }
}
