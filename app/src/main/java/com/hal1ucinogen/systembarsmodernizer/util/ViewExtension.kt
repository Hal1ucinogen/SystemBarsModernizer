package com.hal1ucinogen.systembarsmodernizer.util

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView

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