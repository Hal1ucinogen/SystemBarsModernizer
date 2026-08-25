package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isGone
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig

data class ScopeRuleItem(
    val activityName: String,
    val config: PageConfig
)

class ScopeRuleAdapter(
    var isEditable: Boolean = true,
    private val onDeleteClick: ((ScopeRuleItem) -> Unit)? = null
) : BaseQuickAdapter<ScopeRuleItem, BaseViewHolder>(R.layout.item_scope_rule) {

    override fun convert(holder: BaseViewHolder, item: ScopeRuleItem) {
        val tvActivityName = holder.getView<TextView>(R.id.tv_activity_name)
        val badgeE2E = holder.getView<TextView>(R.id.badge_e2e)
        val badgeActionsCount = holder.getView<TextView>(R.id.badge_actions_count)
        val btnDelete = holder.getView<AppCompatImageButton>(R.id.btn_delete_rule)

        tvActivityName.text = item.activityName

        badgeE2E.isGone = !item.config.edgeToEdge

        val actionCount = item.config.extraActions.size
        if (actionCount > 0) {
            badgeActionsCount.isGone = false
            badgeActionsCount.text = "动作: $actionCount"
        } else {
            badgeActionsCount.isGone = true
        }

        btnDelete.isGone = !isEditable
        if (isEditable) {
            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(item)
            }
        }
    }
}
