package com.hal1ucinogen.systembarsmodernizer.ui.base

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.util.OsUtil
import rikka.material.app.MaterialActivity

abstract class BaseActivity<VB : ViewBinding> : MaterialActivity(), IBinding<VB> {

    override lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = (inflateBinding(layoutInflater) as VB).also {
            setContentView(it.root)
        }
    }

    override fun shouldApplyTranslucentSystemBars(): Boolean = true

    override fun computeUserThemeKey(): String? {
        return "system"
    }

    override fun onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars()
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.post {
            window.navigationBarColor = Color.TRANSPARENT
            if (OsUtil.atLeastQ()) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        theme.applyStyle(R.style.ThemeOverlay, true)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference,
            true
        )
    }
}