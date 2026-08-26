package com.hal1ucinogen.systembarsmodernizer.feature.overview.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.databinding.DialogScopeListBinding
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.AppDetailActivity
import com.hal1ucinogen.systembarsmodernizer.util.PackageUtils

class ScopeListDialog(
    private val scopePackages: List<String>
) : BottomSheetDialogFragment() {

    private var _binding: DialogScopeListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogScopeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCount.text = getString(R.string.scope_dialog_count, scopePackages.size)

        val pm = requireContext().packageManager
        val appItems = scopePackages.map { pkg ->
            val packageInfo = runCatching {
                PackageUtils.getPackageInfo(pkg)
            }.getOrNull()

            val name = runCatching {
                if (packageInfo?.applicationInfo != null) {
                    pm.getApplicationLabel(packageInfo.applicationInfo!!).toString()
                } else {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                }
            }.getOrElse { pkg }

            ScopeAppItem(packageName = pkg, appName = name, packageInfo = packageInfo)
        }.sortedBy { it.appName.lowercase() }

        val adapter = ScopeListAdapter { pkg ->
            val intent = Intent(requireContext(), AppDetailActivity::class.java).apply {
                putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, pkg)
            }
            startActivity(intent)
            dismiss()
        }

        binding.rvScopeApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScopeApps.adapter = adapter
        adapter.submitList(appItems)

        binding.tvEmptyScope.visibility = if (appItems.isEmpty()) View.VISIBLE else View.GONE
        binding.rvScopeApps.visibility = if (appItems.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
