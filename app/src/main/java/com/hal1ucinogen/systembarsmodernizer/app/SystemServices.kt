package com.hal1ucinogen.systembarsmodernizer.app

import android.app.DownloadManager
import android.app.LocaleManager
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import com.hal1ucinogen.systembarsmodernizer.SBMApp

object SystemServices {
    val packageManager: PackageManager by lazy { SBMApp.app.packageManager }
    val inputMethodManager: InputMethodManager by lazy { SBMApp.app.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    val windowManager: WindowManager by lazy { SBMApp.app.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    val downloadManager: DownloadManager by lazy { SBMApp.app.getSystemService(DownloadManager::class.java) as DownloadManager }

    @delegate:RequiresApi(33)
    val localeManager: LocaleManager by lazy { SBMApp.app.getSystemService(LocaleManager::class.java) }
}
