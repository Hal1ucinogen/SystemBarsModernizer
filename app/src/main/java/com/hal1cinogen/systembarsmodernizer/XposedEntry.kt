package com.hal1cinogen.systembarsmodernizer

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import com.hal1cinogen.systembarsmodernizer.bean.AppConfig
import com.hal1cinogen.systembarsmodernizer.plugin.AppPlugin
import com.hal1cinogen.systembarsmodernizer.tool.PreferenceProvider
import com.hal1cinogen.systembarsmodernizer.tool.XposedPluginLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.serialization.json.Json

class XposedEntry : IXposedHookLoadPackage {

    // TODO
    //  考虑换用
    //  https://github.com/libxposed/example/blob/master/app/src/main/java/io/github/libxposed/example/ModuleMain.kt
    //  等新 API 正式发布
    override fun handleLoadPackage(lpp: XC_LoadPackage.LoadPackageParam) {
        if (lpp.packageName == BuildConfig.APPLICATION_ID) return
        hook(lpp)
    }

    private fun hook(lpp: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("loaded - ${lpp.packageName}")
        XposedHelpers.findAndHookMethod(
            Instrumentation::class.java,
            "callApplicationOnCreate",
            Application::class.java,
            object : XC_MethodHook() {
//                    private var called = false

                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    XposedBridge.log("Application onCreate | process - ${lpp.processName}")
//                        if (!called) {
//                            called = true
                    // Only hook main process
                    if (lpp.processName.contains(":")) return
                    val context = param?.args?.firstOrNull() as? Context ?: return
                    val pref = PreferenceProvider.getRemote(context)
                    val appConfigStr = pref.getString(lpp.packageName, null).orEmpty()
                    XposedBridge.log("got config str - $appConfigStr")
                    try {
                        val appConfig = Json.decodeFromString<AppConfig>(appConfigStr)
                        XposedBridge.log("app config - $appConfig")
                        XposedPluginLoader.load(
                            lpp.packageName,
                            AppPlugin::class.java,
                            context,
                            appConfig
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        XposedBridge.log("app config decode failed")
                    }
//                        }
                }
            })
    }
}