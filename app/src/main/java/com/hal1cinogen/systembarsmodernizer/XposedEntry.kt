package com.hal1cinogen.systembarsmodernizer

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedEntry : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
    }

    override fun handleLoadPackage(lpp: XC_LoadPackage.LoadPackageParam) {
//        if (lpp.packageName != TARGET_PACKAGE_NAME) return
        hook(lpp)
    }

    private fun hook(lpp: XC_LoadPackage.LoadPackageParam) {

    }
}