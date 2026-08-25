package com.hal1ucinogen.systembarsmodernizer.feature.applist.data.repository

import android.content.pm.ApplicationInfo
import com.hal1ucinogen.systembarsmodernizer.app.SystemServices
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.compat.PackageManagerCompat
import com.hal1ucinogen.systembarsmodernizer.database.dao.SBMItemDao
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import com.hal1ucinogen.systembarsmodernizer.feature.applist.data.source.DefaultConfigs
import com.hal1ucinogen.systembarsmodernizer.feature.applist.data.sync.ConfigSyncManager
import com.hal1ucinogen.systembarsmodernizer.util.extension.getVersionCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppListRepository(private val sbmItemDao: SBMItemDao) {

    val allItems: Flow<List<SBMItem>> = sbmItemDao.getAllItems()

    suspend fun getItem(packageName: String): SBMItem? = withContext(Dispatchers.IO) {
        sbmItemDao.getItemByPackageName(packageName)
    }

    suspend fun refreshAppList() = withContext(Dispatchers.IO) {
        val installedPackages = PackageManagerCompat.getInstalledPackages(0)
        val existingItemsMap = sbmItemDao.getAllItemsSync().associateBy { it.packageName }
        val defaultConfigsMap = DefaultConfigs.configs.associateBy { it.packageName }

        val installedPackageNames = HashSet<String>(installedPackages.size)
        val updatedList = mutableListOf<SBMItem>()

        for (packageInfo in installedPackages) {
            val appInfo = packageInfo.applicationInfo ?: continue
            val packageName = packageInfo.packageName
            installedPackageNames.add(packageName)

            val existingItem = existingItemsMap[packageName]
            val defaultConfig = defaultConfigsMap[packageName]
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val item = if (existingItem != null) {
                // Keep user's custom configuration, update app metadata
                existingItem.copy(
                    label = appInfo.loadLabel(SystemServices.packageManager).toString(),
                    versionName = packageInfo.versionName ?: "0",
                    versionCode = packageInfo.getVersionCode(),
                    installedTime = packageInfo.firstInstallTime,
                    lastUpdatedTime = packageInfo.lastUpdateTime,
                    isSystem = isSystem,
                    targetApi = appInfo.targetSdkVersion.toShort()
                )
            } else {
                // New application discovered on device
                SBMItem(
                    packageName = packageName,
                    label = appInfo.loadLabel(SystemServices.packageManager).toString(),
                    versionName = packageInfo.versionName ?: "0",
                    versionCode = packageInfo.getVersionCode(),
                    installedTime = packageInfo.firstInstallTime,
                    lastUpdatedTime = packageInfo.lastUpdateTime,
                    isSystem = isSystem,
                    features = if (defaultConfig != null) 1 else 0,
                    targetApi = appInfo.targetSdkVersion.toShort(),
                    config = defaultConfig
                )
            }

            if (item.features > 0 || !item.isSystem) {
                updatedList.add(item)
            }
        }

        // Retain uninstalled apps with custom configs
        for ((packageName, existingItem) in existingItemsMap) {
            if (!installedPackageNames.contains(packageName) && existingItem.features > 0) {
                updatedList.add(existingItem)
            }
        }

        // Batch save to Room database without clearing existing configurations
        sbmItemDao.insertItems(updatedList)

        // Clean up uninstalled apps that have no configuration
        val uninstalledWithoutConfig = existingItemsMap.filter { (pkg, item) ->
            !installedPackageNames.contains(pkg) && item.features <= 0
        }.keys.toList()
        if (uninstalledWithoutConfig.isNotEmpty()) {
            sbmItemDao.deleteByPackageNames(uninstalledWithoutConfig)
        }
    }

    suspend fun saveItemConfig(item: SBMItem) = withContext(Dispatchers.IO) {
        val hasConfig = item.config != null
        val updatedItem = item.copy(features = if (hasConfig) 1 else 0)
        sbmItemDao.insertItem(updatedItem)
        if (updatedItem.config != null) {
            ConfigSyncManager.pushConfig(updatedItem.config)
        } else {
            ConfigSyncManager.removeConfig(updatedItem.packageName)
        }
    }

    suspend fun deleteItemConfig(packageName: String) = withContext(Dispatchers.IO) {
        val existing = sbmItemDao.getItemByPackageName(packageName)
        if (existing != null) {
            val resetItem = existing.copy(
                features = 0,
                config = null
            )
            sbmItemDao.insertItem(resetItem)
        }
        ConfigSyncManager.removeConfig(packageName)
    }

    suspend fun resetToDefaultConfig(packageName: String) = withContext(Dispatchers.IO) {
        val defaultConfig = DefaultConfigs.configs.firstOrNull { it.packageName == packageName }
        val existing = sbmItemDao.getItemByPackageName(packageName)
        if (existing != null) {
            val resetItem = existing.copy(
                features = if (defaultConfig != null) 1 else 0,
                config = defaultConfig
            )
            sbmItemDao.insertItem(resetItem)
            if (defaultConfig != null) {
                ConfigSyncManager.pushConfig(defaultConfig)
            } else {
                ConfigSyncManager.removeConfig(packageName)
            }
        }
    }

    suspend fun syncAllToLsposed(): Boolean = withContext(Dispatchers.IO) {
        val configuredItems = sbmItemDao.getConfiguredItemsSync()
        val configs = configuredItems.mapNotNull { it.config }
        ConfigSyncManager.pushAllConfigs(configs)
    }

    suspend fun clearOrphanConfigs() = withContext(Dispatchers.IO) {
        val installedPackages = PackageManagerCompat.getInstalledPackages(0).map { it.packageName }.toSet()
        val existingItems = sbmItemDao.getAllItemsSync()
        val orphans = existingItems.filter { !installedPackages.contains(it.packageName) }
        orphans.forEach {
            ConfigSyncManager.removeConfig(it.packageName)
        }
        sbmItemDao.deleteByPackageNames(orphans.map { it.packageName })
    }

    suspend fun insert(item: SBMItem) = withContext(Dispatchers.IO) {
        sbmItemDao.insertItem(item)
    }

    suspend fun delete(item: SBMItem) = withContext(Dispatchers.IO) {
        sbmItemDao.deleteItem(item)
    }
}

