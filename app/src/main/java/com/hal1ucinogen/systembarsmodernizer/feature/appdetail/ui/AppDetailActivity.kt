package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.SBMApp
import com.hal1ucinogen.systembarsmodernizer.databinding.ActivityAppDetailBinding
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ScopeRuleAdapter
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ScopeRuleItem
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.viewmodel.AppDetailViewModel
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.util.PackageUtils
import com.hal1ucinogen.systembarsmodernizer.util.UiUtils

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
                    val isE2e = general.config.edgeToEdge
                    val e2eText = if (isE2e) {
                        getString(R.string.state_global_e2e_enabled)
                    } else {
                        getString(R.string.state_global_e2e_disabled)
                    }
                    binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, e2eText, isPrimary = isE2e))

                    // 2. Window Background Color Badge
                    general.config.windowBackgroundColor?.let { color ->
                        val text = String.format("%s: #%06X", getString(R.string.field_window_bg_color), 0xFFFFFF and color)
                        binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, text))
                    }

                    // 3. Clear Translucent Badge
                    if (general.config.clearTranslucent) {
                        binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, getString(R.string.switch_clear_translucent)))
                    }

                    // 4. Extra Actions Badge
                    if (general.config.extraActions.isNotEmpty()) {
                        val text = "${getString(R.string.section_extra_actions)}: ${general.config.extraActions.size}"
                        binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, text))
                    }

                    // Exclusive Activities Chips (Read-Only)
                    renderExclusiveActivities(general.exclusive)
                } else {
                    binding.cardGeneralConfig.visibility = View.GONE
                }

                // Scope Rules Card: only show when scope rules exist
                if (hasScope) {
                    binding.cardScopeConfig.visibility = View.VISIBLE
                    val scopeRules = config!!.scope.map { (activityName, pageConfig) ->
                        val isInvalid = !viewModel.isActivityValid(activityName)
                        ScopeRuleItem(activityName, pageConfig, isInvalid)
                    }
                    scopeRuleAdapter.setList(scopeRules.toMutableList())
                } else {
                    binding.cardScopeConfig.visibility = View.GONE
                }
            }
        }

        viewModel.declaredActivities.observe(this) {
            viewModel.appItem.value?.let { item ->
                val config = item.config ?: return@let
                val hasScope = config.scope.isNotEmpty()
                if (hasScope) {
                    val scopeRules = config.scope.map { (activityName, pageConfig) ->
                        val isInvalid = !viewModel.isActivityValid(activityName)
                        ScopeRuleItem(activityName, pageConfig, isInvalid)
                    }
                    scopeRuleAdapter.setList(scopeRules.toMutableList())
                }
                val general = config.general
                if (general != null) {
                    renderExclusiveActivities(general.exclusive)
                }
            }
        }
    }

    private fun renderExclusiveActivities(exclusiveList: List<String>) {
        binding.chipGroupExclusive.removeAllViews()
        if (exclusiveList.isEmpty()) {
            binding.layoutExclusive.visibility = View.GONE
        } else {
            binding.layoutExclusive.visibility = View.VISIBLE
            exclusiveList.forEach { activityName ->
                val isInvalid = !viewModel.isActivityValid(activityName)
                val simpleName = activityName.substringAfterLast(".")
                val text = if (isInvalid) "$simpleName (${getString(R.string.badge_activity_missing)})" else simpleName
                val badge = UiUtils.createBadge(this, text, isError = isInvalid)
                binding.chipGroupExclusive.addView(badge)
            }
        }
    }
}
