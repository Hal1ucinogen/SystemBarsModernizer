package com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.adapter

import android.view.ViewGroup
import coil.load
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.view.AppItemView
import com.hal1ucinogen.systembarsmodernizer.ui.adapter.HighlightAdapter
import com.hal1ucinogen.systembarsmodernizer.util.PackageUtils
import com.hal1ucinogen.systembarsmodernizer.util.addStrikeThroughSpan
import com.hal1ucinogen.systembarsmodernizer.util.dp

class AppAdapter : HighlightAdapter<SBMItem>() {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return createBaseViewHolder(
            AppItemView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        )
    }

    override fun convert(holder: BaseViewHolder, item: SBMItem) {
        val view = holder.itemView as AppItemView
        with(view.container) {
            val packageInfo = runCatching {
                PackageUtils.getPackageInfo(item.packageName)
            }.getOrNull()
            icon.load(packageInfo ?: item) {
                crossfade(true)
            }
            setOrHighlightText(appName, item.label)
            setOrHighlightText(packageName, item.packageName)
            if (packageInfo == null) {
                appName.addStrikeThroughSpan()
                packageName.addStrikeThroughSpan()
            }
            
            versionInfo.text = PackageUtils.getVersionString(item.versionName, item.versionCode)
            
            if (packageInfo == null) {
                appName.addStrikeThroughSpan()
                packageName.addStrikeThroughSpan()
                view.alpha = 0.6f
                setStatusBadge("已卸载", isPrimary = false)
            } else {
                view.alpha = 1.0f
                val config = item.config
                if (config != null) {
                    val scopeCount = config.scope.size
                    val badgeText = if (config.general != null && scopeCount == 0) {
                        "全局边到边"
                    } else if (config.general != null) {
                        "全局 + $scopeCount 页"
                    } else {
                        "规则 ($scopeCount)"
                    }
                    setStatusBadge(badgeText, isPrimary = true)
                } else if (item.isSystem) {
                    setStatusBadge("系统", isPrimary = false)
                } else {
                    setStatusBadge(null)
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        if (data.isEmpty() || position >= data.size) {
            return super.getItemId(position)
        }
        return data[position].hashCode().toLong()
    }
}
