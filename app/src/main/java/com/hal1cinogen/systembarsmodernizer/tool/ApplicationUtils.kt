package com.hal1cinogen.systembarsmodernizer.tool

import android.app.Activity
import android.app.Application
import android.content.Context
import de.robv.android.xposed.XposedBridge

object ApplicationUtils {

    private var sApplication: Application? = null

    @JvmStatic
    val application: Application?
        get() {
            if (sApplication == null) {
                try {
                    val activityThread = Class.forName("android.app.ActivityThread")
                    val currentApplicationMethod =
                        activityThread.getDeclaredMethod("currentApplication")
                    sApplication = currentApplicationMethod.invoke(null) as Application
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "LibraryLoader not initialized. Call LibraryLoader.initialize() before using library classes.",
                        e
                    )
                }
            }
            return sApplication
        }

    val currentActivity: Activity?
        get() {
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val activityThread =
                    activityThreadClass.getMethod("currentActivityThread").invoke(null)
                val activitiesField = activityThreadClass.getDeclaredField("mActivities")
                activitiesField.isAccessible = true
                val activities = activitiesField[activityThread] as Map<Any, Any>
                    ?: return null
                for (activityRecord in activities.values) {
                    val activityRecordClass: Class<*> = activityRecord.javaClass
                    val pausedField = activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    if (!pausedField.getBoolean(activityRecord)) {
                        val activityField =
                            activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        return activityField[activityRecord] as Activity
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log(e)
            }
            return null
        }

    fun getPackageVersionCode(context: Context, packageName: String?): Int {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(
                    packageName!!, 0
                )
            return packageInfo.versionCode
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
        return 0
    }
}