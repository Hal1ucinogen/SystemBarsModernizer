package com.hal1ucinogen.systembarsmodernizer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
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
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import kotlinx.serialization.json.Json

private lateinit var module: ModuleMain

class ModuleMain(base: XposedInterface, param: ModuleLoadedParam) : XposedModule(base, param) {

    init {
        log("ModuleMain at " + param.processName)
        module = this
    }

    val configMap = mutableMapOf<String, AppConfig>()

    @XposedHooker
    class DefaultHooker : XposedInterface.Hooker {

        companion object {
            @JvmStatic
            @BeforeInvocation
            fun beforeInvocation(callback: BeforeHookCallback) {
                val onCreateMethod = Activity::class.java.getDeclaredMethod(
                    METHOD_ON_CREATE, Bundle::class.java
                )
                module.hook(onCreateMethod, ActivityHooker::class.java)
            }
        }
    }

    @XposedHooker
    class ActivityHooker : XposedInterface.Hooker {
        companion object {
            @JvmStatic
            @AfterInvocation
            fun after(callback: AfterHookCallback) {
                val activity = callback.thisObject as? Activity ?: return
                val window = activity.window ?: return
                module.log("Activity onCreate | ${activity.javaClass.name}")
                val config = module.configMap[activity.packageName] ?: return
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
                module.log("Activity $activityName config | $pageConfig")
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
                        module.log("In ExtraAction | $extraAction")
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
                            module.log("Find Target - $target")
                            module.log("Target Padding - ${target.paddingTop}|${target.paddingBottom}|${target.paddingStart}|${target.paddingEnd}")
                            module.log("Target Margin - ${target.marginTop}|${target.marginBottom}|${target.marginStart}|${target.marginEnd}")
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
                            window.setBackgroundDrawable(
                                ColorDrawable(it)
                            )
                        }
                        if (pageConfig.clearTranslucent) {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        }
                        window.statusBarColor = pageConfig.statusColor
                        window.navigationBarColor = pageConfig.navigationColor
                    }
                }
            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun onPackageLoaded(param: PackageLoadedParam) {
        super.onPackageLoaded(param)
        log("onPackageLoaded: " + param.packageName)
        log("param classloader is " + param.classLoader)
        log("module apk path: " + this.applicationInfo.sourceDir)
        log("----------")

        if (!param.isFirstPackage) return
        if (param.applicationInfo.processName.contains(":")) return
        try {
            val packageName = param.packageName
            val prefs = getRemotePreferences(CONFIG_PREF_NAME)
            val appConfigStr = prefs.getString(packageName, null)
            if (appConfigStr.isNullOrEmpty()) {
                log("Remote prefs: app config is null")
                return
            }
            log("Remote prefs: got config str - $appConfigStr")
            val remoteConfig = Json.decodeFromString<AppConfig>(appConfigStr)
            log("Remote prefs: app config - $remoteConfig")
            configMap[packageName] = remoteConfig
            val callApplicationOnCreateMethod = Instrumentation::class.java.getDeclaredMethod(
                METHOD_CALL_APPLICATION_ON_CREATE, Application::class.java
            )
            hook(callApplicationOnCreateMethod, DefaultHooker::class.java)
        } catch (e: UnsupportedOperationException) {
            log("app config access failed", e)
        } catch (e: Exception) {
            log("app config decode failed", e)
        }
    }
}