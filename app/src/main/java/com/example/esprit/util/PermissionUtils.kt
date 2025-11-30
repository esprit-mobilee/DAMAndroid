package com.example.esprit.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(context: Context, permission: String) {
        if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(permission),
                1001
            )
        }
    }
}
