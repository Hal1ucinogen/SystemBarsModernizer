package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.chip.Chip
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.SBMApp
import com.hal1ucinogen.systembarsmodernizer.databinding.ActivityAppDetailBinding
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ScopeRuleAdapter
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ScopeRuleItem
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.viewmodel.AppDetailViewModel
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.util.PackageUtils

class AppDetailActivity : BaseActivity<ActivityAppDetailBinding>() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    private lateinit var targetPackageName: String

    private val viewModel: AppDetailViewModel by viewModels {
        AppDetailViewModel.Factory((application as SBMApp).repository, targetPackageName)
    }

    private lateinit var scopeRuleAdapter: ScopeRuleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        targetPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
        if (targetPackageName.isEmpty()) {
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Scope rules in read-only mode
        scopeRuleAdapter = ScopeRuleAdapter(isEditable = false)
        scopeRuleAdapter.setOnItemClickListener { _, _, position ->
            val item = scopeRuleAdapter.getItem(position)
            com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog.PageConfigDetailDialog(
                item.activityName,
                item.config
            ).show(supportFragmentManager, "PageConfigDetailDialog")
        }
        binding.rvScopeRules.layoutManager = LinearLayoutManager(this)
        binding.rvScopeRules.adapter = scopeRuleAdapter

        // Launch Edit page from Empty State
        binding.btnCreateConfig.setOnClickListener {
            startEditActivity()
        }

        // Launch Edit page from FAB
        binding.fabEditConfig.setOnClickListener {
            startEditActivity()
        }
    }

    private fun startEditActivity() {
        val intent = Intent(this, AppConfigEditActivity::class.java).apply {
            putExtra(AppConfigEditActivity.EXTRA_PACKAGE_NAME, targetPackageName)
        }
        startActivity(intent)
    }

    private fun observeViewModel() {
        viewModel.appItem.observe(this) { item ->
            if (item == null) return@observe

            // Header Info
            binding.ivAppIcon.load(item) {
                crossfade(true)
            }
            binding.tvAppName.text = item.label
            binding.tvPackageName.text = item.packageName
            val versionStr = PackageUtils.getVersionString(item.versionName, item.versionCode)
            binding.tvVersionInfo.text = "$versionStr • Target API ${item.targetApi}"

            val config = item.config
            val hasGeneral = config?.general != null
            val hasScope = config?.scope?.isNotEmpty() == true
            val hasConfig = hasGeneral || hasScope

            if (!hasConfig) {
                // No configuration exists or both general and scope are empty
                binding.cardEmptyConfig.visibility = View.VISIBLE
                binding.cardGeneralConfig.visibility = View.GONE
                binding.cardScopeConfig.visibility = View.GONE
                binding.fabEditConfig.visibility = View.GONE
            } else {
                // At least one valid configuration exists
                binding.cardEmptyConfig.visibility = View.GONE
                binding.fabEditConfig.visibility = View.VISIBLE

                // General Config Card: only show when general is configured
                if (hasGeneral) {
                    binding.cardGeneralConfig.visibility = View.VISIBLE
                    val general = config!!.general!!

                    // Populate property badges
                    binding.chipGroupGeneralBadges.removeAllViews()

                    // 1. Edge-to-Edge Badge
                    val e2eChip = Chip(this).apply {
                        text = if (general.config.edgeToEdge) {
                            getString(R.string.state_global_e2e_enabled)
                        } else {
                            getString(R.string.state_global_e2e_disabled)
                        }
                        isEnabled = false
                    }
                    binding.chipGroupGeneralBadges.addView(e2eChip)

                    // 2. Window Background Color Badge
                    general.config.windowBackgroundColor?.let { color ->
                        val colorChip = Chip(this).apply {
                            text = String.format("窗口背景: #%06X", 0xFFFFFF and color)
                            isEnabled = false
                        }
                        binding.chipGroupGeneralBadges.addView(colorChip)
                    }

                    // 3. Clear Translucent Badge
                    if (general.config.clearTranslucent) {
                        val clearChip = Chip(this).apply {
                            text = getString(R.string.switch_clear_translucent)
                            isEnabled = false
                        }
                        binding.chipGroupGeneralBadges.addView(clearChip)
                    }

                    // 4. Extra Actions Badge
                    if (general.config.extraActions.isNotEmpty()) {
                        val actionsChip = Chip(this).apply {
                            text = "ExtraActions: ${general.config.extraActions.size}"
                            isEnabled = false
                        }
                        binding.chipGroupGeneralBadges.addView(actionsChip)
                    }

                    // Exclusive Activities Chips (Read-Only)
                    binding.chipGroupExclusive.removeAllViews()
                    val exclusiveList = general.exclusive
                    if (exclusiveList.isEmpty()) {
                        val emptyChip = Chip(this).apply {
                            text = getString(R.string.no_exclusive_activities)
                            isEnabled = false
                        }
                        binding.chipGroupExclusive.addView(emptyChip)
                    } else {
                        exclusiveList.forEach { activityName ->
                            val chip = Chip(this).apply {
                                text = activityName.substringAfterLast(".")
                                isCloseIconVisible = false
                            }
                            binding.chipGroupExclusive.addView(chip)
                        }
                    }
                } else {
                    binding.cardGeneralConfig.visibility = View.GONE
                }

                // Scope Rules Card: only show when scope rules exist
                if (hasScope) {
                    binding.cardScopeConfig.visibility = View.VISIBLE
                    val scopeRules = config!!.scope.map { (activityName, pageConfig) ->
                        ScopeRuleItem(activityName, pageConfig)
                    }
                    scopeRuleAdapter.setList(scopeRules.toMutableList())
                } else {
                    binding.cardScopeConfig.visibility = View.GONE
                }
            }
        }
    }
}
