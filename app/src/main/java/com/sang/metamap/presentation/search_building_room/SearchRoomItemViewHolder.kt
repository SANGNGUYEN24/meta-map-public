package com.sang.metamap.presentation.search_building_room

import androidx.recyclerview.widget.RecyclerView
import com.sang.metamap.databinding.ItemSearchRoomBinding
import com.sang.metamap.utils.Constant.INDOOR_ROUTE_MAIN_GATE_ID

class SearchRoomItemViewHolder(private val binding: ItemSearchRoomBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(roomId: String, onClickListener: SearchRoomItemListener) {
        binding.tvRoomName.text = if (roomId == INDOOR_ROUTE_MAIN_GATE_ID) "The building main gate" else String.format("Room %s", roomId)
        binding.root.setOnClickListener { onClickListener.onClick(roomId) }
    }
}