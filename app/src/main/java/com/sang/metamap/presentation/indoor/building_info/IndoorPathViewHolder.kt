package com.sang.metamap.presentation.indoor.building_info

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sang.metamap.databinding.ItemIndoorPathBinding
import com.sang.metamap.domain.model.IndoorPath
import com.sang.metamap.utils.Constant

class IndoorPathViewHolder(private val binding: ItemIndoorPathBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(indoorPath: IndoorPath, clickListener: IndoorPathClickListener) {
        Glide.with(binding.tvAvartar).load(indoorPath.userPhotoUrl).into(binding.tvAvartar)
        indoorPath.pathFolder.split("-").also {
            val start = it[0]
            val end = it[1]
            if (start == Constant.INDOOR_ROUTE_MAIN_GATE_ID) {
                binding.tvRoomStart.text = String.format("From %s " , "the main gate")
            } else {
                binding.tvRoomStart.text = String.format("From room %s ", it[0])
            }
            if (end == Constant.INDOOR_ROUTE_MAIN_GATE_ID) {
                binding.tvRoomDest.text = String.format("to %s", "the main gate")
            } else {
                binding.tvRoomDest.text = String.format("to room %s", it[1])
            }

        }
        val title = "${binding.tvRoomStart.text} ${binding.tvRoomDest.text}"
        binding.root.setOnClickListener { clickListener.onClick(indoorPath, title) }
        binding.tvCreatedAt.text = String.format("Created at: %s", indoorPath.createdAt)
        binding.tvDistance.text = String.format("%s m", indoorPath.totalDistance)
    }
}

