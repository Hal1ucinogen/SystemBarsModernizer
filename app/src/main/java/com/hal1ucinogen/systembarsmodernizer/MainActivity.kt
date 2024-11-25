package com.hal1ucinogen.systembarsmodernizer

import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hal1ucinogen.systembarsmodernizer.databinding.ActivityMainBinding
import com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.AppListFragment
import com.hal1ucinogen.systembarsmodernizer.feature.overview.ui.OverviewFragment
import com.hal1ucinogen.systembarsmodernizer.feature.settings.ui.SettingsFragment
import com.hal1ucinogen.systembarsmodernizer.ui.base.BaseActivity
import com.hal1ucinogen.systembarsmodernizer.util.addBackStateHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val navViewBehavior by lazy { HideBottomViewOnScrollBehavior<BottomNavigationView>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        val navView = binding.navView
        setSupportActionBar(binding.toolbar)
        binding.container.bringChildToFront(binding.appbar)
        binding.viewpager.run {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> OverviewFragment()
                        1 -> AppListFragment()
                        else -> SettingsFragment()
                    }
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    navView.menu.getItem(position).isChecked = true
                }
            })
            isUserInputEnabled = false
            offscreenPageLimit = 2
            fixViewPager2Insets(this)

            navView.run {
                if (this is BottomNavigationView) {
                    (layoutParams as CoordinatorLayout.LayoutParams).behavior = navViewBehavior
                }
                requestLayout()
                setOnItemSelectedListener {
                    fun performClickNavItem(index: Int) {
                        if (binding.viewpager.currentItem != index) {
                            if (!binding.viewpager.isFakeDragging) {
                                binding.viewpager.setCurrentItem(index, true)
                            }
                        } else {
                            val clickFlag =
                                binding.viewpager.getTag(R.id.viewpager_tab_click) as? Boolean
                                    ?: false
                            if (!clickFlag) {
                                binding.viewpager.setTag(R.id.viewpager_tab_click, true)
                                lifecycleScope.launch {
                                    delay(200L)
                                    binding.viewpager.setTag(R.id.viewpager_tab_click, false)
                                }
                            } else {
                                // todo refresh child
                            }
                        }
                    }
                    when (it.itemId) {
                        R.id.navigation_overview -> performClickNavItem(0)
                        R.id.navigation_app_list -> performClickNavItem(1)
                        R.id.navigation_settings -> performClickNavItem(2)
                    }
                    true
                }
                setOnClickListener { }
                if (this is BottomNavigationView) {
                    fixBottomNavInsets(this)
                }
            }
        }

        onBackPressedDispatcher.addBackStateHandler(
            lifecycleOwner = this,
            enableState = { binding.toolbar.hasExpandedActionView() },
            handler = { binding.toolbar.collapseActionView() }
        )
    }

    private fun fixViewPager2Insets(view: ViewPager2) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            /* Do nothing */
            windowInsets
        }
    }

    private fun fixBottomNavInsets(view: BottomNavigationView) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val navBarInsets = ViewCompat.getRootWindowInsets(view)!!
                .getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = navBarInsets.bottom)
            windowInsets
        }
    }
}