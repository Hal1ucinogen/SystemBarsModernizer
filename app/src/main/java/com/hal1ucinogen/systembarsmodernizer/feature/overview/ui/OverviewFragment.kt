package com.hal1ucinogen.systembarsmodernizer.feature.overview.ui

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import com.hal1ucinogen.systembarsmodernizer.COLOR_INT_UI_MODE_NIGHT
import com.hal1ucinogen.systembarsmodernizer.CONFIG_PREF_NAME
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.bean.ExtraAction
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
        val mainPage = PageConfig(edgeToEdge = false, windowBackgroundColor = Color.WHITE)
        val matchPage = PageConfig(edgeToEdge = false, windowBackgroundColor = Color.WHITE)
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
                    PageConfig(edgeToEdge = false, windowBackgroundColor = Color.WHITE),
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
                general = GeneralConfig(
                    jdMain,
                    exclusive = listOf("com.jd.lib.videoimmersionstyleb.view.activity.VideoImmersionStyleBActivity")
                )
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
                "com.taobao.mytaobao.newsetting.dx.DxSettingCommonActivity" to tbE2E,
                "com.taobao.android.order.bundle.TBOrderListActivity" to tbE2E,
                "com.alibaba.triver.triver_shop.newShop.ShopActivity" to tbE2E,
                "com.taobao.android.detail2.core.framework.NewDetailActivity" to tbE2E,
                "com.taobao.search.sf.MainSearchResultActivity" to PageConfig(windowBackgroundColor = Color.WHITE)
            )
        )
        savePref(tbConfig.packageName, tbConfig)

        val mmE2E = PageConfig(edgeToEdge = true)
        val mmConfig = AppConfig(
            "com.tencent.mm",
            1, mapOf(
//                "com.tencent.mm.plugin.sns.ui.improve.ImproveSnsTimelineUI" to mmE2E,// 朋友圈
                "com.tencent.mm.plugin.sns.ui.SnsUserUI" to mmE2E,// 我的朋友圈
                "com.tencent.mm.plugin.webview.ui.tools.MMWebViewUI" to mmE2E,// 网页
                "com.tencent.mm.plugin.subapp.ui.gallery.GestureGalleryUI" to mmE2E,// 网页图片预览
                "com.tencent.mm.plugin.appbrand.ui.AppBrandUI00" to mmE2E,// 小程序
                "com.tencent.mm.plugin.appbrand.ui.AppBrandUI01" to mmE2E,
                "com.tencent.mm.plugin.appbrand.ui.AppBrandUI02" to mmE2E,
                "com.tencent.mm.plugin.appbrand.ui.AppBrandUI03" to mmE2E,
                "com.tencent.mm.plugin.appbrand.ui.AppBrandUIMoveTaskToBackStubUI0" to mmE2E// 小程序转场
            )
        )
        savePref(mmConfig.packageName, mmConfig)

        // 电笠
        val dlE2E = PageConfig(edgeToEdge = true)
        val dlConfig = AppConfig("media.dl", 1, emptyMap(), general = GeneralConfig(dlE2E))
        savePref(dlConfig.packageName, dlConfig)

        // 小黑盒
        val bbGeneral = PageConfig(edgeToEdge = true)
        val bbConfig = AppConfig(
            "com.max.xiaoheihe", 1,
            mapOf(
                "com.max.xiaoheihe.MainActivity" to PageConfig(
                    edgeToEdge = false,
                    windowBackgroundColor = COLOR_INT_UI_MODE_NIGHT,
                    uiModeWBC = Pair(Color.WHITE, Color.parseColor("#111111"))
                ),
                "com.max.xiaoheihe.module.bbs.ChannelsDetailActivity" to bbGeneral,
                "com.max.xiaoheihe.module.webview.WebActionActivity" to bbGeneral,
                // view is right but setting padding isn't work
/*                "com.max.xiaoheihe.module.miniprogram.MiniProgramHostActivity" to bbGeneral.copy(windowBackgroundColor = Color.CYAN,
                    extraActions = listOf(
                        ExtraAction("vg_webview_container", false, false, true, false, 0,true)
                    )
                )*/
            ),
        )
        savePref(bbConfig.packageName, bbConfig)

        // DouBan
        val dbE2E = PageConfig(edgeToEdge = true)
        val dbImageWithActionConfig =
            PageConfig(
                edgeToEdge = true,
                extraActions = listOf(
                    ExtraAction(/*"social_bar"*/"social_action_bar",
                        true,
                        false,
                        false,
                        true,
                        self = true
                    )
                )
            )
        val dbWebConfig = PageConfig(
            edgeToEdge = true,
            extraActions = listOf(
                ExtraAction(
                    "base_ui_actionbar_layout",
                    true,
                    true,
                    false,
                    false,
                    0,
                    true
                )
            )
        )

        val dbConfig = AppConfig(
            "com.douban.frodo",
            1,
            mapOf(
                "com.douban.frodo.baseproject.image.ImageActivity" to dbE2E,// 普通图片页
                "com.douban.frodo.baseproject.image.SociableImageActivity" to dbImageWithActionConfig, // 带按钮的图片页
                "com.douban.frodo.profile.activity.NewUserProfileActivity" to dbE2E
            ),
            /*general = GeneralConfig(
                dlE2E,
                exclusive = listOf(
                    "com.douban.frodo.MainActivity",// 主页
                    "com.douban.frodo.subject.structure.activity.MovieActivity", // 电影详情页
                    "com.douban.frodo.chat.activity.ChatActivity", // 聊天对话页
                    "com.douban.frodo.subject.activity.ReviewActivity", // 剧评详情页
                    "com.douban.frodo.subject.activity.SubjectWishManageTabActivity",
                    "com.douban.frodo.baseproject.activity.WebActivity"
                )
            )*/
        )
        savePref(dbConfig.packageName, dbConfig)

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
                "com.alibaba.triver.container.TriverMainActivity" to eleGeneralConfig,
                "me.ele.component.webcontainer.view.AppUCWebActivity" to eleGeneralConfig.copy(
                    extraActions = listOf(
                        ExtraAction("comp_uc_container", true, false, true, false, 0, false, 0)
                    )
                )
            )
        )
        savePref(eleConfig.packageName, eleConfig)

        // UnionPay
        val unionPayPageConfig = PageConfig(edgeToEdge = true)
        val unionPagConfig = AppConfig(
            "com.unionpay",
            1,
            mapOf(
                "com.unionpay.activity.message.UPActivityMesssage" to unionPayPageConfig,
//                "com.unionpay.liteapp.app.UPLiteAppActivity1" to unionPayPageConfig,// Not work
//                "com.unionpay.liteapp.app.UPLiteAppActivity2" to unionPayPageConfig,// Not work
//                "com.unionpay.cordova.UPActivityCordovaWeb" to unionPayPageConfig,// Not work
                "com.unionpay.activity.react.UPActivityReactNative" to unionPayPageConfig,
            )
        )
        savePref(unionPagConfig.packageName, unionPagConfig)

        // Bili
        val biliPageConfig = PageConfig(edgeToEdge = true)
        val biliConfig = AppConfig(
            "com.bilibili.app.in",
            1,
            mapOf(
                "tv.danmaku.bili.ui.webview.MWebActivity" to biliPageConfig
            )
        )
        savePref(biliConfig.packageName, biliConfig)
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