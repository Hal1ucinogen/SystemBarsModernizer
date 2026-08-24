package com.hal1ucinogen.systembarsmodernizer.util

import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.TextView

val Number.dp: Int get() = (toInt() * Resources.getSystem().displayMetrics.density).toInt()

fun TextView.tintHighlightText(highlightText: String, rawText: CharSequence) {
    text = rawText
    if (text.contains(highlightText, true)) {
        val builder = SpannableStringBuilder()
        val spannableString = SpannableString(text.toString())
        val start = text.indexOf(highlightText, 0, true)
        val color = context.getColorByAttr(com.google.android.material.R.attr.colorPrimary)
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start,
            start + highlightText.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        builder.append(spannableString)
        text = builder
    }
}

fun TextView.addStrikeThroughSpan() {
    val span = SpannableString(text)
    span.setSpan(StrikethroughSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = span
}

fun View.isRtl(): Boolean {
    return layoutDirection == View.LAYOUT_DIRECTION_RTL
}