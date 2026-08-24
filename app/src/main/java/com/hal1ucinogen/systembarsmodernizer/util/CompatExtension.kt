package com.hal1ucinogen.systembarsmodernizer.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.TintTypedArray
import androidx.core.content.ContextCompat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ColorInt
fun @receiver:ColorRes Int.getColor(context: Context): Int = ContextCompat.getColor(context, this)

fun @receiver:DrawableRes Int.getDrawable(context: Context): Drawable? =
    ContextCompat.getDrawable(context, this)

@OptIn(ExperimentalContracts::class)
@SuppressLint("RestrictedApi")
inline fun <R> TintTypedArray.use(block: (TintTypedArray) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        recycle()
    }
}