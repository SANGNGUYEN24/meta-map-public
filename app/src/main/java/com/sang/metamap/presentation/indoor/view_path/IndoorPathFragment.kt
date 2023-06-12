package com.sang.metamap.presentation.indoor.view_path

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.sang.metamap.databinding.FragmentIndoorPathBinding
import com.sang.metamap.domain.model.IndoorPath
import com.sang.metamap.presentation.MetaMapFragment
import com.sang.metamap.utils.SystemBarUtil

/**
 * Show all steps in an indoor path
 */
class IndoorPathFragment(
    private val indoorPath: IndoorPath,
    private val title: String
) : MetaMapFragment() {
    private lateinit var binding: FragmentIndoorPathBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIndoorPathBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI()
        displayIndoorPathDetails()
    }

    private fun displayIndoorPathDetails() {
        binding.rvIndoorPathDetails.adapter = IndoorPathAdapter(
            indoorPath.record ?: emptyList()
        )

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        binding.toolbar.apply {
            updatePadding(top = this.paddingTop + SystemBarUtil.getStatusBarHeight())
        }
        binding.tvTitle.text = title
    }
}
