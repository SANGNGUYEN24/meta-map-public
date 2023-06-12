package com.sang.metamap.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.sang.metamap.domain.model.Building
import com.sang.metamap.domain.model.FirebaseRecordItem
import com.sang.metamap.domain.model.IndoorPath

interface DatabaseRepository {
    suspend fun getAllBuildingInHCMUT(callback: () -> Unit) : List<Building>
    suspend fun getCurrentBuildingRooms(buildingId: String?, callback: () -> Unit): List<String>
    suspend fun setDefaultFirestoreUserConfig(firebaseUser: FirebaseUser, callback: () -> Unit)
    suspend fun updateFirestoreUserConfig(firebaseUser: FirebaseUser, field: String, value: Any,
                                          callback: () -> Unit)
    suspend fun getFirestoreUserConfig(uid: String, field: String, callback: () -> Unit): Int?
    suspend fun uploadRecordList(
        list: List<FirebaseRecordItem>,
        buildingId: String,
        pathFolder: String,
        callback: () -> Unit,
        totalStepCountedForThePath: Int
    )

    suspend fun getIndoorPathList(buildingId: String, roomStartId: String, roomDestId: String): List<IndoorPath>
}