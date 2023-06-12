package com.sang.metamap.presentation.indoor.record

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.sang.metamap.R
import com.sang.metamap.databinding.FragmentRecordIndoorBinding
import com.sang.metamap.databinding.PopupConfirmSubmitRecordBinding
import com.sang.metamap.domain.model.FirebaseRecordItem
import com.sang.metamap.domain.model.RecordIndoorItem
import com.sang.metamap.domain.model.RecordIndoorItemType
import com.sang.metamap.domain.model.TurnDirection
import com.sang.metamap.domain.sensor.RotationDetector
import com.sang.metamap.domain.sensor.StepCounter
import com.sang.metamap.presentation.MetaMapFragment
import com.sang.metamap.presentation.indoor.record.adapter.RecordIndoorRecyclerViewAdapter
import com.sang.metamap.presentation.viewmodel.MainViewModel
import com.sang.metamap.utils.Constant
import com.sang.metamap.utils.SystemBarUtil
import com.sang.metamap.utils.ToastUtil
import com.sang.metamap.utils.ToastUtil.showMess
import com.sang.metamap.utils.ViewUtil.fadeIn
import com.sang.metamap.utils.ViewUtil.fadeInUp
import com.sang.metamap.utils.ViewUtil.fadeOut
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 100

class RecordIndoorFragment : MetaMapFragment(), View.OnClickListener {

    private lateinit var binding: FragmentRecordIndoorBinding
    private lateinit var recordIndoorListAdapter: RecordIndoorRecyclerViewAdapter
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var stepCounter: StepCounter? = null
    private var rotationDetector: RotationDetector? = null
    private var isRecording = false
    private var addedStartRecordItem = false
    private var preTurnDirection = TurnDirection.NA
    private var recordingItemImage: Bitmap? = null

