package com.sang.metamap.presentation.indoor.building_info

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.sang.metamap.databinding.FragmentBuildingInfoBinding
import com.sang.metamap.presentation.MetaMapFragment
import com.sang.metamap.presentation.indoor.record.RecordIndoorFragment
import com.sang.metamap.presentation.indoor.view_path.IndoorPathFragment
import com.sang.metamap.presentation.search_building_room.SearchBuildingRoomFragment
import com.sang.metamap.presentation.viewmodel.MainViewModel
import com.sang.metamap.utils.Constant.INDOOR_ROUTE_MAIN_GATE_ID
import com.sang.metamap.utils.Constant.STR_FORMAT_ROOM
import com.sang.metamap.utils.ToastUtil.showMess
import kotlinx.coroutines.launch

class BuildingInfoFragment : MetaMapFragment(), View.OnClickListener {
    private lateinit var binding: FragmentBuildingInfoBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var indoorPathAdapter: IndoorPathAdapter
    private lateinit var inputImage: InputImage
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var barcodeScanner: BarcodeScanner
    private var qrCodeOutput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result?.data
            try {
                val photo = data?.extras?.get("data") as Bitmap
                inputImage = InputImage.fromBitmap(photo, 0)
                readQr(inputImage)
            } catch (e: Exception) {
                Log.d("MainActivity", "onActivityResult: " + e.message)
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result?.data
            inputImage =
                data?.data?.let { InputImage.fromFilePath(requireContext(), it) }!!
            readQr(inputImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBuildingInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        paddingStatusBar(binding)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        with(binding) {
            with(toolbar) {
                title = viewModel.currentBuilding.value?.buildingName
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }
            mbInputOrigin.text = "Start location"
            mbInputDestination.text = "Where do you want to go?"

            mbInputOrigin.setOnClickListener(this@BuildingInfoFragment)
            mbInputDestination.setOnClickListener(this@BuildingInfoFragment)
            btnCreateRoute.setOnClickListener(this@BuildingInfoFragment)
            ivScanQr.setOnClickListener(this@BuildingInfoFragment)
            indoorPathAdapter = IndoorPathAdapter(
                IndoorPathClickListener { indoorPath, title ->
                    showFragment(IndoorPathFragment(indoorPath, title))
                }
            )
            rvIndoorPaths.adapter = indoorPathAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            launch {
                viewModel.searchRoomOrigin.collect { roomId ->
                    if (roomId == INDOOR_ROUTE_MAIN_GATE_ID) {
                        binding.mbInputOrigin.text = String.format("%s", "The building main gate")
                    } else if (roomId.isNotEmpty()) {
                        binding.mbInputOrigin.text = String.format(STR_FORMAT_ROOM, roomId)
                    } else {
                        binding.mbInputOrigin.text = "Start location"
                    }
                    displayIndoorPath(roomId, viewModel.searchRoomDestination.value)
                }
            }
            launch {
                viewModel.searchRoomDestination.collect { roomId ->
                    if (roomId == INDOOR_ROUTE_MAIN_GATE_ID) {
                        binding.mbInputDestination.text =
                            String.format("%s", "The building main gate")
                    } else if (roomId.isNotEmpty()) {
                        binding.mbInputDestination.text = String.format(STR_FORMAT_ROOM, roomId)
                    } else {
                        binding.mbInputDestination.text = "Where do you want to go?"
                    }
                    displayIndoorPath(viewModel.searchRoomOrigin.value, roomId)
                }
            }
        }
    }

    private suspend fun displayIndoorPath(roomStartId: String, roomDest: String) {
        val indoorPaths = viewModel.getIndoorPathList(roomStartId, roomDest)
        indoorPathAdapter.indoorPathList.apply {
            clear()
            addAll(indoorPaths)
        }
        binding.rvIndoorPaths.adapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View) {
        when (v) {
            binding.mbInputOrigin -> {
                viewModel.isChooseOriginRoom = true
                showFragment(SearchBuildingRoomFragment())
            }

            binding.mbInputDestination -> {
                viewModel.isChooseOriginRoom = false
                showFragment(SearchBuildingRoomFragment())
            }

            binding.btnCreateRoute -> {
                if (viewModel.searchRoomOrigin.value.isEmpty() || viewModel.searchRoomDestination.value.isEmpty()) {
                    showMess(
                        requireContext().applicationContext,
                        "You need to fill your starting location and destination"
                    )
                    return
                }
                if (FirebaseAuth.getInstance().currentUser == null) {
                    showMess(
                        requireContext().applicationContext,
                        "You need to sign in to create an indoor route"
                    )
                    return
                }
                binding.btnCreateRoute.isEnabled = false
                viewModel.canAddIndoorPath(
                    FirebaseAuth.getInstance().currentUser,
                    canAddIndoorPath = { canAdd, maxPath ->
                        if (canAdd) {
                            showFragment(RecordIndoorFragment())
                        } else {
                            showMess(
                                requireContext().applicationContext,
                                "You can create maximum $maxPath indoor paths"
                            )
                        }
                    }
                ) {
                    showMess(requireContext().applicationContext, "An error occurred")
                }
                binding.btnCreateRoute.isEnabled = false
            }

            binding.ivScanQr -> {
                // Handle alert dialog
                val options = arrayOf("Use camera", "Photo from gallery")
                val dialog =
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        androidx.appcompat.R.style.AlertDialog_AppCompat
                    )
                dialog.setTitle("Pick an option")
                    .setItems(options) { _, which ->
                        if (which == 0) {
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraLauncher.launch(cameraIntent)
                        } else {
                            val storageIntent = Intent()
                            storageIntent.type = "image/*"
                            storageIntent.action = Intent.ACTION_GET_CONTENT
                            galleryLauncher.launch(storageIntent)
                        }
                    }.create().show()
            }
        }
    }

    private fun readQr(inputImage: InputImage) {
        barcodeScanner = BarcodeScanning.getClient()
        Log.d("MetaMap barcodeScanner", barcodeScanner.toString())
        barcodeScanner.process(inputImage).addOnSuccessListener {
            // handle success list
            for (barcode: Barcode in it) {
                Log.d("MetaMap raw value", barcode.rawValue.toString())
                val data = barcode.rawValue
                if (data != null) {
                    qrCodeOutput = "$data"
                    // hcmut-a4-402
                    if (qrCodeOutput.isNotEmpty()) {
                        val parts = qrCodeOutput.split("-")
                        if (parts.size == 3) {
                            lifecycleScope.launch {
                                viewModel.setSearchRoomOriginId(parts[parts.size - 1])
                            }
                        }
                    }
                } else {
                    showMess(requireContext().applicationContext, "Invalid QR, please try again!")
                }
            }
        }.addOnFailureListener {
            showMess(requireContext().applicationContext, "Invalid QR, please try again!")
        }
    }
}
