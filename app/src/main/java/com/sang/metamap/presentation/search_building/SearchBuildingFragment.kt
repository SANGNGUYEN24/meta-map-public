package com.sang.metamap.presentation.search_building

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.geojson.Point
import com.sang.metamap.R
import com.sang.metamap.databinding.FragmentSearchDestinationBinding
import com.sang.metamap.domain.model.Building
import com.sang.metamap.presentation.MetaMapFragment
import com.sang.metamap.presentation.viewmodel.MainViewModel
import com.sang.metamap.utils.Constant.DEFAULT_LATITUDE
import com.sang.metamap.utils.Constant.DEFAULT_LONGITUDE
import com.sang.metamap.utils.SystemBarUtil
import com.sang.metamap.utils.ViewUtil.fadeOut
import com.sang.metamap.utils.ViewUtil.visible
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch

class SearchBuildingFragment : MetaMapFragment() {

    private lateinit var binding: FragmentSearchDestinationBinding

    private val viewModel: MainViewModel by activityViewModels()

    private var originalBuildingList = emptyList<Building>()
    private var searchAdapter: SearchBuildingAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchDestinationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        displayHcmutBuildingList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupUI() {
        binding.included.searchLayout.apply {
            updatePadding(top = this.paddingTop + SystemBarUtil.getStatusBarHeight())
        }
        val divider = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            ?.let { divider.setDrawable(it) }
        binding.searchResultRv.apply {
            addItemDecoration(divider)
        }
        binding.included.editText.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
                searchAdapter?.buildings = originalBuildingList
                binding.searchResultRv.adapter?.notifyDataSetChanged()
            } else {
                searchAdapter?.buildings = originalBuildingList.search(text.toString())
                binding.searchResultRv.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun displayHcmutBuildingList() {
        binding.progressBar.visible()
        lifecycleScope.launch {
            viewModel.hcmutBuildings.collect {
                if (it.isNotEmpty()) {
                    originalBuildingList = it.toList()
                    binding.searchResultRv.apply {
                        searchAdapter = SearchBuildingAdapter(createOnClickSearchItem(), it)
                        adapter = searchAdapter
                        this.adapter?.notifyItemRangeInserted(0, it.size)
                        binding.progressBar.fadeOut()
                    }
                }
            }
        }
    }

    private fun popSearchFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun createOnClickSearchItem() = BuildingItemListener { building ->
        if (viewModel.isSearchRoute.value) {
            viewModel.changeStartPoint(
                Point.fromLngLat(
                    building.longitude ?: DEFAULT_LATITUDE,
                    building.latitude ?: DEFAULT_LONGITUDE
                )
            )
        } else {
            viewModel.changeCurrentBuilding(building)
            viewModel.changeDestinationPoint(
                Point.fromLngLat(
                    building.longitude ?: DEFAULT_LATITUDE,
                    building.latitude ?: DEFAULT_LONGITUDE
                )
            )
        }
        popSearchFragment()
    }
}

private fun List<Building>.search(text: String) = this.filter { it.buildingName?.lowercase()?.contains(text.lowercase()) ?: false }
