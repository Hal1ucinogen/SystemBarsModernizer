package com.hal1ucinogen.systembarsmodernizer.feature.applist.ui

import android.content.res.Configuration
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.SBMApp
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import com.hal1ucinogen.systembarsmodernizer.databinding.FragmentAppListBinding
import com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.adapter.AppAdapter
import com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.adapter.AppListDiffUtil
import com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.view.EmptyListView
import com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.viewmodel.AppListViewModel
import com.hal1ucinogen.systembarsmodernizer.ui.adapter.HorizontalSpacesItemDecoration
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseListControllerFragment
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import rikka.widget.borderview.BorderView

class AppListFragment : BaseListControllerFragment<FragmentAppListBinding>(), SearchView.OnQueryTextListener {

    private val viewModel: AppListViewModel by viewModels {
        AppListViewModel.Factory((requireActivity().application as SBMApp).repository)
    }

    private val appAdapter = AppAdapter()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private var fullList: List<SBMItem> = emptyList()
    private var currentFilterMode = FilterMode.ALL
    private var currentQuery: String = ""

    enum class FilterMode {
        ALL, CONFIGURED_ONLY, USER_ONLY, SYSTEM_ONLY
    }

    override fun init() {
        val context = (context as? BaseActivity<*>) ?: return
        with(appAdapter) {
            setOnItemClickListener { _, _, position ->
                val item = appAdapter.getItem(position)
                val intent = android.content.Intent(requireContext(), com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.AppDetailActivity::class.java).apply {
                    putExtra(com.hal1ucinogen.systembarsmodernizer.feature.appdetail.ui.AppDetailActivity.EXTRA_PACKAGE_NAME, item.packageName)
                }
                startActivity(intent)
            }
            setDiffCallback(AppListDiffUtil())
            setHasStableIds(true)
            setEmptyView(
                EmptyListView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                    )
                })
        }
        with(binding.list) {
            adapter = appAdapter
            borderDelegate = borderViewDelegate
            layoutManager = getSuitableLayoutManagerImpl(resources.configuration)
            borderVisibilityChangedListener =
                BorderView.OnBorderVisibilityChangedListener { top, _, _, _ ->
                    if (isResumed) {
                        scheduleAppbarLiftingStatus(!top)
                    }
                }
            setHasFixedSize(true)
            FastScrollerBuilder(this).useDefaultStyle().build()
        }

        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            fullList = items
            filterAndSubmitList()
        }

        if (appAdapter.data.isEmpty()) {
            viewModel.refreshAppList()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_list_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView
        searchView?.apply {
            queryHint = getString(R.string.action_search)
            setOnQueryTextListener(this@AppListFragment)
            if (currentQuery.isNotEmpty()) {
                setQuery(currentQuery, false)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_refresh -> {
                viewModel.refreshAppList()
                return true
            }
            R.id.filter_all -> {
                menuItem.isChecked = true
                currentFilterMode = FilterMode.ALL
                filterAndSubmitList()
                return true
            }
            R.id.filter_configured_only -> {
                menuItem.isChecked = true
                currentFilterMode = FilterMode.CONFIGURED_ONLY
                filterAndSubmitList()
                return true
            }
            R.id.filter_user_only -> {
                menuItem.isChecked = true
                currentFilterMode = FilterMode.USER_ONLY
                filterAndSubmitList()
                return true
            }
            R.id.filter_system_only -> {
                menuItem.isChecked = true
                currentFilterMode = FilterMode.SYSTEM_ONLY
                filterAndSubmitList()
                return true
            }
        }
        return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        currentQuery = newText.orEmpty()
        filterAndSubmitList()
        return true
    }

    private fun filterAndSubmitList() {
        appAdapter.highlightText = currentQuery.trim()
        val filtered = fullList.filter { item ->
            val matchesFilter = when (currentFilterMode) {
                FilterMode.ALL -> true
                FilterMode.CONFIGURED_ONLY -> item.hasConfig()
                FilterMode.USER_ONLY -> !item.isSystem
                FilterMode.SYSTEM_ONLY -> item.isSystem
            }
            val matchesQuery = if (currentQuery.isBlank()) {
                true
            } else {
                item.label.contains(currentQuery, ignoreCase = true) ||
                        item.packageName.contains(currentQuery, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
        appAdapter.setDiffNewData(filtered.toMutableList())
    }

    private fun getSuitableLayoutManagerImpl(configuration: Configuration): RecyclerView.LayoutManager {
        layoutManager = when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> LinearLayoutManager(requireContext())
            Configuration.ORIENTATION_LANDSCAPE ->
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            else -> throw IllegalStateException("Wrong orientation at AppListFragment")
        }
        return layoutManager
    }

}