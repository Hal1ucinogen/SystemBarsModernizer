package com.hal1ucinogen.systembarsmodernizer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.tool.Task
import com.hal1ucinogen.systembarsmodernizer.util.getNavigationHeight
import com.hal1ucinogen.systembarsmodernizer.util.getStatusHeight
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import kotlinx.serialization.json.Json

class ModuleMain : XposedModule() {

    companion object {
        const val TAG = "SystemBarsModernizer"
    }

    val configMap = mutableMapOf<String, AppConfig>()

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "onModuleLoaded: " + param.processName)
        log(Log.INFO, TAG, "framework: $frameworkName($frameworkVersionCode) API $apiVersion")

        val hasProp: (Long) -> Boolean = { prop -> frameworkProperties.and(prop) != 0L }
        log(Log.INFO, TAG, "system supported: " + hasProp(PROP_CAP_SYSTEM))
        log(Log.INFO, TAG, "remote supported: " + hasProp(PROP_CAP_REMOTE))
        log(Log.INFO, TAG, "api protection: " + hasProp(PROP_RT_API_PROTECTION))
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: PackageLoadedParam) {
        log(Log.INFO, TAG, "onPackageLoaded: " + param.packageName)
        log(Log.INFO, TAG, "default classloader is " + param.defaultClassLoader)
        log(Log.INFO, TAG, "----------")
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        log(Log.INFO, TAG, "onPackageReady: " + param.packageName)
        log(Log.INFO, TAG, "app classloader is " + param.classLoader)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            log(Log.INFO, TAG, "app acf is " + param.appComponentFactory)
        }
        log(Log.INFO, TAG, "module apk path: " + this.moduleApplicationInfo.sourceDir)
        log(Log.INFO, TAG, "----------")
        if (!param.isFirstPackage) return
        if (param.applicationInfo.processName.contains(":")) return
        try {
            val packageName = param.packageName
            val prefs = getRemotePreferences(CONFIG_PREF_NAME)
            val appConfigStr = prefs.getString(packageName, null)
            if (appConfigStr.isNullOrEmpty()) {
                log(Log.INFO, TAG, "Remote prefs: app config is null")
                return
            }
            log(Log.INFO, TAG, "Remote prefs: got config str - $appConfigStr")
            val remoteConfig = Json.decodeFromString<AppConfig>(appConfigStr)
            log(Log.INFO, TAG, "Remote prefs: app config - $remoteConfig")
            configMap[packageName] = remoteConfig
            val callApplicationOnCreateMethod = Instrumentation::class.java.getDeclaredMethod(
                METHOD_CALL_APPLICATION_ON_CREATE, Application::class.java
            )
            hook(callApplicationOnCreateMethod).intercept { chain ->
                val onCreateMethod = Activity::class.java.getDeclaredMethod(
                    METHOD_ON_CREATE, Bundle::class.java
                )
                hook(onCreateMethod).intercept { c ->
                    c.proceed()
                    doOnActivityCreated(c)
                }
                chain.proceed()
            }
        } catch (e: UnsupportedOperationException) {
            log(Log.INFO, TAG, "app config access failed", e)
        } catch (e: Exception) {
            log(Log.INFO, TAG, "app config decode failed", e)
        }
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        log(Log.INFO, TAG, "onSystemServerStarting, system classloader: " + param.classLoader)
    }

    private fun doOnActivityCreated(chain: XposedInterface.Chain) {
        val activity = chain.thisObject as? Activity ?: return
        val window = activity.window
        log(Log.INFO, TAG, "Activity onCreate | ${activity.javaClass.name}")
        val config = configMap[activity.packageName] ?: return
        val activityName = activity.javaClass.name
        val scope = config.scope
        var pageConfig = scope[activityName]
        if (pageConfig == null) {
            val general = config.general
            if (general == null || activityName in general.exclusive) {
                return
            } else {
                pageConfig = config.general.config
            }
        }
        log(Log.INFO, TAG, "Activity $activityName config | $pageConfig")
        if (pageConfig.edgeToEdge) {
            pageConfig.windowBackgroundColor?.let {
                window.setBackgroundDrawable(
                    ColorDrawable(it)
                )
            }
            window.statusBarColor = Color.TRANSPARENT
            window.isStatusBarContrastEnforced = false
            window.decorView.post {
                window.navigationBarColor = Color.TRANSPARENT
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.setDecorFitsSystemWindows(window, false)
//                    activity.enableEdgeToEdgeCompat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            // TODO There is a inset change issue. Try to useViewCompat.setOnApplyWindowInsetsListener instead
            pageConfig.extraActions.forEach { extraAction ->
                log(Log.INFO, TAG, "In ExtraAction | $extraAction")
                Task.onMain(100) {
                    val resources = window.decorView.resources
                    val id = resources.getIdentifier(
                        extraAction.viewId,
                        "id",
                        activity.packageName
                    )
                    val inset = if (extraAction.useSystemInsets) {
                        if (extraAction.isTop) {
                            activity.getStatusHeight()
                        } else {
                            activity.getNavigationHeight()
                        }
                    } else {
                        extraAction.customInset
                    }
                    val target = if (extraAction.isGroup) {
                        val group = activity.findViewById<ViewGroup>(id)
                        if (extraAction.self) {
                            group
                        } else {
                            group.children.elementAtOrNull(extraAction.childIndex)
                        }
                    } else {
                        activity.findViewById<View>(id)
                    } ?: return@onMain
                    log(Log.INFO, TAG, "Find Target - $target")
                    log(
                        Log.INFO,
                        TAG,
                        "Target Padding - ${target.paddingTop}|${target.paddingBottom}|${target.paddingStart}|${target.paddingEnd}"
                    )
                    log(
                        Log.INFO,
                        TAG,
                        "Target Margin - ${target.marginTop}|${target.marginBottom}|${target.marginStart}|${target.marginEnd}"
                    )
                    if (extraAction.isPadding) {
                        if (extraAction.isTop) {
                            target.updatePadding(top = inset)
                        } else {
                            target.updatePadding(bottom = inset)
                        }
                    } else {
                        if (extraAction.isTop) {
                            target.updateLayoutParams<ViewGroup.LayoutParams> {
                                (this as ViewGroup.MarginLayoutParams).topMargin =
                                    inset
                            }
                        } else {
                            target.updateLayoutParams<ViewGroup.LayoutParams> {
                                (this as ViewGroup.MarginLayoutParams).bottomMargin =
                                    inset
                            }
                        }
                    }
                }
            }
        } else {
            Task.onMain(100) {
                pageConfig.windowBackgroundColor?.let {
                    if (it == COLOR_INT_UI_MODE_NIGHT) {
                        val currentNightMode =
                            activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                        val color = when (currentNightMode) {
                            Configuration.UI_MODE_NIGHT_NO -> pageConfig.uiModeWBC.first// Night mode is not active, we're using the light theme.
                            Configuration.UI_MODE_NIGHT_YES -> pageConfig.uiModeWBC.second // Night mode is active, we're using dark theme.
                            else -> pageConfig.uiModeWBC.second
                        }
                        window.setBackgroundDrawable(ColorDrawable(color))
                    } else {
                        window.setBackgroundDrawable(
                            ColorDrawable(it)
                        )
                    }
                }
                if (pageConfig.clearTranslucent) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                }
                window.statusBarColor = pageConfig.statusColor
                window.isStatusBarContrastEnforced = false
                window.decorView.post {
                    window.navigationBarColor = Color.TRANSPARENT
                    window.isNavigationBarContrastEnforced = false
                }
            }
        }

    }
}