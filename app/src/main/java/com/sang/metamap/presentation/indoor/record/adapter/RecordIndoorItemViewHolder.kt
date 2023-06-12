package com.sang.metamap.presentation.indoor.record.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sang.metamap.R
import com.sang.metamap.databinding.ItemRecordIndoorBinding
import com.sang.metamap.domain.model.FirebaseRecordItem
import com.sang.metamap.domain.model.RecordIndoorItem
import com.sang.metamap.domain.model.RecordIndoorItemType
import com.sang.metamap.domain.model.TurnDirection

class RecordIndoorItemViewHolder(private val binding: ItemRecordIndoorBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(recordItem: RecordIndoorItem) {
        with(binding) {
            ivIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    when (recordItem.type) {
                        RecordIndoorItemType.START -> R.drawable.round_home_24
                        RecordIndoorItemType.IN_RECORD -> {
                            when (recordItem.turnDirection) {
                                TurnDirection.LEFT -> R.drawable.round_turn_left_24
                                TurnDirection.RIGHT -> R.drawable.round_turn_right_24
                                TurnDirection.NA -> R.drawable.twotone_circle_24
                            }
                        }

                        RecordIndoorItemType.END -> R.drawable.round_location_on_24
                    }
                )
            )

            tvContentMain.text = recordItem.mainContent
            tvContentDetail.text = recordItem.detailContent
            if (recordItem.imageBitmap != null) {
                Glide.with(ivPreviewImage).load(recordItem.imageBitmap)
                    .placeholder(R.drawable.placeholder).into(ivPreviewImage)
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): RecordIndoorItemViewHolder = RecordIndoorItemViewHolder(
            ItemRecordIndoorBinding.inflate(LayoutInflater.from(parent.context))
        )
    }
}
