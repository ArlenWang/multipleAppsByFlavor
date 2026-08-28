package com.smarola.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

data class AppConfig(
    val flavor: String,
    val scheme: String,
    val defaultWebUrl: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long
) {
    companion object {
        private const val META_FLAVOR = "com.smarola.APP_FLAVOR"
        private const val META_SCHEME = "com.smarola.APP_SCHEME"
        private const val META_WEB_URL = "com.smarola.DEFAULT_WEB_URL"

        fun from(context: Context): AppConfig {
            val pm = context.packageManager
            val packageInfo = pm.getPackageInfo(context.packageName, 0)
            val applicationInfo = pm.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val meta = applicationInfo.metaData
            return AppConfig(
                flavor = meta?.getString(META_FLAVOR).orEmpty(),
                scheme = meta?.getString(META_SCHEME).orEmpty(),
                defaultWebUrl = meta?.getString(META_WEB_URL) ?: "https://example.com",
                appName = pm.getApplicationLabel(applicationInfo).toString(),
                packageName = context.packageName,
                versionName = packageInfo.versionName.orEmpty(),
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
            )
        }
    }
}
