package com.sang.metamap.presentation.indoor.record

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sang.metamap.databinding.FragmentViewFullPhotoBinding
import com.sang.metamap.presentation.MetaMapFragment

class ViewFullPhotoFragment(private val imageBitmap: Bitmap): MetaMapFragment() {

    private lateinit var binding: FragmentViewFullPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewFullPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.pvPhoto.setImageBitmap(imageBitmap)
    }
}