package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.databinding.DialogPageConfigDetailBinding
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ExtraActionAdapter

class PageConfigDetailDialog(
    private val activityName: String,
    private val config: PageConfig
) : BottomSheetDialogFragment() {

    private var _binding: DialogPageConfigDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPageConfigDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTargetActivity.text = activityName

        // Populate badges
        binding.chipGroupProperties.removeAllViews()

        // 1. Edge-to-Edge
        val e2eChip = Chip(requireContext()).apply {
            text = if (config.edgeToEdge) {
                getString(R.string.state_global_e2e_enabled)
            } else {
                getString(R.string.state_global_e2e_disabled)
            }
            isEnabled = false
        }
        binding.chipGroupProperties.addView(e2eChip)

        // 2. Window Background Color
        config.windowBackgroundColor?.let { color ->
            val colorChip = Chip(requireContext()).apply {
                text = String.format("窗口背景: #%06X", 0xFFFFFF and color)
                isEnabled = false
            }
            binding.chipGroupProperties.addView(colorChip)
        }

        // 3. Clear Translucent
        if (config.clearTranslucent) {
            val clearChip = Chip(requireContext()).apply {
                text = getString(R.string.switch_clear_translucent)
                isEnabled = false
            }
            binding.chipGroupProperties.addView(clearChip)
        }

        // 4. Extra Actions List (Read-Only)
        val extraActionAdapter = ExtraActionAdapter(isEditable = false)
        binding.rvExtraActions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExtraActions.adapter = extraActionAdapter
        extraActionAdapter.setList(config.extraActions.toMutableList())

        binding.tvEmptyExtraActions.visibility =
            if (config.extraActions.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
