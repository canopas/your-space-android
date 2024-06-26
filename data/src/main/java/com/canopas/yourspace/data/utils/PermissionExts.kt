package com.canopas.yourspace.data.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat

private fun checkPermission(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}

val Context.isLocationPermissionGranted
    get() =
        hasFineLocationPermission && hasBackgroundLocationPermission

val Context.isBackgroundLocationPermissionGranted get() = hasBackgroundLocationPermission

val Context.hasFineLocationPermission
    get() = checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

val Context.hasCoarseLocationPermission
    get() = checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

private val Context.hasBackgroundLocationPermission
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        checkPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        true
    }

val Context.hasNotificationPermission
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

val Context.hasAllPermission get() = isLocationPermissionGranted

val Context.isBatteryOptimizationEnabled: Boolean
    get() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(packageName).not()
        } else {
            false
        }
    }
