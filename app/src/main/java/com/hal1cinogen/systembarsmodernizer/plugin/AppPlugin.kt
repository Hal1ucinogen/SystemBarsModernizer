package com.hal1cinogen.systembarsmodernizer.plugin

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import com.hal1cinogen.systembarsmodernizer.METHOD_ON_CREATE
import com.hal1cinogen.systembarsmodernizer.bean.AppConfig
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class AppPlugin : BaseAppPlugin() {

    @Keep
    fun main(context: Context, appConfig: AppConfig) {
        try {
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                METHOD_ON_CREATE,
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        val activity = param?.thisObject as? Activity ?: return
                        onActivityCreated(activity, appConfig)
                    }
                })
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }
}