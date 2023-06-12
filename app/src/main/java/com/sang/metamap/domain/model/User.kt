package com.sang.metamap.domain.model

import android.net.Uri

data class User(
    val uid: String? = null,
    val email: String? = null,
    val userName: String? = null,
    val profileUrl: Uri? = null
)
