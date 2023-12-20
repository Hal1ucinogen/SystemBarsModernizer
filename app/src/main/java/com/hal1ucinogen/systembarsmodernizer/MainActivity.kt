package com.hal1ucinogen.systembarsmodernizer

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.databinding.ActivityMainBinding
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private var mService: XposedService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.binder.text = "Loading"
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                mService = service
                binding.binder.text = "Binder acquired"
                binding.api.text = "API " + service.apiVersion
                binding.framework.text = "Framework " + service.frameworkName
                binding.frameworkVersion.text = "Framework version " + service.frameworkVersion
                binding.frameworkVersionCode.text =
                    "Framework version code " + service.frameworkVersionCode
                binding.scope.text = "Scope: " + service.scope
                binding.btnSave.setOnClickListener {
                    savePrefs()
                }
            }

            override fun onServiceDied(service: XposedService) {
            }
        })

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (mService == null) {
                binding.binder.text = "Binder is null"
            }
        }, 5000)
    }

    private fun savePrefs() {
        // DOTA MAX
        val mainPage = PageConfig()
        val matchPage = PageConfig()
        val webPage = PageConfig(
            edgeToEdge = true,
            windowBackgroundColor = Color.WHITE
        )
        val config =
            AppConfig(
                "com.dotamax.app",
                false,
                1,
                mapOf(
                    "com.max.app.module.main.MainActivity" to mainPage,
                    "com.max.app.module.match.match.MatchActivity" to matchPage,
                    "com.max.app.module.me.PlayerMeActivity" to matchPage,
                    "com.max.app.module.webaction.WebActionActivity" to webPage
                )
            )
        savePref(config.packageName, config)

        // 闲鱼
        val xyMain = PageConfig(edgeToEdge = true)
        val xyConfig =
            AppConfig(
                "com.taobao.idlefish",
                false,
                1,
                mapOf(
                    "com.taobao.idlefish.maincontainer.activity.MainActivity" to xyMain,
                )
            )
        savePref(xyConfig.packageName, xyConfig)

        // 京东
        val jdMain = PageConfig(navigationColor = Color.WHITE)
        val jdSearch = PageConfig(edgeToEdge = true)
        val jdConfig =
            AppConfig(
                "com.jingdong.app.mall",
                false,
                1,
                mapOf(
                    "com.jingdong.app.mall.MainFrameActivity" to jdMain,
                    "com.jd.lib.search.view.Activity.SearchActivity" to jdSearch
                )
            )
        savePref(jdConfig.packageName, jdConfig)

        // SBM - Checker
        val checkerMain = PageConfig(edgeToEdge = true)
        val checkerConfig =
            AppConfig(
                "com.hal1ucinogen.sbmchecker",
                false,
                1,
                mapOf(
                    "com.hal1ucinogen.sbmchecker.MainActivity" to checkerMain,
                )
            )
        savePref(checkerConfig.packageName, checkerConfig)

        // 淘宝
        val tbE2E = PageConfig(edgeToEdge = true)
        val tbConfig = AppConfig(
            TARGET_PACKAGE_NAME,
            false, 1, mapOf(
                ACTIVITY_SETTINGS to tbE2E,
                ACTIVITY_ORDER_LIST to tbE2E,
                ACTIVITY_SHOP to tbE2E,
                ACTIVITY_GOODS_PAGER to tbE2E
            )
        )
        savePref(tbConfig.packageName, tbConfig)
    }

    private fun savePref(group: String, config: AppConfig) {
        mService?.let {
            try {
                val prefs = it.getRemotePreferences(CONFIG_PREF_NAME)
                val json = Json.encodeToString(config)
                prefs.edit {
                    putString(group, json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}