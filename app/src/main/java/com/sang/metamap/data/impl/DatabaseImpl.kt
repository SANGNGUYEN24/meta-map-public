package com.sang.metamap.data.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sang.metamap.domain.model.Building
import com.sang.metamap.domain.model.FirebaseRecordItem
import com.sang.metamap.domain.model.IndoorPath
import com.sang.metamap.domain.repository.DatabaseRepository
import com.sang.metamap.utils.Constant
import com.sang.metamap.utils.Constant.BUILDING_INFO
import com.sang.metamap.utils.Constant.CAMPUS
import com.sang.metamap.utils.Constant.COLLECTION_HCMUT_MAP_USER
import com.sang.metamap.utils.Constant.HCMUT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class DatabaseImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DatabaseRepository {
    override suspend fun getAllBuildingInHCMUT(callback: () -> Unit): List<Building> =
        withContext(ioDispatcher) {
            db.collection(CAMPUS).document(HCMUT).collection(BUILDING_INFO)
                .get().addOnFailureListener {
                    callback()
                }.await().toObjects(Building::class.java)
        }

    override suspend fun getCurrentBuildingRooms(
        buildingId: String?,
        callback: () -> Unit
    ): List<String> =
        withContext(ioDispatcher) {
            if (buildingId.isNullOrEmpty()) {
                emptyList()
            } else {
                db.collection(CAMPUS).document(HCMUT).collection(BUILDING_INFO).document(buildingId)
                    .get().await().get("rooms") as? List<String> ?: emptyList()
            }
        }

    override suspend fun setDefaultFirestoreUserConfig(
        firebaseUser: FirebaseUser,
        callback: () -> Unit
    ) {
        withContext(ioDispatcher) {
            db.collection(CAMPUS).document(HCMUT).collection(COLLECTION_HCMUT_MAP_USER)
                .document(firebaseUser.uid).set(
                    mapOf(
                        Constant.FIELD_MAX_INDOOR_PATH_CAN_BE_CREATED to 3,
                        Constant.FIELD_NUM_OF_INDOOR_PATH_CREATED to 0,
                        "userName" to firebaseUser.displayName,
                        "userEmail" to firebaseUser.email
                    ), SetOptions.merge()
                )
        }
    }

    override suspend fun updateFirestoreUserConfig(
        firebaseUser: FirebaseUser,
        field: String,
        value: Any,
        callback: () -> Unit
    ) {
        withContext(ioDispatcher) {
            db.collection(CAMPUS).document(HCMUT).collection(COLLECTION_HCMUT_MAP_USER)
                .document(firebaseUser.uid).update(field, value)
        }
    }

    override suspend fun getFirestoreUserConfig(
        uid: String,
        field: String,
        callback: () -> Unit
    ): Int? =
        withContext(ioDispatcher) {
            db.collection(CAMPUS).document(HCMUT).collection(COLLECTION_HCMUT_MAP_USER)
                .document(uid).get().await().getLong(field)?.toInt()
        }

    override suspend fun uploadRecordList(
        list: List<FirebaseRecordItem>,
        buildingId: String,
        pathFolder: String,
        callback: () -> Unit,
        totalStepCountedForThePath: Int
    ) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            withContext(ioDispatcher) {
                val pathFolderRef = db.collection(CAMPUS).document(HCMUT)
                    .collection(Constant.COLLECTION_HCMUT_INDOOR_PATH)
                    .document(buildingId).collection (Constant.COLLECTION_BUILDING_INDOOR_PATH)
                    .document(pathFolder)

                pathFolderRef.set(
                    mapOf("id" to buildingId), SetOptions.merge()
                ).await()

                pathFolderRef.collection(Constant.COLLECTION_BUILDING_INDOOR_PATH)
                    .document("${UUID.randomUUID()}")
                    .set(
                        mapOf(
                            "record" to list,
                            "userName" to firebaseUser.displayName,
                            "userPhotoUrl" to firebaseUser.photoUrl,
                            "pathFolder" to pathFolder,
                            "createdAt" to Date(System.currentTimeMillis()).toString(),
                            "totalDistance" to (totalStepCountedForThePath * 0.5).toInt()
                        )
                    ).addOnSuccessListener {
                        callback()
                    }.await()
            }
        }
    }

    override suspend fun getIndoorPathList(
        buildingId: String,
        roomStartId: String,
        roomDestId: String
    ): List<IndoorPath> = withContext(Dispatchers.IO) {
        db.collection(CAMPUS).document(HCMUT)
            .collection(Constant.COLLECTION_HCMUT_INDOOR_PATH)
            .document(buildingId)
            .collection(Constant.COLLECTION_BUILDING_INDOOR_PATH)
            .document("$roomStartId-$roomDestId")
            .collection(Constant.COLLECTION_BUILDING_INDOOR_PATH)
            .get().addOnFailureListener {
                it.printStackTrace()
            }.await().toObjects(IndoorPath::class.java)
    }
}
