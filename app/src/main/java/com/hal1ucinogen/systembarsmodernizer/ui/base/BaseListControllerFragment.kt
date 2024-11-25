package com.hal1ucinogen.systembarsmodernizer.ui.base

import android.os.Bundle
import android.view.View
import androidx.core.view.MenuProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import rikka.widget.borderview.BorderViewDelegate

abstract class BaseListControllerFragment<T : ViewBinding> : BaseFragment<T>(), MenuProvider,
    IListController {

    protected var borderDelegate: BorderViewDelegate? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                scheduleAppbarLiftingStatus(!(getBorderViewDelegate()?.isShowingTopBorder ?: true))
            }

            override fun onStop(owner: LifecycleOwner) {

            }
        })
    }

    override fun getBorderViewDelegate(): BorderViewDelegate? = borderDelegate

    protected fun scheduleAppbarLiftingStatus(lifted: Boolean) {
        // todo
    }
}