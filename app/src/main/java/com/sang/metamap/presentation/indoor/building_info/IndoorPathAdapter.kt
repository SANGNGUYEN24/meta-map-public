package com.sang.metamap.presentation.indoor.building_info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sang.metamap.databinding.ItemIndoorPathBinding
import com.sang.metamap.domain.model.IndoorPath

class IndoorPathAdapter(
    private val clickListener: IndoorPathClickListener
) : RecyclerView.Adapter<IndoorPathViewHolder>() {
    val indoorPathList = mutableListOf<IndoorPath>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndoorPathViewHolder {
        return IndoorPathViewHolder(
            ItemIndoorPathBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = indoorPathList.size

    override fun onBindViewHolder(holder: IndoorPathViewHolder, position: Int) {
        holder.bind(indoorPathList[position], clickListener)
    }
}

class IndoorPathClickListener(private val clickListener: (IndoorPath, String) -> Unit) {
    fun onClick(indoorPath: IndoorPath, title: String) = clickListener(indoorPath, title)
}