    private var popupSubmitConfirmBinding: PopupConfirmSubmitRecordBinding? = null
    private var popupSubmitConfirmWindow: PopupWindow? = null
    private val viewModel by activityViewModels<MainViewModel>()
    private var pathFolder = ""
    private var totalStepCountedForThePath = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerCameraLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordIndoorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI()
        checkValidPermission()
        setupSensors()
    }

    private fun registerCameraLauncher() {
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result?.data
            try {
//                binding.iclItemPreviewRecordIndoor.ivPreviewImage.setImageBitmap(recordingItemImage)
//                Glide.with(this).load(recordingItemImage).placeholder(R.drawable.placeholder)
//                    .into(binding.iclItemPreviewRecordIndoor.ivPreviewImage)

                Glide.with(this)
                    .asBitmap()
                    .load(data?.extras?.get("data") as? Bitmap)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            // This is the scaled bitmap returned by Glide
                            // You can now use it to set the image of an ImageView or do other operations
                            recordingItemImage = resource
                            binding.iclItemPreviewRecordIndoor.ivPreviewImage.setImageBitmap(
                                resource
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // This is called when the target is cleared or has been destroyed.
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupUI() {
        binding.toolbar.apply {
            updatePadding(top = this.paddingTop + SystemBarUtil.getStatusBarHeight())
        }
        binding.apply {
            with(iclItemPreviewRecordIndoor) {
                ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.round_home_24
                    )
                )
                tvContentMain.text = "From start location"
                tvPreStep.text = "You've taken"
                tvStepsPerformed.text = "0"
                tvPostStep.text = "step"
            }
            RecordIndoorRecyclerViewAdapter().also {
                recordIndoorListAdapter = it
                rvRecordResult.adapter = recordIndoorListAdapter
            }
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
            btnRecord.setOnClickListener(this@RecordIndoorFragment)
            btnSubmitRecord.setOnClickListener(this@RecordIndoorFragment)
            btnTakePhoto.setOnClickListener(this@RecordIndoorFragment)
        }
    }

    private fun setupSensors() {
        val sensorManager =
            requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = StepCounter(sensorManager, createStepCounterListener())
        rotationDetector = RotationDetector(sensorManager, createRotationListener())
    }

    private fun checkValidPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!checkPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)) {
                requestPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            if (!checkPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")) {
                requestPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
            }
        }
    }

    private fun createRotationListener(): RotationDetector.Listener =
        object : RotationDetector.Listener {
            override fun onDirectionChanged(turnDirection: TurnDirection) {
                addInRecordItem(preTurnDirection)
                stepCounter?.apply {
                    shouldResetStepWhenStart = true
                    stepNow = 0
                }

                with(binding) {
                    if (addedStartRecordItem) {
                        with(iclItemPreviewRecordIndoor) {
                            tvStepsPerformed.text = "0"
                            tvPreStep.text = "And walk"
                            tvPostStep.text = "step"
                            when (turnDirection) {
                                TurnDirection.LEFT -> {
                                    ivIcon.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            requireContext(),
                                            R.drawable.round_turn_left_24
                                        )
                                    )
                                    tvContentMain.text = Constant.MAIN_CONTENT_JUST_TURN_LEFT
                                }

                                TurnDirection.RIGHT -> {
                                    ivIcon.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            requireContext(),
                                            R.drawable.round_turn_right_24
                                        )
                                    )
                                    tvContentMain.text = Constant.MAIN_CONTENT_JUST_TURN_RIGHT
                                }

                                else -> {}
                            }
                        }
                    }
                }
                preTurnDirection = turnDirection
            }

            override fun onRotationDetectorWannaSay(message: String) {
                showMess(requireContext().applicationContext, message)
            }
        }

    override fun onClick(v: View) {
        when (v) {
            binding.btnRecord -> handleOnClickRecordBtn()
            binding.btnSubmitRecord -> {
                if (recordIndoorListAdapter.recordList.size <= 1) {
                    showMess(
                        requireContext().applicationContext,
                        "This record list is too short to submit"
                    )
                    return
                }
                showConfirmPopup()
            }

            binding.btnTakePhoto -> takeImage()

            popupSubmitConfirmBinding?.cancelButton -> {
                stepCounter?.start()
                rotationDetector?.start()
                popupSubmitConfirmWindow?.dismiss()
            }

            popupSubmitConfirmBinding?.submitButton -> {
                stepCounter?.stop()
                rotationDetector?.stop()
                submitRecordListToFirebase()
            }
        }
    }

    private fun submitRecordListToFirebase() {
        lifecycleScope.launch {
            popupSubmitConfirmWindow?.dismiss()
            with(binding) {
                iclItemPreviewRecordIndoor.root.fadeOut()
                btnRecord.fadeOut()
                btnTakePhoto.fadeOut()
                clOverlay.fadeIn()
            }
            addLastRecordItemBeforeSubmit()
            recordIndoorListAdapter.recordList.uploadToFirebase()
        }
    }

    private fun showConfirmPopup() {
        stepCounter?.stop()
        rotationDetector?.stop()
        popupSubmitConfirmBinding =
            PopupConfirmSubmitRecordBinding.inflate(
                LayoutInflater.from(this.context),
                binding.root,
                false
            )
        with(popupSubmitConfirmBinding!!) {
            cancelButton.setOnClickListener(this@RecordIndoorFragment)
            submitButton.setOnClickListener(this@RecordIndoorFragment)
        }
        popupSubmitConfirmWindow = initPopupWindow(popupSubmitConfirmBinding!!).apply {
            showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        }
    }

    private fun addLastRecordItemBeforeSubmit() {
        RecordIndoorItem(
            mainContent = getMainContentLastItem(binding.iclItemPreviewRecordIndoor.tvContentMain.text as String),
            detailContent = String.format(
                "%s %s %s",
                binding.iclItemPreviewRecordIndoor.tvPreStep.text,
                binding.iclItemPreviewRecordIndoor.tvStepsPerformed.text,
                binding.iclItemPreviewRecordIndoor.tvPostStep.text,
            ),
            imageBitmap = recordingItemImage
        ).also {
            with(recordIndoorListAdapter) {
                recordList.add(it)
                notifyItemInserted(recordList.size)
            }
        }
    }

    private fun getMainContentLastItem(string: String): String {
        return when (string) {
            Constant.MAIN_CONTENT_JUST_TURN_RIGHT -> "Turn right"
            Constant.MAIN_CONTENT_JUST_TURN_LEFT -> "Turn left"
            else -> ""
        }
    }

    private fun handleOnClickRecordBtn() {
        binding.btnRecord.apply {
            binding.toolbar.title = "Steps recorded"
            fadeInUp()
            if (!isRecording) {
                isRecording = true
                stepCounter?.start()
                rotationDetector?.start()
                text = "Recording"
                background.setTint(ContextCompat.getColor(context, R.color.red))
                if (!addedStartRecordItem) {
                    takeImage()
                }
            } else {
                isRecording = false
                stepCounter?.stop()
                rotationDetector?.stop()
                text = "Record"
                background.setTint(ContextCompat.getColor(context, R.color.blue))
            }
        }
    }

    private fun takeImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(permission),
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    handleOnClickRecordBtn()
                } else {
                    // Permission denied
                    parentFragmentManager.popBackStack()
                    ToastUtil.showMess(
                        requireContext().applicationContext,
                        "MetaMap needs physical activities permission"
                    )
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun createStepCounterListener(): StepCounter.Listener =
        object : StepCounter.Listener {
            override fun onStepChange(stepCounted: Int) {
                binding.iclItemPreviewRecordIndoor.tvStepsPerformed.apply {
                    text = stepCounted.toString()
                    fadeInUp()
                    totalStepCountedForThePath++
                }
                if (stepCounted > 1) {
                    if (binding.iclItemPreviewRecordIndoor.tvPostStep.text != "Steps") {
                        binding.iclItemPreviewRecordIndoor.tvPostStep.text = "Steps"
                    }
                    binding.btnRecord.text = "Keep going"
                }
            }

            override fun onStepSensorNotAvailable() {
                context?.let { ToastUtil.showMess(it, "Step counter sensor not available") }
            }

            override fun onStepCounterWannaSay(message: String) {
                ToastUtil.showMess(requireContext().applicationContext, message)
            }
        }

    private fun addInRecordItem(turnDirection: TurnDirection) {
        if (!addedStartRecordItem) {
            addStartRecordItem()
            addedStartRecordItem = true
        }
        if (preTurnDirection != TurnDirection.NA) {
            val mainContentString = when (turnDirection) {
                TurnDirection.LEFT -> requireContext().getString(R.string.record_item_in_record_turn_left_main_content)
                TurnDirection.RIGHT -> requireContext().getString(R.string.record_item_in_record_turn_right_main_content)
                else -> ""
            }
            val detailContentString = when (stepCounter?.stepNow) {
                in 0..1 -> {
                    requireContext().getString(
                        R.string.record_item_in_record_detail_content_one,
                        stepCounter?.stepNow
                    )
                }

                else -> {
                    requireContext().getString(
                        R.string.record_item_in_record_detail_content_others,
                        stepCounter?.stepNow
                    )
                }
            }
            RecordIndoorItem(
                mainContent = mainContentString,
                detailContent = detailContentString,
                imageBitmap = recordingItemImage,
                type = RecordIndoorItemType.IN_RECORD,
                turnDirection = turnDirection,
            ).also {
                recordIndoorListAdapter.apply {
                    recordList.add(it)
                    notifyItemInserted(recordList.size)
                    binding.rvRecordResult.layoutManager?.scrollToPosition(recordList.size - 1)
                }
            }
        }
        recordingItemImage = null
        binding.iclItemPreviewRecordIndoor.ivPreviewImage.setImageDrawable(null)
        binding.iclItemPreviewRecordIndoor.root.fadeInUp()
    }

    private fun addStartRecordItem() {
        val detailContentString = when (stepCounter?.stepNow) {
            in 0..1 -> {
                requireContext().getString(
                    R.string.record_item_start_location_detail_content_one,
                    stepCounter?.stepNow
                )
            }

            else -> {
                requireContext().getString(
                    R.string.record_item_start_location_detail_content_others,
                    stepCounter?.stepNow
                )
            }
        }
        RecordIndoorItem(
            mainContent = requireContext().getString(R.string.record_item_start_location_main_content),
            detailContent = detailContentString,
            imageBitmap = recordingItemImage,
            type = RecordIndoorItemType.START
        ).also {
            recordIndoorListAdapter.apply {
                recordList.add(it)
                notifyItemInserted(0)
            }
        }
    }

    private suspend fun List<RecordIndoorItem>.uploadToFirebase() {
        val numRecordImageUploaded = AtomicInteger(0)
        var numOfBitmap = 0 // The num of bitmaps need uploading
        val imageUrlMap = mutableMapOf<Int, String?>()
        val storageReference = FirebaseStorage.getInstance().reference
        pathFolder =
            "${viewModel.searchRoomOrigin.value}-${viewModel.searchRoomDestination.value}"

        for (i in this.indices) {
            if (this[i].imageBitmap != null) {
                numOfBitmap++
            }
        }

        for (i in this.indices) {
            val recordItem = this[i]
            if (recordItem.imageBitmap == null) {
                imageUrlMap[i] = null
            } else {
                val fileName = "$pathFolder-${UUID.randomUUID()}"
                val imagesRef =
                    storageReference.child(
                        "hcmut/building/${viewModel.currentBuilding.value?.buildingId ?: "unknown"}/indoorPath/$pathFolder/$fileName.jpg"
                    )
                val baos = ByteArrayOutputStream()
                recordItem.imageBitmap!!.let {
                    it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    imagesRef.putBytes(data).addOnSuccessListener { taskSnapshot ->
                        imagesRef.downloadUrl.addOnSuccessListener { uri ->
                            // Use imageUrl as needed
                            imageUrlMap[i] = uri.toString()
                            numRecordImageUploaded.set(numRecordImageUploaded.get() + 1)
                            if (numRecordImageUploaded.get() == numOfBitmap) {
                                uploadRecordListItemToFirebase(imageUrlMap)
                            }
                        }
                    }.addOnFailureListener { exception ->
                        showMess(
                            requireContext().applicationContext,
                            "An error occurred, please try again!"
                        )
                        parentFragmentManager.popBackStack()
                    }.await()
                }
            }
        }
    }

    private fun uploadRecordListItemToFirebase(imageUrlMap: MutableMap<Int, String?>) {
        val result = mutableListOf<FirebaseRecordItem>()
        for (i in recordIndoorListAdapter.recordList.indices) {
            val recordItem = recordIndoorListAdapter.recordList[i]
            result.add(
                FirebaseRecordItem(
                    mainContent = recordItem.mainContent,
                    detailContent = recordItem.detailContent,
                    imageUrl = imageUrlMap[i]
                )
            )
        }

        if (result.isNotEmpty()) {
            viewModel.uploadRecordListToFirebase(
                result,
                pathFolder,
                totalStepCountedForThePath
            ) {
                showMess(requireContext().applicationContext, "Submit success!")
                viewModel.plusOneNumOfIndoorPathCreated(FirebaseAuth.getInstance().currentUser) {
                    showMess(requireContext().applicationContext, "An error occurred!")
                }
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stepCounter?.stop()
        rotationDetector?.stop()
    }

    override fun onResume() {
        super.onResume()
        if (isRecording) {
            stepCounter?.start()
            rotationDetector?.start()
        }
    }
}