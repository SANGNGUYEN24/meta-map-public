package com.sang.metamap.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mapbox.geojson.Point
import com.sang.metamap.data.impl.DatabaseImpl
import com.sang.metamap.domain.model.Building
import com.sang.metamap.domain.model.FirebaseRecordItem
import com.sang.metamap.domain.model.IndoorPath
import com.sang.metamap.domain.model.User
import com.sang.metamap.domain.repository.DatabaseRepository
import com.sang.metamap.utils.Constant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val databaseRepository: DatabaseRepository = DatabaseImpl()
) : ViewModel() {
    var isChooseOriginRoom: Boolean = false
    private val _startPoint: MutableStateFlow<Point?> =
        MutableStateFlow(Point.fromLngLat(106.65782112123814, 10.772094040691128))
    val startPoint: StateFlow<Point?> = _startPoint

    private val _destinationPoint: MutableStateFlow<Point?> = MutableStateFlow(null)
    val destinationPoint: StateFlow<Point?> = _destinationPoint

    private val _currentBuilding: MutableStateFlow<Building?> = MutableStateFlow(null)
    val currentBuilding: StateFlow<Building?> = _currentBuilding

    /**
     * Determine whether the user is wanting search route or not
     */
    private val _isSearchRoute: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSearchRoute: StateFlow<Boolean> = _isSearchRoute

    private var _hcmutBuildings: MutableStateFlow<List<Building>> =
        MutableStateFlow(emptyList())
    val hcmutBuildings = _hcmutBuildings

    private var _hadHcmutBuildings: MutableLiveData<Boolean> = MutableLiveData(false)
    val hadHcmutBuildings: LiveData<Boolean> = _hadHcmutBuildings

    private val _currentBuildingRooms: MutableStateFlow<List<String>> =
        MutableStateFlow(emptyList())
    val currentBuildingRooms: StateFlow<List<String>> = _currentBuildingRooms

    /**
     * Default value is the building main gate
     */
    private val _searchRoomOriginId: MutableStateFlow<String> =
        MutableStateFlow(Constant.INDOOR_ROUTE_MAIN_GATE_ID)
    val searchRoomOrigin: StateFlow<String> = _searchRoomOriginId

    private val _searchRoomDestinationId: MutableStateFlow<String> = MutableStateFlow("")
    val searchRoomDestination: StateFlow<String> = _searchRoomDestinationId

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun updateUser(user: User?) {
        _user.value = user
    }

    fun getAllBuildingsInHCMUT(callback: () -> Unit) {
        viewModelScope.launch {
            val buildings = databaseRepository.getAllBuildingInHCMUT {
                callback()
            }
            _hcmutBuildings.emit(buildings)
        }
    }

    fun getCurrentBuildingRooms(callback: () -> Unit) {
        viewModelScope.launch {
            _currentBuildingRooms.value =
                databaseRepository.getCurrentBuildingRooms(_currentBuilding.value?.buildingId) {
                    callback()
                }
        }
    }

    fun changeStartPoint(origin: Point) {
        _startPoint.value = origin
    }

    fun changeDestinationPoint(destination: Point) {
        _destinationPoint.value = destination
    }

    fun changeCurrentBuilding(building: Building) {
        _currentBuilding.value = building
    }

    fun changeIsSearchDirection(isSearchDirection: Boolean) {
        _isSearchRoute.value = isSearchDirection
    }

    suspend fun setSearchRoomOriginId(id: String) {
        _searchRoomOriginId.emit(id)
    }

    suspend fun setSearchRoomDestinationId(id: String) {
        _searchRoomDestinationId.emit(id)
    }

    fun updateFirestoreUserConfig(firebaseUser: FirebaseUser?, callback: () -> Unit) {
        if (firebaseUser != null) {
            viewModelScope.launch {
                databaseRepository.setDefaultFirestoreUserConfig(firebaseUser) {
                    callback()
                }
            }
        }
    }

    /**
     * Add 1 to the number of created indoor paths of the user
     */
    fun plusOneNumOfIndoorPathCreated(firebaseUser: FirebaseUser?, callback: () -> Unit) {
        if (firebaseUser != null) {
            viewModelScope.launch {
                val maxPath = databaseRepository.getFirestoreUserConfig(
                    firebaseUser.uid,
                    Constant.FIELD_MAX_INDOOR_PATH_CAN_BE_CREATED
                ) {
                    callback()
                }

                val numPath = databaseRepository.getFirestoreUserConfig(
                    firebaseUser.uid,
                    Constant.FIELD_NUM_OF_INDOOR_PATH_CREATED
                ) {
                    callback()
                }

                if (maxPath != null && numPath != null) {
                    if (numPath < maxPath) {
                        databaseRepository.updateFirestoreUserConfig(
                            firebaseUser, Constant.FIELD_NUM_OF_INDOOR_PATH_CREATED, numPath + 1
                        ) {
                            callback()
                        }
                    }
                }
            }
        }
    }

    /**
     * Check an user is illegal to add an indoor path or not
     */
    fun canAddIndoorPath(firebaseUser: FirebaseUser?, canAddIndoorPath: (Boolean, Int) -> Unit,
                         callback: () -> Unit) {
        if (firebaseUser != null) {
            viewModelScope.launch {
                val maxPath = databaseRepository.getFirestoreUserConfig(
                    firebaseUser.uid,
                    Constant.FIELD_MAX_INDOOR_PATH_CAN_BE_CREATED
                ) {
                    callback()
                }

                val numPath = databaseRepository.getFirestoreUserConfig(
                    firebaseUser.uid,
                    Constant.FIELD_NUM_OF_INDOOR_PATH_CREATED
                ) {
                    callback()
                }

                if (maxPath != null && numPath != null) {
                    if (numPath >= maxPath) {
                        canAddIndoorPath(false, maxPath)
                    } else {
                        canAddIndoorPath(true, maxPath)
                    }
                }
            }
        }
    }

    fun uploadRecordListToFirebase(
        list: List<FirebaseRecordItem>,
        pathFolder: String, // gate1-402
        totalStepCountedForThePath: Int,
        callback: () -> Unit
    ) {
        viewModelScope.launch {
            databaseRepository.uploadRecordList(
                list,
                currentBuilding.value?.buildingId ?: "unknown",
                pathFolder,
                callback,
                totalStepCountedForThePath
            )
        }
    }

    suspend fun getIndoorPathList(roomStartId: String, roomDestId: String): List<IndoorPath> =
        databaseRepository.getIndoorPathList(
            _currentBuilding.value?.buildingId ?: "",
            roomStartId, roomDestId
        )
}
