package com.sang.metamap.presentation.indoor.view_path

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sang.metamap.R
import com.sang.metamap.databinding.ItemRecordIndoorBinding
import com.sang.metamap.domain.model.FirebaseRecordItem

class FirebaseRecordItemViewHolder(private val binding: ItemRecordIndoorBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(firebaseRecordItem: FirebaseRecordItem) {
        binding.ivIcon.setImageDrawable(
            if (firebaseRecordItem.mainContent.contains("left")) {
                ContextCompat.getDrawable(binding.root.context, R.drawable.round_turn_left_24)
            } else if (firebaseRecordItem.mainContent.contains("right")) {
                ContextCompat.getDrawable(binding.root.context, R.drawable.round_turn_right_24)
            } else {
                ContextCompat.getDrawable(binding.root.context, R.drawable.round_home_24)
            }
        )
        binding.tvContentMain.text = firebaseRecordItem.mainContent
        binding.tvContentDetail.text = firebaseRecordItem.detailContent
        firebaseRecordItem.imageUrl?.let {
            Glide.with(binding.ivPreviewImage).load(firebaseRecordItem.imageUrl).placeholder(
                ContextCompat.getDrawable(binding.root.context, R.drawable.placeholder)
            ).into(binding.ivPreviewImage)
        }
    }
}