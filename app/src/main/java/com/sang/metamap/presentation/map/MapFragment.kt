package com.sang.metamap.presentation.map

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.sang.metamap.R
import com.sang.metamap.databinding.FragmentMapBinding
import com.sang.metamap.databinding.SettingPopupBinding
import com.sang.metamap.domain.model.Building
import com.sang.metamap.domain.model.getCoordinate
import com.sang.metamap.presentation.MetaMapFragment
import com.sang.metamap.presentation.indoor.building_info.BuildingInfoFragment
import com.sang.metamap.presentation.search_building.SearchBuildingFragment
import com.sang.metamap.presentation.viewmodel.MainViewModel
import com.sang.metamap.utils.Constant.BOTTOM_SHEET_PEEK_HEIGHT
import com.sang.metamap.utils.Constant.HCMUT_MAP_NORTH_EAST_POINT
import com.sang.metamap.utils.Constant.HCMUT_MAP_SOUTH_WEST_POINT
import com.sang.metamap.utils.Constant.SHORT_ANIM_TIME
import com.sang.metamap.utils.StringUtil.addParentheses
import com.sang.metamap.utils.SystemBarUtil
import com.sang.metamap.utils.TimeUtil.toEstimatedDistance
import com.sang.metamap.utils.TimeUtil.toEstimatedTime
import com.sang.metamap.utils.ToastUtil
import com.sang.metamap.utils.ViewUtil.fadeIn
import com.sang.metamap.utils.ViewUtil.fadeInDown
import com.sang.metamap.utils.ViewUtil.fadeInUp
import com.sang.metamap.utils.ViewUtil.fadeOut
import com.sang.metamap.utils.ViewUtil.fadeOutDown
import com.sang.metamap.utils.ViewUtil.gone
import com.sang.metamap.utils.ViewUtil.visible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

const val ROUTE_LAYER_ID = "route-layer-id"
const val ROUTE_SOURCE_ID = "route-source-id"

const val ICON_SOURCE_LAYER_ID = "ICON_SOURCE_LAYER_ID"
const val ICON_SOURCE_POINT_ID = "ICON_SOURCE_ID"
const val ICON_SOURCE_CIRCLE_PIN_ID = "ICON_SOURCE_CIRCLE_PIN_ID"

const val ICON_DEST_LAYER_ID = "ICON_DEST_LAYER_ID"
const val ICON_DEST_POINT_ID = "ICON_DEST_ID"
const val ICON_DEST_RED_PIN_ID = "ICON_DEST_RED_PIN_ID"

