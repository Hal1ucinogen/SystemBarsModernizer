package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hal1ucinogen.systembarsmodernizer.bean.ExtraAction
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.databinding.DialogPageConfigEditBinding
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ExtraActionAdapter

class PageConfigEditDialog(
    private val initialActivityName: String,
    private val initialConfig: PageConfig? = null,
    private val packageName: String? = null,
    private val declaredActivities: List<String>? = null,
    private val onConfigSaved: (String, PageConfig) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogPageConfigEditBinding? = null
    private val binding get() = _binding!!

    private var currentActivityName: String = initialActivityName
    private lateinit var extraActionAdapter: ExtraActionAdapter
    private val extraActionsList = mutableListOf<ExtraAction>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPageConfigEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTargetActivity.text = currentActivityName

        if (packageName != null && declaredActivities != null) {
            binding.btnChangeActivity.visibility = View.VISIBLE
            binding.btnChangeActivity.setOnClickListener {
                ActivityPickerDialog(packageName, declaredActivities) { selectedActivity ->
                    currentActivityName = selectedActivity
                    binding.tvTargetActivity.text = currentActivityName
                }.show(childFragmentManager, "ActivityPickerDialog_Change")
            }
        } else {
            binding.btnChangeActivity.visibility = View.GONE
        }

        initialConfig?.let { config ->
            binding.switchE2e.isChecked = config.edgeToEdge
            binding.switchClearTranslucent.isChecked = config.clearTranslucent
            config.windowBackgroundColor?.let { color ->
                binding.etWindowBgColor.setText(String.format("#%06X", 0xFFFFFF and color))
            }
            extraActionsList.addAll(config.extraActions)
        } ?: run {
            binding.switchE2e.isChecked = true
        }

        extraActionAdapter = ExtraActionAdapter { position ->
            if (position in extraActionsList.indices) {
                extraActionsList.removeAt(position)
                extraActionAdapter.removeAt(position)
                updateEmptyState()
            }
        }

        extraActionAdapter.setOnItemClickListener { _, _, position ->
            if (position in extraActionsList.indices) {
                ExtraActionEditDialog(extraActionsList[position]) { updatedAction ->
                    extraActionsList[position] = updatedAction
                    extraActionAdapter.setList(extraActionsList)
                }.show(childFragmentManager, "ExtraActionEditDialog_Edit")
            }
        }

        binding.rvExtraActions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExtraActions.adapter = extraActionAdapter
        extraActionAdapter.setList(extraActionsList)
        updateEmptyState()

        binding.btnAddExtraAction.setOnClickListener {
            ExtraActionEditDialog(null) { newAction ->
                extraActionsList.add(newAction)
                extraActionAdapter.setList(extraActionsList)
                updateEmptyState()
            }.show(childFragmentManager, "ExtraActionEditDialog")
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSavePageConfig.setOnClickListener {
            val edgeToEdge = binding.switchE2e.isChecked
            val clearTranslucent = binding.switchClearTranslucent.isChecked
            val bgColorStr = binding.etWindowBgColor.text?.toString().orEmpty().trim()
            val windowBackgroundColor = if (bgColorStr.isNotEmpty()) {
                runCatching { Color.parseColor(bgColorStr) }.getOrNull()
            } else null

            val updatedConfig = PageConfig(
                edgeToEdge = edgeToEdge,
                clearTranslucent = clearTranslucent,
                windowBackgroundColor = windowBackgroundColor,
                extraActions = extraActionsList.toList()
            )

            onConfigSaved(currentActivityName, updatedConfig)
            dismiss()
        }
    }

    private fun updateEmptyState() {
        binding.tvEmptyExtraActions.visibility = if (extraActionsList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
