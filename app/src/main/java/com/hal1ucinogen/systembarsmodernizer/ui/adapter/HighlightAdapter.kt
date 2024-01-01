package com.hal1ucinogen.systembarsmodernizer.ui.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hal1ucinogen.systembarsmodernizer.util.tintHighlightText

abstract class HighlightAdapter<T> : BaseQuickAdapter<T, BaseViewHolder>(0) {

    var highlightText: String = ""

    protected fun setOrHighlightText(view: TextView, text: CharSequence) {
        if (highlightText.isNotBlank()) {
            view.tintHighlightText(highlightText, text)
        } else {
            view.text = text
        }
    }
}