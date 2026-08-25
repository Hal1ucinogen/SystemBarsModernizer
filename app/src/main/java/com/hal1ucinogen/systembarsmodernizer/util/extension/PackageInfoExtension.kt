package com.hal1ucinogen.systembarsmodernizer.util.extension

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat
import com.hal1ucinogen.systembarsmodernizer.app.SystemServices

/**
 * Get version code of an app
 * @return version code as Long Integer
 */
fun PackageInfo.getVersionCode(): Long {
    return PackageInfoCompat.getLongVersionCode(this)
}

/**
 * Get version string of an app ( 1.0.0(1) )
 * @return version code as String
 */
fun PackageInfo.getVersionString(): String {
    return runCatching {
        "${versionName ?: "<unknown>"} (${getVersionCode()})"
    }.getOrDefault("Unknown")
}

/**
 * Get target api string of an app ( API 30 )
 * @return version code as String
 */
fun PackageInfo.getTargetApiString(): String {
    return runCatching {
        applicationInfo!!.targetSdkVersion.toString()
    }.getOrDefault("?")
}

/**
 * Get permissions list of an app
 * @return Permissions list
 */
fun PackageInfo.getPermissionsList(): List<String> {
    return requestedPermissions?.toList() ?: emptyList()
}

/**
 * Check if an app uses split apks
 * @return true if it uses split apks
 */
fun PackageInfo.isSplitsApk(): Boolean {
    return !applicationInfo?.splitSourceDirs.isNullOrEmpty()
}

private const val AGP_KEYWORD = "androidGradlePluginVersion"
private const val AGP_KEYWORD2 = "Created-By: Android Gradle "

/**
 * Check if an app is a Xposed module
 * @return True if is a Xposed module
 */
fun PackageInfo.isXposedModule(): Boolean {
    val metaData = applicationInfo?.metaData ?: return false
    return metaData.getBoolean("xposedmodule") || metaData.containsKey("xposedminversion")
}

fun PackageInfo.getAppName(): String? =
    applicationInfo?.loadLabel(SystemServices.packageManager)?.toString()

val PREINSTALLED_TIMESTAMP by lazy {
    // default is 2009-01-01 08:00:00 GMT+8
    runCatching {
        SystemServices.packageManager.getPackageInfo("android", 0).lastUpdateTime
    }.getOrDefault(1230768000000)
}

fun PackageInfo.isPreinstalled(): Boolean {
    return lastUpdateTime <= PREINSTALLED_TIMESTAMP
}