class MapFragment : MetaMapFragment(), OnMapReadyCallback, PermissionsListener,
    View.OnClickListener {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxDirections: MapboxDirections
    private lateinit var currentRoute: DirectionsRoute

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var binding: FragmentMapBinding
    private var popupSetting: SettingPopupBinding? = null
    private var windowSetting: PopupWindow? = null

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            Log.d("MapFragment", "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                //Google Signin success, now auth with firebase
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                // Failed Google Signin
                Log.d("MapFragment", "onActivityResult: s(e.message}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment)
        }
        updateSigningInfo()
    }

    private fun updateSigningInfo() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            Glide.with(this@MapFragment).load(firebaseUser.photoUrl)
                .into(binding.included.ivUserProfile)
            popupSetting?.apply {
                ivAppInfoSignOutIcon.visible()
                tvAppInfoLogOutDesc.visible()
                with(tvUserEmail) {
                    visible()
                    text = firebaseUser.email
                }
                if (pbSigingGoogle.isVisible) {
                    pbSigingGoogle.fadeOut()
                }
                Glide.with(this@MapFragment).load(firebaseUser.photoUrl)
                    .into(ivUserAvatar)
                tvUserName.text = firebaseUser.displayName
            }
        } else {
            binding.included.ivUserProfile.setImageResource(R.drawable.placeholder)
            popupSetting?.apply {
                ivUserAvatar.setImageResource(R.drawable.placeholder)
                tvUserName.text = resources.getString(R.string.sign_in_with_google)
                tvUserEmail.fadeOut()
                ivAppInfoSignOutIcon.fadeOut()
                tvAppInfoLogOutDesc.fadeOut()
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        val bounds = LatLngBounds.Builder()
            .include(HCMUT_MAP_SOUTH_WEST_POINT)
            .include(HCMUT_MAP_NORTH_EAST_POINT)
            .build()
        mapboxMap.apply {
            adjustMapBoxUI(mapboxMap)
            setLatLngBoundsForCameraTarget(bounds)
            setStyle(Style.MAPBOX_STREETS) { style ->
                binding.fabMyLocation.setOnClickListener {
                    enableLocationComponent(style)
                }

                val origin = mainViewModel.startPoint.value
                val destination = mainViewModel.currentBuilding.value?.getCoordinate()

                destination?.let {
                    if (!mainViewModel.isSearchRoute.value) {
                        setCameraToPoint(destination, style)
                    } else {
                        origin?.let {
                            if (origin != destination) {
                                getRoute(mapboxMap, style, origin, destination)
                            } else {
                                setCameraToPoint(destination, style)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setCameraToPoint(destination: Point, style: Style) {
        zoomCameraToPoint(destination)
        displayBuildingMarker(style, destination)
        mainViewModel.currentBuilding.value?.let {
            showBuildingDetailBottomSheet(it)
        }
    }

    private fun adjustMapBoxUI(mapboxMap: MapboxMap) {
        mapboxMap.uiSettings.apply {
            setCompassMargins(
                this.compassMarginLeft,
                SystemBarUtil.getStatusBarHeight() + 180,
                resources.getDimensionPixelSize(R.dimen.padding_side_parent),
                this.compassMarginBottom
            )
            setCompassFadeFacingNorth(true)
        }
    }

    private fun zoomCameraToPoint(point: Point) {
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(point.latitude(), point.longitude()))
            .zoom(18.0)
            .build()
        mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            1000
        )
    }

    private fun zoomCameraToRoute(startPoint: Point, destinationPoint: Point) {
        val routeBounds = LatLngBounds.Builder()
            .include(
                LatLng(
                    startPoint.latitude(),
                    startPoint.longitude()
                )
            ) // southwest corner
            .include(
                LatLng(
                    destinationPoint.latitude(),
                    destinationPoint.longitude()
                )
            ) // northeast corner
            .build()
        val newPosition = CameraPosition.Builder(mapboxMap.cameraPosition)
            .target(routeBounds.center)
            .zoom(mapboxMap.getCameraForLatLngBounds(routeBounds)!!.zoom - 0.8)
            .build()
        mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                newPosition
            ), 1000
        )
    }

    private fun showBuildingDetailBottomSheet(building: Building) {
        binding.clBottomSheetBuildingDetail.tvBuildingName.text = building.buildingName
        binding.clBottomSheetBuildingDetail.ivBuildingPhoto.apply {
            Glide.with(this.context).load(building.imageLink)
                .placeholder(R.drawable.placeholder)
                .into(this)
        }
        binding.clBottomSheetBuildingDetail.tvBuildingDetail.text = building.desc

        val bottomSheetBehavior =
            BottomSheetBehavior.from(binding.clBottomSheetBuildingDetail.root).apply {
                peekHeight = BOTTOM_SHEET_PEEK_HEIGHT
                state = BottomSheetBehavior.STATE_COLLAPSED
            }

        binding.clBottomSheetBuildingDetail.svContents.apply {
            setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> bottomSheetBehavior.setDraggable(false)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> bottomSheetBehavior.setDraggable(
                            true
                        )
                    }
                    return false
                }
            })
        }

        binding.clBottomSheetBuildingDetail.root.fadeInUp()
    }

    private fun showRouteDetailBottomSheet(route: DirectionsRoute) {
        val currentBuilding = mainViewModel.currentBuilding.value
        currentBuilding?.let {
            binding.clBottomSheetRouteDetail.apply {
                tvEstimatedTime.text = route.duration().toEstimatedTime()
                tvEstimatedDistance.text = route.distance().toEstimatedDistance().addParentheses()
                if (currentBuilding.enableIndoorFeature) {
                    with(btExploreBuilding) {
                        visible()
                        text = String.format(
                            "Explore %s",
                            mainViewModel.currentBuilding.value?.buildingName
                        )
                        setOnClickListener(this@MapFragment)
                    }
                }
            }
            binding.clBottomSheetRouteDetail.root.doOnLayout {
                BottomSheetBehavior.from(binding.clBottomSheetRouteDetail.root).apply {
                    peekHeight =
                        binding.clBottomSheetRouteDetail.llEstimateInfo.height + binding.clBottomSheetRouteDetail.btExploreBuilding.height + 48
                    isDraggable = false
                }

                binding.fabMyLocation.apply {
                    updatePadding(
                        bottom = paddingBottom + binding.clBottomSheetRouteDetail.root.height
                    )
                }
            }
            binding.clBottomSheetRouteDetail.root.visible()
        }
    }

    private fun displayBuildingMarker(loadedMapStyle: Style, point: Point) {
        val iconGeoJsonSource = GeoJsonSource(
            ICON_SOURCE_POINT_ID,
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            point.longitude(),
                            point.latitude()
                        )
                    )
                )
            )
        )

        loadedMapStyle.addSource(iconGeoJsonSource)

        loadedMapStyle.addImage(
            ICON_DEST_RED_PIN_ID,
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    requireActivity().applicationContext,
                    R.drawable.round_location_on_24
                )
            )!!
        )

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(
            SymbolLayer(
                ICON_SOURCE_LAYER_ID,
                ICON_SOURCE_POINT_ID
            ).withProperties(
                PropertyFactory.iconImage(ICON_DEST_RED_PIN_ID),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(arrayOf(0f, -9f))
            )
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            requireActivity(),
            "HCMUT Map can't tell you where you are without your permission",
            Toast.LENGTH_LONG
        )
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(
                requireActivity(),
                "Grant location permission to know where you are",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireActivity())) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(requireActivity())
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(requireActivity(), R.color.blue))
                .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(requireActivity(), loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(requireActivity())
        }
    }

    private fun displayMarkerForRoute(
        loadedMapStyle: Style,
        startPoint: Point,
        destinationPoint: Point
    ) {
        val iconGeoJsonSourceStart = GeoJsonSource(
            ICON_SOURCE_POINT_ID,
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            startPoint.longitude(),
                            startPoint.latitude()
                        )
                    )
                )
            )
        )

        val iconGeoJsonSourceDest = GeoJsonSource(
            ICON_DEST_POINT_ID,
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            destinationPoint.longitude(),
                            destinationPoint.latitude()
                        )
                    )
                )
            )
        )
        loadedMapStyle.addSource(iconGeoJsonSourceStart)
        loadedMapStyle.addSource(iconGeoJsonSourceDest)

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(
            ICON_DEST_RED_PIN_ID,
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    requireActivity().applicationContext,
                    R.drawable.round_location_on_24
                )
            )!!
        )
        loadedMapStyle.addImage(
            ICON_SOURCE_CIRCLE_PIN_ID,
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    requireActivity().applicationContext,
                    R.drawable.twotone_circle_24
                )
            )!!
        )

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(
            SymbolLayer(
                ICON_SOURCE_LAYER_ID,
                ICON_SOURCE_POINT_ID
            ).withProperties(
                PropertyFactory.iconImage(ICON_SOURCE_CIRCLE_PIN_ID),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(arrayOf(0f, -9f))
            )
        )

        loadedMapStyle.addLayer(
            SymbolLayer(
                ICON_DEST_LAYER_ID,
                ICON_DEST_POINT_ID
            ).withProperties(
                PropertyFactory.iconImage(ICON_DEST_RED_PIN_ID),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(arrayOf(0f, -9f))
            )
        )
    }

    private fun displayGuideLineRoute(loadedMapStyle: Style) {
        val routeLayer = LineLayer(
            ROUTE_LAYER_ID,
            ROUTE_SOURCE_ID
        )
        loadedMapStyle.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(
                ContextCompat.getColor(
                    requireActivity().applicationContext,
                    R.color.blue
                )
            )
        )
        loadedMapStyle.addLayer(routeLayer)
    }

    private fun getRoute(
        mapboxMap: MapboxMap?,
        style: Style,
        origin: Point,
        destination: Point
    ) {
        mapboxDirections = MapboxDirections.builder()
            .accessToken(requireActivity().applicationContext.getString(R.string.mapbox_access_token))
            .routeOptions(
                RouteOptions.builder()
                    .coordinatesList(listOf(origin, destination))
                    .profile(DirectionsCriteria.PROFILE_WALKING)
                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                    .build()
            )
            .build()

        mapboxDirections.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>
            ) {
                // You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code())
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Timber.e("No routes found")
                    return
                }

                // Get the directions route
                currentRoute = response.body()!!.routes()[0]

                displayGuideLineRoute(style)
                displayMarkerForRoute(style, origin, destination)
                zoomCameraToRoute(origin, destination)
                showRouteDetailBottomSheet(currentRoute)

                mapboxMap?.getStyle { style -> // Retrieve and update the source designated for showing the directions route
                    val source =
                        style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)

                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    source?.setGeoJson(
                        currentRoute.geometry()?.let {
                            LineString.fromPolyline(
                                it,
                                Constants.PRECISION_6
                            )
                        }
                    )
                }
            }

            override fun onFailure(call: Call<DirectionsResponse?>, throwable: Throwable) {
                Timber.e("Error: " + throwable.message)
                Toast.makeText(
                    requireActivity(), "Error: " + throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onClick(v: View) {
        when (v) {
            binding.included.searchLayout -> onClickSearchDestination()
            binding.clBottomSheetBuildingDetail.btDirection -> onClickButtonDirection()
//            binding.clBottomSheetBuildingDetail.btShare -> showFragment(RecordIndoorFragment())
            binding.clSearchDirection.ivBackButton -> onClickBackButtonOnSearchDirectionLayout()
            binding.clSearchDirection.mbInputOrigin -> onClickInputOrigin()
            binding.clBottomSheetRouteDetail.btExploreBuilding -> onExploreBuildingBtn()
            binding.included.ivUserProfile -> showSettingPopup()
            popupSetting?.llUserInfo -> {
                if (firebaseAuth.currentUser == null) {
                    requestSigningWithGoogle()
                    updateSigningInfo()
                    mainViewModel.updateFirestoreUserConfig(firebaseAuth.currentUser) {
                        ToastUtil.showMess(requireContext().applicationContext, "An error occurred")
                    }
                }
            }

            popupSetting?.tvAppInfoLogOutDesc -> {
                firebaseAuth.signOut()
                updateSigningInfo()
            }
        }
    }

    private fun requestSigningWithGoogle() {
        popupSetting?.pbSigingGoogle?.fadeIn()
        // Configure the Google SignIn
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Resolve when building
                .requestEmail() //we only need email from google account
                .build()
        // Request choose an email to sign in
        val intent = GoogleSignIn.getClient(requireActivity(), googleSignInOptions).signInIntent
        launcher.launch(intent)
    }

    private fun showSettingPopup() {
        popupSetting =
            SettingPopupBinding.inflate(LayoutInflater.from(this.context), binding.root, false)
        with(popupSetting!!) {
            llUserInfo.setOnClickListener(this@MapFragment)
            tvAppInfoLogOutDesc.setOnClickListener(this@MapFragment)
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                Glide.with(this@MapFragment).load(firebaseUser.photoUrl).into(ivUserAvatar)
                tvUserName.text = firebaseUser.displayName
                with(tvUserEmail) {
                    text = firebaseUser.email
                    visible()
                }
            }
        }
        updateSigningInfo()
        windowSetting = initPopupWindow(popupSetting!!).apply {
            showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        }
    }

    private fun onExploreBuildingBtn() {
        showFragment(BuildingInfoFragment())
    }

    private fun onClickSearchDestination() {
        mainViewModel.changeIsSearchDirection(false)
        showFragment(SearchBuildingFragment())
    }

    private fun onClickInputOrigin() {
        mainViewModel.changeIsSearchDirection(true)
        showFragment(SearchBuildingFragment())
    }

    private fun onClickBackButtonOnSearchDirectionLayout() {
        binding.clSearchDirection.root.gone()
        binding.included.searchLayout.visible()
    }

    private fun onClickButtonDirection() {
        binding.clBottomSheetBuildingDetail.root.fadeOutDown()
        binding.included.searchLayout.gone()
        binding.clSearchDirection.root.apply {
            updatePadding(top = this.paddingTop + SystemBarUtil.getStatusBarHeight())
            fadeInDown(SHORT_ANIM_TIME)
        }
        binding.clSearchDirection.mbInputDestination.text =
            mainViewModel.currentBuilding.value?.buildingName
    }

    override fun showFragment(fragment: MetaMapFragment, addToBackStack: Boolean) {
        super.showFragment(fragment, addToBackStack)
        binding.included.searchLayout.gone()
    }

    private fun setupUI() {
        binding.apply {
            with(included) {
                with(searchLayout) {
                    updatePadding(top = this.paddingTop + SystemBarUtil.getStatusBarHeight())
                    setOnClickListener(this@MapFragment)
                }
                ivUserProfile.setOnClickListener(this@MapFragment)
            }
            clBottomSheetBuildingDetail.btDirection.setOnClickListener(this@MapFragment)
            clBottomSheetBuildingDetail.btShare.setOnClickListener(this@MapFragment)
            clSearchDirection.apply {
                ivBackButton.setOnClickListener(this@MapFragment)
                mbInputOrigin.setOnClickListener(this@MapFragment)
                mbInputDestination.setOnClickListener(this@MapFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.included.searchLayout.visible()
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)

        firebaseAuth.signInWithCredential(credential).addOnSuccessListener { authResult ->
            // Update UI
            windowSetting?.dismiss()
            updateSigningInfo()
            mainViewModel.updateFirestoreUserConfig(firebaseAuth.currentUser) {
                ToastUtil.showMess(requireContext().applicationContext, "An error occurred")
            }
            Toast.makeText(
                context,
                "Welcome ${firebaseAuth.currentUser?.displayName}",
                Toast.LENGTH_SHORT
            )
                .show()
        }.addOnFailureListener { e ->
            //login failed
            Toast.makeText(context, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}