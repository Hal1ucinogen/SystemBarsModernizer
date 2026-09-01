package com.hal1ucinogen.systembarsmodernizer.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.TintTypedArray

fun Context.getDimensionPixelSize(@DimenRes id: Int) = resources.getDimensionPixelSize(id)

fun Context.getColorByAttr(@AttrRes attr: Int): Int =
    getColorStateListByAttr(attr).defaultColor

@SuppressLint("RestrictedApi")
fun Context.getColorStateListByAttr(@AttrRes attr: Int): ColorStateList =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getColorStateList(0) }


@SuppressLint("RestrictedApi")
fun Context.obtainStyledAttributesCompat(
    set: AttributeSet? = null,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): TintTypedArray =
    TintTypedArray.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes)

@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun Activity.getNavigationHeight(): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insets = window.decorView.rootWindowInsets?.getInsets(
            WindowInsets.Type.navigationBars()
        )
        if (insets != null && insets.bottom > 0) {
            return insets.bottom
        }
    }
    val height: Int = try {
        val resources = this.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    } catch (e: Exception) {
        0
    }
    return height
}

@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun Activity.getStatusHeight(): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insets = window.decorView.rootWindowInsets?.getInsets(
            WindowInsets.Type.statusBars()
        )
        if (insets != null && insets.top > 0) {
            return insets.top
        }
    }
    val height: Int = try {
        val resources = this.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    } catch (e: Exception) {
        0
    }
    return height
}
