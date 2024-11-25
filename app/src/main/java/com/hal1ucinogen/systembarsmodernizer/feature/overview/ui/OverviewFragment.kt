package com.hal1ucinogen.systembarsmodernizer.feature.overview.ui

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import com.hal1ucinogen.systembarsmodernizer.CONFIG_PREF_NAME
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.bean.GeneralConfig
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.databinding.FragmentOverviewBinding
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseFragment
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OverviewFragment : BaseFragment<FragmentOverviewBinding>() {

    private var mService: XposedService? = null

    override fun init() {
        val context = (context as? BaseActivity<*>) ?: return
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
                mService = null
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
        val mainPage = PageConfig(edgeToEdge = true, windowBackgroundColor = Color.WHITE)
        val matchPage = PageConfig(edgeToEdge = true, windowBackgroundColor = Color.WHITE)
        val webPage = PageConfig(
            edgeToEdge = true,
            windowBackgroundColor = Color.WHITE
        )
        val config =
            AppConfig(
                "com.dotamax.app",
                1,
                mapOf(
                    "com.max.app.module.main.MainActivity" to mainPage,
                    "com.max.app.module.match.match.MatchActivity" to matchPage,
                    "com.max.app.module.me.PlayerMeActivity" to matchPage,
                    "com.max.app.module.webaction.WebActionActivity" to webPage
                ),
                GeneralConfig(
                    PageConfig(edgeToEdge = true),
                    listOf("com.max.app.module.discovery.ImageActivity")
                )
            )
        savePref(config.packageName, config)

        // 闲鱼
        val xyMain = PageConfig(edgeToEdge = false)
        val xyConfig =
            AppConfig(
                "com.taobao.idlefish",
                1,
                mapOf(
                    "com.taobao.idlefish.maincontainer.activity.MainActivity" to xyMain,
                ),
                GeneralConfig(PageConfig(edgeToEdge = true))
            )
        savePref(xyConfig.packageName, xyConfig)

        // 京东
        val jdMain = PageConfig(navigationColor = Color.WHITE)
        val jdE2E = PageConfig(edgeToEdge = true)
        val jdConfig =
            AppConfig(
                "com.jingdong.app.mall",
                1,
                mapOf(
                    "com.jd.lib.ordercenter.mygoodsorderlist.view.activity.MyOrderListActivity" to jdE2E,
                    "com.jd.lib.search.view.Activity.SearchActivity" to jdE2E,
                    "com.jd.lib.productdetail.mainimage.bigimage.PdBigImageActivity" to jdE2E,
                    "com.jd.lib.evaluatecenter.view.activity.DetailPreviewActivity" to jdE2E,
                    "com.jd.lib.productdetailmini.PdMiniActivity" to jdE2E
                ),
                general = GeneralConfig(jdMain, exclusive = listOf("com.jd.lib.videoimmersionstyleb.view.activity.VideoImmersionStyleBActivity"))
            )
        savePref(jdConfig.packageName, jdConfig)

        // SBM - Checker
        val checkerMain = PageConfig(edgeToEdge = true)
        val checkerConfig =
            AppConfig(
                "com.hal1ucinogen.sbmchecker",
                1,
                mapOf(
                    "com.hal1ucinogen.sbmchecker.MainActivity" to checkerMain,
                )
            )
        savePref(checkerConfig.packageName, checkerConfig)

        // 淘宝
        val tbE2E = PageConfig(edgeToEdge = true)
        val tbConfig = AppConfig(
            "com.taobao.taobao",
            1, mapOf(
                "com.taobao.mytaobao.newsetting.NewTaobaoSettingActivity" to tbE2E,
                "com.taobao.android.order.bundle.TBOrderListActivity" to tbE2E,
                "com.alibaba.triver.triver_shop.newShop.ShopActivity" to tbE2E,
                "com.taobao.android.detail2.core.framework.NewDetailActivity" to tbE2E
            )
        )
        savePref(tbConfig.packageName, tbConfig)

        // 电笠
        val dlE2E = PageConfig(edgeToEdge = true)
        val dlConfig = AppConfig("media.dl", 1, emptyMap(), general = GeneralConfig(dlE2E))
        savePref(dlConfig.packageName, dlConfig)

        // DouBan
        /*        val dlGeneral = PageConfig(edgeToEdge = true)
                val dbMain = PageConfig()
                val dbE2E = PageConfig(edgeToEdge = true)
                val dbConfig = AppConfig(
                    "com.douban.frodo", false, 1, mapOf(
                        "com.douban.frodo.MainActivity" to dbMain,
                        "com.douban.frodo.baseproject.image.ImageActivity" to dbE2E
                    ), general = GeneralConfig(dlGeneral)
                )
                savePref(dbConfig.packageName, dbConfig)*/

        // 饿了么
        val eleGeneralConfig = PageConfig(edgeToEdge = true)
        val eleConfig = AppConfig(
            "me.ele",
            1,
            mapOf(
                "me.ele.pha.shell.ui.ElePhaActivity" to eleGeneralConfig,
                "me.ele.muise.page.WeexPageActivity" to eleGeneralConfig,
                "me.ele.orderdetail.ui.lmagex.WMOrderDetailActivity" to eleGeneralConfig,
                "me.ele.newretail.emagex.activity.EMagexOrderDetailActivity" to eleGeneralConfig,
                "com.alibaba.triver.container.TriverMainActivity" to eleGeneralConfig
            )
        )
        savePref(eleConfig.packageName, eleConfig)
    }

    private fun savePref(group: String, config: AppConfig) {
        binding.config.append("\n\n$config")
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