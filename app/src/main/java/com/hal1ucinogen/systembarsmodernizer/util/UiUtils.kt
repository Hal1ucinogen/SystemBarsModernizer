package com.hal1ucinogen.systembarsmodernizer.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.absinthe.libraries.utils.utils.UiUtils
import com.hal1ucinogen.systembarsmodernizer.app.SystemServices
import com.hal1ucinogen.systembarsmodernizer.constant.Constants
import com.hal1ucinogen.systembarsmodernizer.constant.GlobalValues

object UiUtils {
    fun getRandomColor(): Int {
        val range = if (UiUtils.isDarkMode()) {
            (68..136)
        } else {
            (132..200)
        }
        val r = range.random()
        val g = range.random()
        val b = range.random()

        return Color.parseColor(String.format("#%02x%02x%02x", r, g, b))
    }

    fun isDarkColor(@ColorInt color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val brightness = (r * 299 + g * 587 + b * 114) / 1000
        return brightness >= 192
    }

    fun getNightMode(): Int {
        return when (GlobalValues.darkMode) {
            Constants.DARK_MODE_OFF -> AppCompatDelegate.MODE_NIGHT_NO
            Constants.DARK_MODE_ON -> AppCompatDelegate.MODE_NIGHT_YES
            Constants.DARK_MODE_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun isSoftInputOpen(): Boolean {
        return SystemServices.inputMethodManager.isActive
    }

//  fun createLoadingDialog(context: ContextThemeWrapper): AlertDialog {
//    return BaseAlertDialogBuilder(context)
//      .setView(
//        LinearProgressIndicator(context).apply {
//          layoutParams = ViewGroup.LayoutParams(200.dp, ViewGroup.LayoutParams.WRAP_CONTENT).also {
//            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
//          }
//          trackCornerRadius = 3.dp
//          isIndeterminate = true
//        }
//      )
//      .setCancelable(false)
//      .create()
//  }

    @Suppress("DEPRECATION")
    fun getScreenAspectRatio(): Float {
        val displayMetrics = DisplayMetrics()
        SystemServices.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        return width.toFloat() / height.toFloat()
    }

    fun hasHinge() = if (OsUtils.atLeastR()) {
        SystemServices.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
    } else {
        false
    }

    fun changeDrawableColor(context: Context, drawableResId: Int, color: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
            ?: throw IllegalArgumentException("Drawable is null")
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_ATOP)
        DrawableCompat.setTint(drawable, color)
        return drawable
    }

    fun createBadge(
        context: Context,
        text: CharSequence,
        isPrimary: Boolean = false,
        isError: Boolean = false
    ): android.widget.TextView {
        val density = context.resources.displayMetrics.density
        val hPad = (8 * density).toInt()
        val vPad = (3 * density).toInt()
        return android.widget.TextView(context).apply {
            this.text = text
            textSize = 11f
            typeface = android.graphics.Typeface.create(if (isPrimary) "sans-serif-medium" else "sans-serif", android.graphics.Typeface.NORMAL)
            setBackgroundResource(if (isPrimary) com.hal1ucinogen.systembarsmodernizer.R.drawable.bg_badge_primary else com.hal1ucinogen.systembarsmodernizer.R.drawable.bg_badge_secondary)
            setPadding(hPad, vPad, hPad, vPad)
            val typedValue = android.util.TypedValue()
            val colorAttr = when {
                isPrimary -> com.google.android.material.R.attr.colorOnPrimaryContainer
                isError -> com.google.android.material.R.attr.colorError
                else -> com.google.android.material.R.attr.colorOnSurfaceVariant
            }
            context.theme.resolveAttribute(colorAttr, typedValue, true)
            setTextColor(typedValue.data)
        }
    }
}
