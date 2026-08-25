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
            PageConfigEditDialog(item.activityName, item.config) { activityName, updatedConfig ->
                viewModel.addOrUpdateScopeRule(activityName, updatedConfig)
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
            PageConfigEditDialog("通用属性 (General)", pageConfig) { _, updatedConfig ->
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
                PageConfigEditDialog(selectedActivity, null) { activityName, newConfig ->
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
        }

        viewModel.draftConfig.observe(this) { config ->
            if (config == null) {
                binding.switchEnableGeneral.isChecked = false
                binding.layoutGeneralEditBody.visibility = View.GONE
                scopeRuleAdapter.setList(mutableListOf())
                binding.tvEmptyScope.visibility = View.VISIBLE
                return@observe
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

                val e2eChip = Chip(this).apply {
                    text = if (general.config.edgeToEdge) {
                        getString(R.string.state_global_e2e_enabled)
                    } else {
                        getString(R.string.state_global_e2e_disabled)
                    }
                    isEnabled = false
                }
                binding.chipGroupGeneralBadges.addView(e2eChip)

                general.config.windowBackgroundColor?.let { color ->
                    val colorChip = Chip(this).apply {
                        text = String.format("窗口背景: #%06X", 0xFFFFFF and color)
                        isEnabled = false
                    }
                    binding.chipGroupGeneralBadges.addView(colorChip)
                }

                if (general.config.clearTranslucent) {
                    val clearChip = Chip(this).apply {
                        text = getString(R.string.switch_clear_translucent)
                        isEnabled = false
                    }
                    binding.chipGroupGeneralBadges.addView(clearChip)
                }

                if (general.config.extraActions.isNotEmpty()) {
                    val actionsChip = Chip(this).apply {
                        text = "动作: ${general.config.extraActions.size}"
                        isEnabled = false
                    }
                    binding.chipGroupGeneralBadges.addView(actionsChip)
                }

                // Exclusive Activities Chips
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
                ScopeRuleItem(activityName, pageConfig)
            }
            scopeRuleAdapter.setList(scopeRules.toMutableList())
            binding.tvEmptyScope.visibility = if (scopeRules.isEmpty()) View.VISIBLE else View.GONE
        }
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
        menu?.add(0, 1, 0, R.string.action_save)?.setIcon(android.R.drawable.ic_menu_save)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu?.add(0, 2, 1, R.string.action_reset_default)?.setIcon(android.R.drawable.ic_menu_revert)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu?.add(0, 3, 2, R.string.action_clear_config)?.setIcon(android.R.drawable.ic_menu_delete)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                lifecycleScope.launch {
                    val success = viewModel.save()
                    if (success) {
                        Toast.makeText(this@AppConfigEditActivity, R.string.msg_config_saved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                return true
            }
            2 -> {
                viewModel.resetToDefault()
                Toast.makeText(this, R.string.msg_config_reset, Toast.LENGTH_SHORT).show()
                return true
            }
            3 -> {
                viewModel.clearAll()
                Toast.makeText(this, R.string.msg_config_cleared, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
