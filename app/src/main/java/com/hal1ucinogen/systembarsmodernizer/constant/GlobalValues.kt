package com.hal1ucinogen.systembarsmodernizer.constant

import android.content.SharedPreferences
import androidx.core.content.edit
import com.hal1ucinogen.systembarsmodernizer.BuildConfig
import com.hal1ucinogen.systembarsmodernizer.SBMApp
import com.hal1ucinogen.systembarsmodernizer.app.SystemServices
import com.hal1ucinogen.systembarsmodernizer.constant.option.AdvancedOptions
import com.hal1ucinogen.systembarsmodernizer.util.OsUtils
import com.hal1ucinogen.systembarsmodernizer.util.SPDelegates
import com.hal1ucinogen.systembarsmodernizer.util.SPUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber
import java.util.Locale

const val SP_NAME = "${BuildConfig.APPLICATION_ID}_preferences"

object GlobalValues {

    private fun getPreferences(): SharedPreferences {
        return SPUtils.sp
    }

    val preferencesFlow = MutableSharedFlow<Pair<String, Any>>()

    var advancedOptions: Int by SPDelegates(
        Constants.PREF_ADVANCED_OPTIONS,
        AdvancedOptions.DEFAULT_OPTIONS
    )

    var itemAdvancedOptions: Int by SPDelegates(
        Constants.PREF_ITEM_ADVANCED_OPTIONS,
        AdvancedOptions.ITEM_DEFAULT_OPTIONS
    )

    var repo: String by SPDelegates(Constants.PREF_RULES_REPO, Constants.REPO_GITLAB)

    var snapshotTimestamp: Long by SPDelegates(Constants.PREF_SNAPSHOT_TIMESTAMP, 0)

    var distributionUpdateTimestamp: Long by SPDelegates(
        Constants.PREF_DISTRIBUTION_UPDATE_TIMESTAMP,
        0
    )

    var debugMode: Boolean by SPDelegates(Constants.PREF_DEBUG_MODE, false)

    var snapshotKeep: String by SPDelegates(
        Constants.PREF_SNAPSHOT_KEEP,
        Constants.SNAPSHOT_DEFAULT
    )

    var darkMode: String by SPDelegates(Constants.PREF_DARK_MODE, Constants.DARK_MODE_FOLLOW_SYSTEM)

    var rengeTheme: Boolean by SPDelegates(Constants.RENGE_THEME, false)


    var processMode: Boolean by SPDelegates(Constants.PREF_PROCESS_MODE, false)

    var libReferenceThreshold: Int by SPDelegates(Constants.PREF_LIB_REF_THRESHOLD, 2)

    val isShowSystemApps: Boolean
        get() = (advancedOptions and AdvancedOptions.SHOW_SYSTEM_APPS) > 0

    var isColorfulIcon: Boolean by SPDelegates(Constants.PREF_COLORFUL_ICON, true)

    val isAnonymousAnalyticsEnabled: Boolean by SPDelegates(
        Constants.PREF_ANONYMOUS_ANALYTICS,
        true
    )

    var isDetailedAbiChart: Boolean by SPDelegates(Constants.PREF_DETAILED_ABI_CHART, false)

    var preferredRuleLanguage: String by SPDelegates(Constants.PREF_RULE_LANGUAGE, "zh-Hans")


    var localeTag: String by SPDelegates(Constants.PREF_LOCALE, "SYSTEM")

    var locale: Locale = Locale.getDefault()
        get() {
            if (OsUtils.atLeastT()) {
                val systemSelectedLocale =
                    SystemServices.localeManager.getApplicationLocales(SBMApp.app.packageName)
                val appLocale = systemSelectedLocale.get(0)
                if (appLocale != null) {
                    return appLocale
                }
            }
            val tag = localeTag
            if (tag.isEmpty() || "SYSTEM" == tag) {
                return Locale.getDefault()
            }
            return Locale.forLanguageTag(tag)
        }
        set(value) {
            field = value
            localeTag = value.toLanguageTag()
        }

    var uuid: String by SPDelegates(Constants.PREF_UUID, String())

    var isGitHubReachable = true

    var trackItemsChanged = false
}
