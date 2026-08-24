package com.hal1ucinogen.systembarsmodernizer.feature.overview.ui

import android.graphics.Color
import android.widget.Toast
import androidx.core.content.edit
import com.hal1ucinogen.systembarsmodernizer.CONFIG_PREF_NAME
import com.hal1ucinogen.systembarsmodernizer.SBMApp
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.bean.ExtraAction
import com.hal1ucinogen.systembarsmodernizer.bean.GeneralConfig
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.databinding.FragmentOverviewBinding
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseFragment
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedService.OnScopeEventListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OverviewFragment : BaseFragment<FragmentOverviewBinding>(), SBMApp.ServiceStateListener {

    private var mService: XposedService? = null
    private val mCallback = object : OnScopeEventListener {
        override fun onScopeRequestApproved(approved: List<String>) {
            activity?.runOnUiThread {
                Toast.makeText(
                    this@OverviewFragment.requireActivity(),
                    "onScopeRequestApproved: $approved",
                    Toast.LENGTH_SHORT
                ).show()
                binding.scope.text = "Scope: " + mService?.scope
            }
        }

        override fun onScopeRequestFailed(message: String) {
            activity?.runOnUiThread {
                Toast.makeText(
                    this@OverviewFragment.requireActivity(),
                    "onScopeRequestFailed: $message",
                    Toast.LENGTH_SHORT
                ).show()
                binding.scope.text = "Scope: " + mService?.scope
            }
        }
    }

    override fun init() {
        val context = (context as? BaseActivity<*>) ?: return
        binding.binder.text = "Loading"
    }

    override fun onStart() {
        super.onStart()
        SBMApp.addServiceStateListener(this, true)
    }

    override fun onStop() {
        SBMApp.removeServiceStateListener(this)
        super.onStop()
    }

    override fun onServiceStateChanged(service: XposedService?) {
        mService = service
        activity?.runOnUiThread {
            if (service == null) {
                binding.binder.text = "Binder is null"
            } else {
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
        }

    }

    private fun savePrefs() {
        // Spotify
        val spPlay = PageConfig(edgeToEdge = false, clearTranslucent = true)
        val spConfig =
            AppConfig(
                "com.spotify.music",
                1,
                mapOf("com.spotify.nowplaying.musicinstallation.NowPlayingActivity" to spPlay)
            )
        savePref(spConfig.packageName, spConfig)

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
        val jdE2E = PageConfig(edgeToEdge = true)
        val jdConfig =
            AppConfig(
                "com.jingdong.app.mall",
                1,
                mapOf(),
                general = GeneralConfig(
                    jdE2E,
                    exclusive = listOf(
                        "com.jingdong.app.mall.MainFrameActivity",
                        "com.jd.lib.settlement.fillorder.activity.PopupNewFillOrderActivity",
                        "com.jd.lib.productdetail.ProductDetailPopActivity",
                        "com.jd.lib.productdetail.ProductDetailActivity"
                    )
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
        val tbDecorConfig = PageConfig(
            edgeToEdge = true,
            extraActions = listOf(
                ExtraAction(
                    viewId = "decor",
                    isGroup = true,
                    self = false,
                    childIndex = 0,
                    isTop = false,
                    isPadding = false,
                    useSystemInsets = false,
                    customInset = 0
                )
            )
        )
        val tbConfig = AppConfig(
            "com.taobao.taobao",
            1, mapOf(
                "com.taobao.tao.welcome.Welcome" to PageConfig(
                    navigationColor = Color.TRANSPARENT,
                    windowBackgroundColor = Color.WHITE
                ),
                "com.taobao.themis.container.app.TMSActivity" to tbDecorConfig,
                "com.taobao.android.detail2.core.framework.NewDetailActivity" to tbDecorConfig,
                "com.taobao.weex.weexv2.page.WeexV2Activity" to tbDecorConfig
            ), general = GeneralConfig(
                PageConfig(edgeToEdge = true), exclusive = listOf(
                    "com.taobao.android.detail.alittdetail.TTDetailActivity",
                    "com.taobao.android.purchase.aura.TBBuyActivity"
                )
            )
        )
        savePref(tbConfig.packageName, tbConfig)

        // 支付宝
        val navPlaceholderConfig = PageConfig(
            edgeToEdge = true,
            extraActions = listOf(
                ExtraAction(
                    viewId = "v_navbar_placeholder",
                    isGone = true
                )
            )
        )
        val decorConfig = PageConfig(
            edgeToEdge = true,
            extraActions = listOf(
                ExtraAction(
                    viewId = "decor",
                    isGroup = true,
                    self = false,
                    childIndex = 0,
                    isTop = false,
                    isPadding = true,
                    useSystemInsets = false,
                    customInset = 0
                )
            )
        )
        val aliPayConfig = AppConfig(
            "com.eg.android.AlipayGphone",
            1, mapOf(
                "com.alipay.mobile.nebulax.xriver.activity.XRiverActivity" to PageConfig(
                    edgeToEdge = true,
                    extraActions = listOf(
                        ExtraAction(
                            viewId = "nebulax_root_view",
                            isGroup = false,
                            isTop = false,
                            isPadding = true,
                            useSystemInsets = false,
                            customInset = 0
                        )
                    )
                ),
                "com.alipay.android.phone.msgboxapp.ui.activity.*" to navPlaceholderConfig,
                "com.alipay.mobile.antcardsdk.cardapp.CSPushActivity" to decorConfig,
                "com.alipay.mobile.socialcontactsdk.contact.ui.ContactMainPageActivity" to decorConfig,
                "com.alipay.mobile.chatapp.ui.PersonalChatMsgActivity_" to
                        PageConfig(
                            navigationColor = Color.WHITE,
                            windowBackgroundColor = Color.WHITE
                        )
            ), general = GeneralConfig(
                PageConfig(edgeToEdge = true),
                exclusive = listOf(
                    "com.eg.android.AlipayGphone.AlipayLogin",
                )
            )
        )
        savePref(aliPayConfig.packageName, aliPayConfig)


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
                    uiModeWBC = Pair(Color.WHITE, Color.parseColor("#111111"))
                ),
                // todo white bottom nav issue
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