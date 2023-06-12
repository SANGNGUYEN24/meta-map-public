package com.sang.metamap.presentation.search_building_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sang.metamap.R
import com.sang.metamap.databinding.FragmentSearchBuildingRoomBinding
import com.sang.metamap.presentation.MetaMapFragment
import com.sang.metamap.presentation.viewmodel.MainViewModel
import com.sang.metamap.utils.ToastUtil
import kotlinx.coroutines.launch

class SearchBuildingRoomFragment : MetaMapFragment() {
    private lateinit var binding: FragmentSearchBuildingRoomBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBuildingRoomBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        viewModel.getCurrentBuildingRooms {
            ToastUtil.showMess(requireContext().applicationContext, "An error occurred")
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.currentBuildingRooms.collect {
                binding.rcRoomList.adapter = SearchBuildingRoomAdapter(
                    rooms = it,
                    onClickListener = SearchRoomItemListener { roomId ->
                        if (viewModel.isChooseOriginRoom) {
                            launch {
                                viewModel.setSearchRoomOriginId(roomId)
                            }
                        } else {
                            launch {
                                viewModel.setSearchRoomDestinationId(roomId)
                            }
                        }
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

    private fun setupUI() {
        paddingStatusBar(binding)
        val divider = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            ?.let { divider.setDrawable(it) }
        binding.rcRoomList.apply {
            addItemDecoration(divider)
        }
    }
}