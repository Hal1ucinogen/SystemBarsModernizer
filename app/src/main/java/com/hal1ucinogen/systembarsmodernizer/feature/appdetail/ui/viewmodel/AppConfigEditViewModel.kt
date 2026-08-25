package com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.bean.GeneralConfig
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import com.hal1ucinogen.systembarsmodernizer.compat.PackageManagerCompat
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import com.hal1ucinogen.systembarsmodernizer.feature.applist.data.repository.AppListRepository
import com.hal1ucinogen.systembarsmodernizer.feature.applist.data.source.DefaultConfigs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppConfigEditViewModel(
    private val repository: AppListRepository,
    val packageName: String
) : ViewModel() {

    private val _appItem = MutableLiveData<SBMItem?>()
    val appItem: LiveData<SBMItem?> = _appItem

    private val _draftConfig = MutableLiveData<AppConfig?>()
    val draftConfig: LiveData<AppConfig?> = _draftConfig

    private val _declaredActivities = MutableLiveData<List<String>>()
    val declaredActivities: LiveData<List<String>> = _declaredActivities

    var isModified: Boolean = false
        private set

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val item = repository.getItem(packageName)
            _appItem.value = item

            val initialConfig = item?.config ?: run {
                val defaultConfig = DefaultConfigs.configs.firstOrNull { it.packageName == packageName }
                defaultConfig ?: AppConfig(
                    packageName = packageName,
                    configVersion = 1,
                    scope = emptyMap(),
                    general = GeneralConfig(config = PageConfig(edgeToEdge = true))
                )
            }
            _draftConfig.value = initialConfig

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

    fun setGeneralEnabled(enabled: Boolean) {
        val currentConfig = _draftConfig.value ?: AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap()
        )
        val updatedGeneral = if (enabled) {
            currentConfig.general ?: GeneralConfig(config = PageConfig(edgeToEdge = true))
        } else {
            null
        }
        _draftConfig.value = currentConfig.copy(general = updatedGeneral)
        isModified = true
    }

    fun updateGeneralPageConfig(pageConfig: PageConfig) {
        val currentConfig = _draftConfig.value ?: AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap()
        )
        val currentGeneral = currentConfig.general ?: GeneralConfig(config = pageConfig)
        _draftConfig.value = currentConfig.copy(general = currentGeneral.copy(config = pageConfig))
        isModified = true
    }

    fun addExclusiveActivity(activityName: String) {
        val currentConfig = _draftConfig.value ?: AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap()
        )
        val general = currentConfig.general ?: GeneralConfig(config = PageConfig(edgeToEdge = true))
        if (!general.exclusive.contains(activityName)) {
            val newExclusive = general.exclusive + activityName
            _draftConfig.value = currentConfig.copy(general = general.copy(exclusive = newExclusive))
            isModified = true
        }
    }

    fun removeExclusiveActivity(activityName: String) {
        val currentConfig = _draftConfig.value ?: return
        val general = currentConfig.general ?: return
        val newExclusive = general.exclusive.filter { it != activityName }
        _draftConfig.value = currentConfig.copy(general = general.copy(exclusive = newExclusive))
        isModified = true
    }

    fun addOrUpdateScopeRule(activityName: String, pageConfig: PageConfig) {
        val currentConfig = _draftConfig.value ?: AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap()
        )
        val newScope = currentConfig.scope.toMutableMap()
        newScope[activityName] = pageConfig
        _draftConfig.value = currentConfig.copy(scope = newScope)
        isModified = true
    }

    fun removeScopeRule(activityName: String) {
        val currentConfig = _draftConfig.value ?: return
        val newScope = currentConfig.scope.toMutableMap()
        newScope.remove(activityName)
        _draftConfig.value = currentConfig.copy(scope = newScope)
        isModified = true
    }

    fun resetToDefault() {
        val defaultConfig = DefaultConfigs.configs.firstOrNull { it.packageName == packageName }
        _draftConfig.value = defaultConfig ?: AppConfig(
            packageName = packageName,
            configVersion = 1,
            scope = emptyMap(),
            general = GeneralConfig(config = PageConfig(edgeToEdge = true))
        )
        isModified = true
    }

    fun clearAll() {
        _draftConfig.value = null
        isModified = true
    }

    suspend fun save(): Boolean = withContext(Dispatchers.IO) {
        val item = _appItem.value ?: return@withContext false
        val finalConfig = _draftConfig.value
        val updatedItem = item.copy(
            features = if (finalConfig != null) 1 else 0,
            config = finalConfig
        )
        repository.saveItemConfig(updatedItem)
        isModified = false
        true
    }

    class Factory(
        private val repository: AppListRepository,
        private val packageName: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppConfigEditViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppConfigEditViewModel(repository, packageName) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
