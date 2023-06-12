package com.sang.metamap.utils

import android.content.Context
import android.widget.Toast

object ToastUtil {
    fun showMess(context: Context, message: String) {
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}