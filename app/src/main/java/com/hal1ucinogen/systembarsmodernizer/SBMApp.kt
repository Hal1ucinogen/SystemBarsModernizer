package com.hal1ucinogen.systembarsmodernizer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.color.DynamicColors
import com.hal1ucinogen.systembarsmodernizer.constant.GlobalValues
import com.hal1ucinogen.systembarsmodernizer.util.OsUtils
import com.hal1ucinogen.systembarsmodernizer.util.PackageUtils
import com.hal1ucinogen.systembarsmodernizer.util.UiUtils
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import rikka.material.app.LocaleDelegate
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.Volatile

class SBMApp : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        //noinspection StaticFieldLeak
        lateinit var app: Application

        fun generateAuthKey(): Int {
            if (GlobalValues.uuid.isEmpty()) {
                GlobalValues.uuid = UUID.randomUUID().toString()
            }
            return (GlobalValues.uuid.hashCode() + PackageUtils.getPackageInfo(app.packageName).firstInstallTime).mod(
                90000
            ) + 10000
        }

        @Volatile
        var mService: XposedService? = null
            private set
        private val serviceStateListeners =
            CopyOnWriteArraySet<ServiceStateListener>()

        private fun dispatchServiceState(
            listener: ServiceStateListener,
            service: XposedService?
        ) {
            if (serviceStateListeners.contains(listener)) {
                listener.onServiceStateChanged(service)
            }
        }

        fun addServiceStateListener(
            listener: ServiceStateListener,
            notifyImmediately: Boolean
        ) {
            serviceStateListeners.add(listener)
            if (notifyImmediately) {
                dispatchServiceState(listener, mService)
            }
        }

        fun removeServiceStateListener(listener: ServiceStateListener) {
            serviceStateListeners.remove(listener)
        }
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
        app = this
//        Utility.init(this)
        LocaleDelegate.defaultLocale = GlobalValues.locale
        if (OsUtils.atLeastT()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(GlobalValues.locale))
        }
        AppCompatDelegate.setDefaultNightMode(UiUtils.getNightMode())
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun notifyServiceStateChanged(service: XposedService?) {
        for (listener in serviceStateListeners) {
            dispatchServiceState(listener, service)
        }
    }

    interface ServiceStateListener {
        fun onServiceStateChanged(service: XposedService?)
    }

    override fun onServiceBind(service: XposedService) {
        mService = service
        notifyServiceStateChanged(mService)
    }

    override fun onServiceDied(service: XposedService) {
        mService = null
        notifyServiceStateChanged(mService)
    }
}
