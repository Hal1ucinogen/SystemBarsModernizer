package com.hal1ucinogen.systembarsmodernizer.feature.overview.ui.dialog

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hal1ucinogen.systembarsmodernizer.databinding.ItemScopeAppBinding

data class ScopeAppItem(
    val packageName: String,
    val appName: String,
    val packageInfo: PackageInfo? = null
)

class ScopeListAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ScopeListAdapter.ViewHolder>() {

    private var items: List<ScopeAppItem> = emptyList()

    fun submitList(newItems: List<ScopeAppItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScopeAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvAppName.text = item.appName
        holder.binding.tvPackageName.text = item.packageName

        if (item.packageInfo != null) {
            holder.binding.ivAppIcon.load(item.packageInfo) {
                crossfade(true)
            }
        } else {
            val icon = runCatching {
                holder.itemView.context.packageManager.getApplicationIcon(item.packageName)
            }.getOrElse {
                ContextCompat.getDrawable(holder.itemView.context, android.R.drawable.sym_def_app_icon)
            }
            holder.binding.ivAppIcon.setImageDrawable(icon)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item.packageName)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemScopeAppBinding) : RecyclerView.ViewHolder(binding.root)
}
