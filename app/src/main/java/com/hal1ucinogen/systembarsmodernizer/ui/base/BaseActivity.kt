package com.hal1ucinogen.systembarsmodernizer.ui.base

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.util.OsUtils
import rikka.material.app.MaterialActivity

abstract class BaseActivity<VB : ViewBinding> : MaterialActivity(), IBinding<VB> {

    override lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySystemBarsAppearance()
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
        applySystemBarsAppearance()
        window.decorView.post {
            window.navigationBarColor = Color.TRANSPARENT
            if (OsUtils.atLeastQ()) {
                window.isNavigationBarContrastEnforced = false
            }
            applySystemBarsAppearance()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemBarsAppearance()
    }

    fun applySystemBarsAppearance() {
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNight
            isAppearanceLightNavigationBars = !isNight
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