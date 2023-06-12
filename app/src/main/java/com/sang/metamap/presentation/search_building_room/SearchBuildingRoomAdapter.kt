package com.sang.metamap.presentation.search_building_room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sang.metamap.databinding.ItemSearchRoomBinding

class SearchBuildingRoomAdapter(
    private val rooms: List<String>,
    private val onClickListener: SearchRoomItemListener
): RecyclerView.Adapter<SearchRoomItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRoomItemViewHolder {
        return SearchRoomItemViewHolder(
            ItemSearchRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = rooms.size

    override fun onBindViewHolder(holder: SearchRoomItemViewHolder, position: Int) {
        holder.bind(rooms[position], onClickListener)
    }
}

class SearchRoomItemListener(val clickListener: (roomName: String) -> Unit) {
    fun onClick(roomName: String) = clickListener(roomName)
}