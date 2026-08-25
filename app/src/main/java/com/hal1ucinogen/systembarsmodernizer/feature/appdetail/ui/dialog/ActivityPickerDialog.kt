package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.databinding.DialogActivityPickerBinding

class ActivityPickerDialog(
    private val packageName: String,
    private val activities: List<String>,
    private val onActivitySelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogActivityPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ActivityPickerAdapter
    private var filteredList: List<String> = activities

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogActivityPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ActivityPickerAdapter { selectedActivity ->
            onActivitySelected(selectedActivity)
            dismiss()
        }

        binding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivities.adapter = adapter
        adapter.setList(filteredList.toMutableList())

        binding.etSearchActivity.doAfterTextChanged { text ->
            val query = text?.toString().orEmpty().trim()
            if (query.isEmpty()) {
                filteredList = activities
                binding.btnUseCustomInput.visibility = View.GONE
            } else {
                filteredList = activities.filter { it.contains(query, ignoreCase = true) }
                binding.btnUseCustomInput.visibility = View.VISIBLE
                binding.btnUseCustomInput.text = "使用 \"$query\""
            }
            adapter.setList(filteredList.toMutableList())
        }

        binding.btnUseCustomInput.setOnClickListener {
            val custom = binding.etSearchActivity.text?.toString().orEmpty().trim()
            if (custom.isNotEmpty()) {
                val finalName = if (custom.startsWith(".") && !custom.startsWith(packageName)) {
                    "$packageName$custom"
                } else {
                    custom
                }
                onActivitySelected(finalName)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ActivityPickerAdapter(
        private val onItemClick: (String) -> Unit
    ) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_activity_picker) {

        override fun convert(holder: BaseViewHolder, item: String) {
            val tvSimpleName = holder.getView<TextView>(R.id.tv_simple_name)
            val tvFullName = holder.getView<TextView>(R.id.tv_full_name)

            val simpleName = item.substringAfterLast(".")
            tvSimpleName.text = simpleName
            tvFullName.text = item

            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
