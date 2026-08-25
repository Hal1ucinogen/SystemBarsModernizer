package com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem

class AppListDiffUtil : DiffUtil.ItemCallback<SBMItem>() {

    override fun areItemsTheSame(oldItem: SBMItem, newItem: SBMItem): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: SBMItem, newItem: SBMItem): Boolean {
        return oldItem.label == newItem.label &&
                oldItem.versionName == newItem.versionName &&
                oldItem.lastUpdatedTime == newItem.lastUpdatedTime
    }
}
