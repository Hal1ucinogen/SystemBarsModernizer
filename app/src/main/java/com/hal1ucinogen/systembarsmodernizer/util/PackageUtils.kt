package com.hal1ucinogen.systembarsmodernizer.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.hal1ucinogen.systembarsmodernizer.compat.PackageManagerCompat
import com.hal1ucinogen.systembarsmodernizer.constant.AndroidVersions
import com.hal1ucinogen.systembarsmodernizer.constant.GlobalValues
import com.hal1ucinogen.systembarsmodernizer.constant.option.AdvancedOptions
import com.hal1ucinogen.systembarsmodernizer.util.extension.getVersionCode

object PackageUtils {

    /**
     * Get packageInfo
     * @param info ApplicationInfo
     * @param flag Flag mask
     * @return PackageInfo
     * @throws PackageManager.NameNotFoundException
     */
    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(info: ApplicationInfo, flag: Int = 0): PackageInfo {
        return getPackageInfo(info.packageName, flag)
    }

    /**
     * Get packageInfo
     * @param packageName Package name string
     * @param flag Flag mask
     * @return PackageInfo
     * @throws PackageManager.NameNotFoundException
     */
    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageName: String, flag: Int = 0): PackageInfo {
        val packageInfo = PackageManagerCompat.getPackageInfo(
            packageName,
            PackageManager.MATCH_DISABLED_COMPONENTS or flag
        )
        return packageInfo
    }

    /**
     * Get version code of an app
     * @param packageName packageName
     * @return version code as Long Integer
     */
    fun getVersionCode(packageName: String): Long {
        return getPackageInfo(packageName).getVersionCode()
    }

    /**
     * Get version string of an app ( 1.0.0(1) )
     * @param versionName Version name
     * @param versionCode Version code
     * @return version code as String
     */
    fun getVersionString(versionName: String, versionCode: Long): String {
        return "$versionName ($versionCode)"
    }

    /**
     * Get components list of an app
     * @param packageName Package name of the app
     * @param list List of components(can be nullable)
     * @param isSimpleName Whether to show class name as a simple name
     * @return List of String
     */
    private fun getComponentStringList(
        packageName: String,
        list: Array<out ComponentInfo>?,
        isSimpleName: Boolean
    ): List<String> {
        if (list.isNullOrEmpty()) {
            return emptyList()
        }
        return list.asSequence()
            .map {
                if (isSimpleName) {
                    it.name.removePrefix(packageName)
                } else {
                    it.name
                }
            }
            .toList()
    }

    /**
     * Check if an app is installed
     * @return true if it is installed
     */
    fun isAppInstalled(pkgName: String): Boolean {
        return runCatching {
            PackageManagerCompat.getApplicationInfo(pkgName, 0).enabled
        }.getOrDefault(false)
    }

    fun getLauncherActivity(packageName: String): String {
        val intent = Intent(Intent.ACTION_MAIN, null)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(packageName)
        val info = PackageManagerCompat.queryIntentActivities(intent, 0)
        return info.getOrNull(0)?.activityInfo?.name.orEmpty()
    }

    fun startLaunchAppActivity(context: Context, packageName: String?) {
        if (packageName == null) {
            return
        }
        val launcherActivity = getLauncherActivity(packageName)
        val launchIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(packageName, launcherActivity)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    }

    fun getBuildVersionsInfo(packageInfo: PackageInfo?, packageName: String): CharSequence {
        val showAndroidVersion =
            (GlobalValues.advancedOptions and AdvancedOptions.SHOW_ANDROID_VERSION) > 0
        val showTarget =
            (GlobalValues.advancedOptions and AdvancedOptions.SHOW_TARGET_API) > 0
        val showMin =
            (GlobalValues.advancedOptions and AdvancedOptions.SHOW_MIN_API) > 0
        val target = packageInfo?.applicationInfo?.targetSdkVersion ?: Build.VERSION.SDK_INT
        val min = packageInfo?.applicationInfo?.minSdkVersion ?: Build.VERSION.SDK_INT

        return buildSpannedString {
            if (showTarget) {
                append(", ")
                scale(0.8f) {
                    append("Target: ")
                }
                append(target.toString())
                if (showAndroidVersion) {
                    append(" (${AndroidVersions.simpleVersions[target]})")
                }
            }

            if (showMin) {
                if (showTarget) {
                    append(", ")
                }
                scale(0.8f) {
                    append(" Min: ")
                }
                append(min.toString())
                if (showAndroidVersion) {
                    append(" (${AndroidVersions.simpleVersions[min]})")
                }
            }
        }
    }
}
