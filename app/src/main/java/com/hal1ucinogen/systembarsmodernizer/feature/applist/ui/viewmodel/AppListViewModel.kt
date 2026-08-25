package com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.viewmodel

import androidx.lifecycle.*
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import com.hal1ucinogen.systembarsmodernizer.feature.applist.data.repository.AppListRepository
import kotlinx.coroutines.launch

class AppListViewModel(private val repository: AppListRepository) : ViewModel() {

    val allItems: LiveData<List<SBMItem>> = repository.allItems.asLiveData()

    fun refreshAppList() = viewModelScope.launch {
        repository.refreshAppList()
    }

    fun saveConfig(item: SBMItem) = viewModelScope.launch {
        repository.saveItemConfig(item)
    }

    fun deleteConfig(packageName: String) = viewModelScope.launch {
        repository.deleteItemConfig(packageName)
    }

    fun resetToDefaultConfig(packageName: String) = viewModelScope.launch {
        repository.resetToDefaultConfig(packageName)
    }

    fun syncAllToLsposed() = viewModelScope.launch {
        repository.syncAllToLsposed()
    }

    class Factory(private val repository: AppListRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
