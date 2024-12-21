package com.hal1ucinogen.systembarsmodernizer.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.TintTypedArray

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
    val height: Int = try {
        val resources = this.resources
        val resourceId =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        resources.getDimensionPixelSize(resourceId)
    } catch (e: Exception) {
        0
    }
    return height
}

@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun Activity.getStatusHeight(): Int {
    val height: Int = try {
        val resources = this.resources
        val resourceId =
            resources.getIdentifier("status_bar_height", "dimen", "android")
        resources.getDimensionPixelSize(resourceId)
    } catch (e: Exception) {
        0
    }
    return height
}
