package com.hal1ucinogen.systembarsmodernizer.feature.applist.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.hal1ucinogen.systembarsmodernizer.databinding.FragmentAppListBinding
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseListControllerFragment

class AppListFragment : BaseListControllerFragment<FragmentAppListBinding>() {
    override fun init() {
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

}