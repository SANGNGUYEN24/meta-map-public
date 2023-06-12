package com.sang.metamap.domain.model

data class IndoorPath(
    val pathFolder: String = "",
    val record: ArrayList<FirebaseRecordItem>? = null,
    val userName: String = "",
    val userPhotoUrl: String = "",
    val createdAt: String = "",
    val totalDistance: Int = 0 // meters
)
