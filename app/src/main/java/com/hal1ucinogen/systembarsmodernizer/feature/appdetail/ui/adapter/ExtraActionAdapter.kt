package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter

import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isGone
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.bean.ExtraAction

class ExtraActionAdapter(
    var isEditable: Boolean = true,
    private val onDeleteClick: ((Int) -> Unit)? = null
) : BaseQuickAdapter<ExtraAction, BaseViewHolder>(R.layout.item_extra_action) {

    override fun convert(holder: BaseViewHolder, item: ExtraAction) {
        val tvViewId = holder.getView<TextView>(R.id.tv_view_id)
        val tvSummary = holder.getView<TextView>(R.id.tv_action_summary)
        val btnDelete = holder.getView<AppCompatImageButton>(R.id.btn_delete_action)

        val idDisplay = if (item.childIndex >= 0) {
            "${item.viewId} [child: ${item.childIndex}]"
        } else {
            item.viewId
        }
        tvViewId.text = idDisplay

        val summary = buildString {
            if (item.isGone) {
                append("GONE")
            } else {
                append(if (item.isPadding) "Padding" else "Margin")
                append(" • ")
                append(if (item.isTop) "Top" else "Bottom")
                append(" • ")
                if (item.useSystemInsets) {
                    append("System Inset")
                } else {
                    append("${item.customInset}px")
                }
            }
            if (item.isGroup) {
                append(" • Group")
            }
            if (item.delay != 100L) {
                append(" • ${item.delay}ms")
            }
            if (item.routes.isNotEmpty()) {
                append(if (item.isRouteExclusive) " • Exclude: " else " • Include: ")
                append(item.routes.joinToString(", "))
            }
        }
        tvSummary.text = summary

        btnDelete.isGone = !isEditable
        if (isEditable) {
            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(holder.bindingAdapterPosition)
            }
        }
    }
}
