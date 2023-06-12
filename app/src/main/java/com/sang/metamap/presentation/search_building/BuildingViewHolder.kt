package com.sang.metamap.presentation.search_building

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sang.metamap.R
import com.sang.metamap.databinding.ItemSearchBuildingBinding
import com.sang.metamap.domain.model.Building

class BuildingViewHolder(
    private val binding: ItemSearchBuildingBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(building: Building, onClickListener: BuildingItemListener) {
        binding.buildingNameTv.text = building.buildingName
        binding.root.setOnClickListener { onClickListener.onClick(building) }
        Glide.with(binding.buildingPreviewIv).load(building.imageLink)
            .placeholder(R.drawable.placeholder)
            .into(binding.buildingPreviewIv)
    }
}