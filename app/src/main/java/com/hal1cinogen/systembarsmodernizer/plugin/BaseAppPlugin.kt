package com.hal1cinogen.systembarsmodernizer.plugin

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hal1cinogen.systembarsmodernizer.bean.AppConfig
import com.hal1cinogen.systembarsmodernizer.tool.Task
import de.robv.android.xposed.XposedBridge

open class BaseAppPlugin {

    protected fun onActivityCreated(activity: Activity, appConfig: AppConfig) {
        XposedBridge.log("Activity onCreate - $activity")
        val scope = appConfig.scope
        val pageConfig = scope[activity.javaClass.name] ?: return
        XposedBridge.log("Activity ${activity.javaClass.name} config | $pageConfig")
        val window = activity.window ?: return
        if (pageConfig.edgeToEdge) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } else {
            Task.onMain(100) {
                window.setBackgroundDrawable(ColorDrawable(pageConfig.windowBackgroundColor))
                if (pageConfig.clearTranslucent) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                }
                setTransparent(window)
            }
        }
        /*when (activity.javaClass.name) {
            ACTIVITY_GOODS_DETAIL -> {
                window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                removeTranslucentStatusOverlay(window)
            }

            ACTIVITY_SUB_PROJECT -> {
                setSystemBarsLight(window)
            }

            ACTIVITY_BROWSER -> {
                removeTranslucentStatusOverlay(window)
            }

            ACTIVITY_SEARCH_RESULT -> {
                window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            }

            ACTIVITY_WEEX,ACTIVITY_CONTACTS-> {
                removeTranslucentStatusOverlay(window)
            }

            ACTIVITY_CHAT -> {
                setSystemBarsLight(window)
            }

            ACTIVITY_SETTINGS, ACTIVITY_ORDER_LIST, ACTIVITY_SHOP, ACTIVITY_GOODS_PAGER -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }*/
    }

    private fun removeTranslucentStatusOverlay(window: Window) {
        Task.onMain(100) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTransparent(window)
//            window.decorView.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            setSystemBarsLight(window)
        }
    }

    private fun setTransparent(window: Window) {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setSystemBarsLight(window: Window, light: Boolean = true) {
        WindowInsetsControllerCompat(window, window.decorView).run {
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }
}