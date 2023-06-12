package com.sang.metamap.presentation.search_building

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sang.metamap.databinding.ItemSearchBuildingBinding
import com.sang.metamap.domain.model.Building

class SearchBuildingAdapter(
    private val onClickListener: BuildingItemListener,
    var buildings: List<Building>
) : RecyclerView.Adapter<BuildingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        return BuildingViewHolder(
            ItemSearchBuildingBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun getItemCount(): Int = buildings.size

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        holder.bind(buildings[position], onClickListener)
    }
}

class BuildingItemListener(val clickListener: (building: Building) -> Unit) {
    fun onClick(building: Building) = clickListener(building)
}