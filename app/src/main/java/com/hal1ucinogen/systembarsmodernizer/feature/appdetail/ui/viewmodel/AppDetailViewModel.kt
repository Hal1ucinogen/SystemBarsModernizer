package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hal1ucinogen.systembarsmodernizer.bean.GeneralConfig
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.compat.PackageManagerCompat
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import com.hal1ucinogen.systembarsmodernizer.feature.applist.data.repository.AppListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDetailViewModel(
    private val repository: AppListRepository,
    val packageName: String
) : ViewModel() {

    private val _appItem = MutableLiveData<SBMItem?>()
    val appItem: LiveData<SBMItem?> = _appItem

    private val _declaredActivities = MutableLiveData<List<String>>()
    val declaredActivities: LiveData<List<String>> = _declaredActivities

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val item = repository.getItem(packageName)
            _appItem.value = item

            loadDeclaredActivities()
        }
    }

    private suspend fun loadDeclaredActivities() = withContext(Dispatchers.IO) {
        val activities = runCatching {
            val packageInfo = PackageManagerCompat.getPackageInfo(
                packageName,
                PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
            )
            packageInfo.activities?.map { it.name } ?: emptyList()
        }.getOrDefault(emptyList())

        _declaredActivities.postValue(activities)
    }

    fun isActivityValid(activityName: String): Boolean {
        if (activityName.endsWith("*")) return true
        val declared = _declaredActivities.value ?: return true
        if (declared.isEmpty()) return true
        if (declared.contains(activityName)) return true
        if (activityName.contains("$") && declared.contains(activityName.substringBefore("$"))) return true
        return false
    }

    fun createConfig() {
        val current = _appItem.value ?: return
        val defaultConfig = com.hal1ucinogen.systembarsmodernizer.feature.applist.data.source.DefaultConfigs.configs.firstOrNull { it.packageName == packageName }
        val newConfig = defaultConfig ?: com.hal1ucinogen.systembarsmodernizer.bean.AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap(),
            general = GeneralConfig(config = PageConfig(edgeToEdge = true))
        )
        updateItem(current.copy(features = 1, config = newConfig))
    }

    fun toggleGeneralEdgeToEdge(enabled: Boolean) {
        val current = _appItem.value ?: return
        val currentConfig = current.config ?: return
        val currentGeneral = currentConfig.general
        val updatedGeneral = if (enabled) {
            currentGeneral?.copy(config = currentGeneral.config.copy(edgeToEdge = true))
                ?: GeneralConfig(config = PageConfig(edgeToEdge = true))
        } else {
            if (currentGeneral != null && currentGeneral.exclusive.isNotEmpty()) {
                currentGeneral.copy(config = currentGeneral.config.copy(edgeToEdge = false))
            } else {
                null
            }
        }
        val updatedConfig = currentConfig.copy(general = updatedGeneral)
        updateItem(current.copy(config = updatedConfig))
    }

    fun addExclusiveActivity(activityName: String) {
        val current = _appItem.value ?: return
        val currentConfig = current.config ?: return
        val general = currentConfig.general ?: GeneralConfig(config = PageConfig(edgeToEdge = true))
        if (!general.exclusive.contains(activityName)) {
            val newExclusive = general.exclusive + activityName
            val updatedConfig = currentConfig.copy(general = general.copy(exclusive = newExclusive))
            updateItem(current.copy(config = updatedConfig))
        }
    }

    fun removeExclusiveActivity(activityName: String) {
        val current = _appItem.value ?: return
        val currentConfig = current.config ?: return
        val general = currentConfig.general ?: return
        val newExclusive = general.exclusive.filter { it != activityName }
        val updatedConfig = currentConfig.copy(general = general.copy(exclusive = newExclusive))
        updateItem(current.copy(config = updatedConfig))
    }

    fun addOrUpdateScopeRule(activityName: String, pageConfig: PageConfig) {
        val current = _appItem.value ?: return
        val currentConfig = current.config ?: com.hal1ucinogen.systembarsmodernizer.bean.AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap()
        )
        val newScope = currentConfig.scope.toMutableMap()
        newScope[activityName] = pageConfig
        val updatedConfig = currentConfig.copy(scope = newScope)
        updateItem(current.copy(features = 1, config = updatedConfig))
    }

    fun removeScopeRule(activityName: String) {
        val current = _appItem.value ?: return
        val currentConfig = current.config ?: return
        val newScope = currentConfig.scope.toMutableMap()
        newScope.remove(activityName)
        val updatedConfig = currentConfig.copy(scope = newScope)
        updateItem(current.copy(config = updatedConfig))
    }

    fun resetToDefault() {
        viewModelScope.launch {
            repository.resetToDefaultConfig(packageName)
            loadData()
        }
    }

    fun clearAllConfig() {
        viewModelScope.launch {
            repository.deleteItemConfig(packageName)
            loadData()
        }
    }

    private fun updateItem(newItem: SBMItem) {
        _appItem.value = newItem
        viewModelScope.launch {
            repository.saveItemConfig(newItem)
        }
    }

    class Factory(
        private val repository: AppListRepository,
        private val packageName: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppDetailViewModel(repository, packageName) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
