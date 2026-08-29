package com.hal1ucinogen.systembarsmodernizer.feature.overview.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hal1ucinogen.systembarsmodernizer.database.dao.SBMItemDao
import kotlinx.coroutines.flow.map

data class OverviewStats(
    val configuredAppsCount: Int = 0,
    val totalScopeConfigsCount: Int = 0,
    val globalE2eAppsCount: Int = 0,
    val totalExtraActionsCount: Int = 0
)

class OverviewViewModel(
    sbmItemDao: SBMItemDao
) : ViewModel() {

    val stats = sbmItemDao.getAllItems().map { items ->
        var configuredApps = 0
        var totalScopeConfigs = 0
        var globalE2eApps = 0
        var totalExtraActions = 0

        for (item in items) {
            val config = item.config ?: continue
            val hasGeneral = config.general != null
            val hasScope = config.scope.isNotEmpty()

            if (hasGeneral || hasScope) {
                configuredApps++
            }

            if (config.general?.config?.edgeToEdge == true) {
                globalE2eApps++
            }

            totalScopeConfigs += config.scope.size

            // Count general extra actions
            config.general?.config?.extraActions?.let {
                totalExtraActions += it.size
            }

            // Count scope extra actions
            for ((_, pageConfig) in config.scope) {
                totalExtraActions += pageConfig.extraActions.size
            }
        }

        OverviewStats(
            configuredAppsCount = configuredApps,
            totalScopeConfigsCount = totalScopeConfigs,
            globalE2eAppsCount = globalE2eApps,
            totalExtraActionsCount = totalExtraActions
        )
    }.asLiveData()
}
