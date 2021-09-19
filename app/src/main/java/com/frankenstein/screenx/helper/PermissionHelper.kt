/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.frankenstein.screenx.helper

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.frankenstein.screenx.overlay.OverlayPermission


class PermissionHelper {
    companion object {
        @JvmStatic
        fun hasStoragePermission(context: Context) = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        @JvmStatic
        fun hasUsagePermission(context: Context): Boolean {
                var granted = false;
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(), context.packageName)

                granted = if (mode == AppOpsManager.MODE_DEFAULT) {
                    context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) === PackageManager.PERMISSION_GRANTED
                } else {
                    mode == AppOpsManager.MODE_ALLOWED
                }
                return granted;
        }

        @TargetApi(Build.VERSION_CODES.M)
        fun shouldShowStorageRational(activity: FragmentActivity): Boolean {
            return activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        @JvmStatic
        fun hasOverlayPermission(context: Context): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || OverlayPermission.hasPermission(context)
        }
        @JvmStatic
        fun requestOverlayPermission(activity: Activity?, requestCode: Int) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return
            }
            activity?.let {
                val intent = OverlayPermission.createPermissionIntent(it)
                it.startActivityForResult(intent, requestCode)
            }
        }
    }
}
