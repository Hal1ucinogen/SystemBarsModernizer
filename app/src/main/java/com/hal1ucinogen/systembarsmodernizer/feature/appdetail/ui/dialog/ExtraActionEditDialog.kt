package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.bean.ExtraAction
import com.hal1ucinogen.systembarsmodernizer.databinding.DialogExtraActionEditBinding

class ExtraActionEditDialog(
    private val initialAction: ExtraAction? = null,
    private val onActionSaved: (ExtraAction) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogExtraActionEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExtraActionEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init values if editing existing action
        initialAction?.let { action ->
            binding.etViewId.setText(action.viewId)

            when {
                action.isGone -> binding.rbGone.isChecked = true
                action.isPadding -> binding.rbPadding.isChecked = true
                else -> binding.rbMargin.isChecked = true
            }

            if (action.isTop) {
                binding.rbStatusBar.isChecked = true
            } else {
                binding.rbNavBar.isChecked = true
            }

            binding.switchUseSystemInsets.isChecked = action.useSystemInsets
            if (action.customInset >= 0) {
                binding.etCustomInset.setText(action.customInset.toString())
            }

            binding.switchIsGroup.isChecked = action.isGroup
            binding.switchSelf.isChecked = action.self
            if (action.childIndex >= 0) {
                binding.etChildIndex.setText(action.childIndex.toString())
            }
            binding.etDelay.setText(action.delay.toString())
            if (action.routes.isNotEmpty()) {
                binding.etRoutes.setText(action.routes.joinToString(", "))
            }
            binding.switchRouteExclusive.isChecked = action.isRouteExclusive
        } ?: run {
            binding.etDelay.setText("100")
        }

        fun updateVisibility() {
            val isGone = binding.rbGone.isChecked
            binding.layoutBarDirection.visibility = if (isGone) View.GONE else View.VISIBLE
            binding.layoutInsetSettings.visibility = if (isGone) View.GONE else View.VISIBLE
        }

        binding.rgActionType.setOnCheckedChangeListener { _, _ -> updateVisibility() }
        updateVisibility()

        binding.btnCancelAction.setOnClickListener {
            dismiss()
        }

        binding.btnSaveAction.setOnClickListener {
            val viewId = binding.etViewId.text?.toString().orEmpty().trim()
            if (viewId.isEmpty()) {
                binding.tilViewId.error = "View ID cannot be empty"
                return@setOnClickListener
            }

            val isGone = binding.rbGone.isChecked
            val isPadding = binding.rbPadding.isChecked
            val isTop = binding.rbStatusBar.isChecked
            val useSystemInsets = binding.switchUseSystemInsets.isChecked
            val customInset = binding.etCustomInset.text?.toString()?.toIntOrNull() ?: 0
            val isGroup = binding.switchIsGroup.isChecked
            val self = binding.switchSelf.isChecked
            val childIndex = binding.etChildIndex.text?.toString()?.toIntOrNull() ?: -1
            val delay = binding.etDelay.text?.toString()?.toLongOrNull() ?: 100L
            val routesStr = binding.etRoutes.text?.toString().orEmpty().trim()
            val routes = if (routesStr.isNotEmpty()) {
                routesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else emptyList()
            val isRouteExclusive = binding.switchRouteExclusive.isChecked

            val action = ExtraAction(
                viewId = viewId,
                isGroup = isGroup,
                isTop = isTop,
                isPadding = isPadding,
                useSystemInsets = useSystemInsets,
                customInset = customInset,
                self = self,
                childIndex = childIndex,
                isGone = isGone,
                delay = delay,
                routes = routes,
                isRouteExclusive = isRouteExclusive
            )

            onActionSaved(action)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
