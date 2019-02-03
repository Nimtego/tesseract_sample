package com.nimtego.tesseract_sample

import android.content.Context
import android.support.v4.app.FragmentActivity


interface RequestPermissionsTool {
    fun requestPermissions(context: FragmentActivity, permissions: Array<String>)

    fun isPermissionsGranted(context: Context, permissions: Array<String>): Boolean

    fun onPermissionDenied()
}