package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.SBMApp
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.databinding.ActivityAppConfigEditBinding
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ScopeRuleAdapter
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.adapter.ScopeRuleItem
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog.ActivityPickerDialog
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.dialog.PageConfigEditDialog
import com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.viewmodel.AppConfigEditViewModel
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.util.PackageUtils
import com.hal1ucinogen.systembarsmodernizer.util.UiUtils
import kotlinx.coroutines.launch

class AppConfigEditActivity : BaseActivity<ActivityAppConfigEditBinding>() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    private lateinit var targetPackageName: String

    private val viewModel: AppConfigEditViewModel by viewModels {
        AppConfigEditViewModel.Factory((application as SBMApp).repository, targetPackageName)
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
        setupBackPressHandling()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            handleBackExit()
        }

        scopeRuleAdapter = ScopeRuleAdapter(isEditable = true) { ruleItem ->
            viewModel.removeScopeRule(ruleItem.activityName)
        }

        scopeRuleAdapter.setOnItemClickListener { _, _, position ->
            val item = scopeRuleAdapter.getItem(position)
            val declared = viewModel.declaredActivities.value.orEmpty()
            PageConfigEditDialog(
                initialActivityName = item.activityName,
                initialConfig = item.config,
                packageName = targetPackageName,
                declaredActivities = declared
            ) { newActivityName, updatedConfig ->
                viewModel.migrateScopeRule(item.activityName, newActivityName, updatedConfig)
            }.show(supportFragmentManager, "PageConfigEditDialog")
        }

        binding.rvScopeRules.layoutManager = LinearLayoutManager(this)
        binding.rvScopeRules.adapter = scopeRuleAdapter

        // General Config Toggle Switch
        binding.switchEnableGeneral.setOnCheckedChangeListener { _, isChecked ->
            val currentHasGeneral = viewModel.draftConfig.value?.general != null
            if (isChecked != currentHasGeneral) {
                viewModel.setGeneralEnabled(isChecked)
            }
        }

        // Edit General Properties Button (opens PageConfigEditDialog)
        binding.btnEditGeneralPageConfig.setOnClickListener {
            val general = viewModel.draftConfig.value?.general
            val pageConfig = general?.config ?: PageConfig(edgeToEdge = true)
            PageConfigEditDialog(getString(R.string.label_general_properties), pageConfig) { _, updatedConfig ->
                viewModel.updateGeneralPageConfig(updatedConfig)
            }.show(supportFragmentManager, "PageConfigEditDialog_General")
        }

        // Add Exclusive Activity Button
        binding.btnAddExclusive.setOnClickListener {
            val declared = viewModel.declaredActivities.value.orEmpty()
            ActivityPickerDialog(targetPackageName, declared) { selectedActivity ->
                viewModel.addExclusiveActivity(selectedActivity)
            }.show(supportFragmentManager, "ActivityPickerDialog_Exclusive")
        }

        // Add Scope Rule Button
        binding.btnAddScopeRule.setOnClickListener {
            val declared = viewModel.declaredActivities.value.orEmpty()
            ActivityPickerDialog(targetPackageName, declared) { selectedActivity ->
                PageConfigEditDialog(
                    initialActivityName = selectedActivity,
                    initialConfig = null,
                    packageName = targetPackageName,
                    declaredActivities = declared
                ) { activityName, newConfig ->
                    viewModel.addOrUpdateScopeRule(activityName, newConfig)
                }.show(supportFragmentManager, "PageConfigEditDialog_New")
            }.show(supportFragmentManager, "ActivityPickerDialog_Scope")
        }
    }

    private fun observeViewModel() {
        viewModel.appItem.observe(this) { item ->
            if (item == null) return@observe
            binding.ivAppIcon.load(item) { crossfade(true) }
            binding.tvAppName.text = item.label
            binding.tvPackageName.text = item.packageName
            val versionStr = PackageUtils.getVersionString(item.versionName, item.versionCode)
            binding.tvVersionInfo.text = "$versionStr • Target API ${item.targetApi}"
        }

        viewModel.draftConfig.observe(this) { config ->
            renderDraftConfig(config)
        }

        viewModel.declaredActivities.observe(this) {
            renderDraftConfig(viewModel.draftConfig.value)
        }
    }

    private fun renderDraftConfig(config: com.hal1ucinogen.systembarsmodernizer.bean.AppConfig?) {
        if (config == null) {
            binding.switchEnableGeneral.isChecked = false
            binding.layoutGeneralEditBody.visibility = View.GONE
            scopeRuleAdapter.setList(mutableListOf())
            binding.tvEmptyScope.visibility = View.VISIBLE
            return
        }

        val general = config.general
        if (general == null) {
            if (binding.switchEnableGeneral.isChecked) {
                binding.switchEnableGeneral.isChecked = false
            }
            binding.layoutGeneralEditBody.visibility = View.GONE
        } else {
            if (!binding.switchEnableGeneral.isChecked) {
                binding.switchEnableGeneral.isChecked = true
            }
            binding.layoutGeneralEditBody.visibility = View.VISIBLE

            // General badges
            binding.chipGroupGeneralBadges.removeAllViews()

            val isE2e = general.config.edgeToEdge
            val e2eText = if (isE2e) {
                getString(R.string.state_global_e2e_enabled)
            } else {
                getString(R.string.state_global_e2e_disabled)
            }
            binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, e2eText, isPrimary = isE2e))

            general.config.windowBackgroundColor?.let { color ->
                val text = String.format("%s: #%06X", getString(R.string.field_window_bg_color), 0xFFFFFF and color)
                binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, text))
            }

            if (general.config.clearTranslucent) {
                binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, getString(R.string.switch_clear_translucent)))
            }

            if (general.config.extraActions.isNotEmpty()) {
                val text = "${getString(R.string.section_extra_actions)}: ${general.config.extraActions.size}"
                binding.chipGroupGeneralBadges.addView(UiUtils.createBadge(this, text))
            }

            // Exclusive Activities Chips
            binding.chipGroupExclusive.removeAllViews()
            val exclusiveList = general.exclusive
            if (exclusiveList.isEmpty()) {
                binding.tvEmptyExclusive.visibility = View.VISIBLE
                binding.chipGroupExclusive.visibility = View.GONE
            } else {
                binding.tvEmptyExclusive.visibility = View.GONE
                binding.chipGroupExclusive.visibility = View.VISIBLE
                val density = resources.displayMetrics.density
                exclusiveList.forEach { activityName ->
                    val isInvalid = !viewModel.isActivityValid(activityName)
                    val simpleName = activityName.substringAfterLast(".")
                    val chip = Chip(this).apply {
                        text = if (isInvalid) "$simpleName (${getString(R.string.badge_activity_missing)})" else simpleName
                        textSize = 11.5f
                        chipMinHeight = 26f * density
                        setEnsureMinTouchTargetSize(false)
                        chipStartPadding = 8f * density
                        chipEndPadding = 4f * density
                        textStartPadding = 0f
                        textEndPadding = 2f * density
                        closeIconSize = 14f * density
                        chipCornerRadius = 6f * density
                        if (isInvalid) {
                            alpha = 0.7f
                        }
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            viewModel.removeExclusiveActivity(activityName)
                        }
                    }
                    binding.chipGroupExclusive.addView(chip)
                }
            }
        }

        // Scope Rules
        val scopeRules = config.scope.map { (activityName, pageConfig) ->
            val isInvalid = !viewModel.isActivityValid(activityName)
            ScopeRuleItem(activityName, pageConfig, isInvalid)
        }
        scopeRuleAdapter.setList(scopeRules.toMutableList())
        binding.tvEmptyScope.visibility = if (scopeRules.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackExit()
            }
        })
    }

    private fun handleBackExit() {
        if (viewModel.isModified) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_discard_title)
                .setMessage(R.string.dialog_discard_message)
                .setPositiveButton(R.string.action_discard) { _, _ ->
                    finish()
                }
                .setNegativeButton(R.string.action_keep_editing, null)
                .show()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_config_edit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                lifecycleScope.launch {
                    val success = viewModel.save()
                    if (success) {
                        Toast.makeText(this@AppConfigEditActivity, R.string.msg_config_saved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                return true
            }
            R.id.action_clean_invalid_rules -> {
                val cleaned = viewModel.cleanInvalidRules()
                if (cleaned > 0) {
                    Toast.makeText(this, getString(R.string.msg_clean_invalid_rules_success, cleaned), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.msg_clean_invalid_rules_empty, Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.action_reset_default -> {
                viewModel.resetToDefault()
                Toast.makeText(this, R.string.msg_config_reset, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_clear_config -> {
                viewModel.clearAll()
                Toast.makeText(this, R.string.msg_config_cleared, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
