package com.hal1cinogen.systembarsmodernizer.tool

import android.os.Handler
import android.os.Looper
import com.hal1cinogen.systembarsmodernizer.tool.ApplicationUtils.application
import de.robv.android.xposed.XposedBridge
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Task {
    fun onMain(runnable: Runnable) {
        onMain(0, runnable)
    }

    fun onMain(delay: Long, runnable: Runnable) {
        val run = Runnable {
            try {
                runnable.run()
            } catch (e: Exception) {
                XposedBridge.log(e)
            }
        }
        if (delay > 0) {
            MainHandlerHolder.handler.postDelayed(run, delay)
        } else {
            MainHandlerHolder.handler.post(run)
        }
    }

    fun onBackground(runnable: Runnable) {
        onBackground(0, runnable)
    }

    fun onBackground(delay: Long, runnable: Runnable) {
        val run = Runnable {
            try {
                runnable.run()
            } catch (e: Exception) {
                XposedBridge.log(e)
            }
        }
        if (delay > 0) {
            BackgroundThreadPoolHolder.pool.schedule(run, delay, TimeUnit.MILLISECONDS)
        } else {
            BackgroundThreadPoolHolder.pool.submit(run)
        }
    }

    fun onApplicationReady(runnable: Runnable?) {
        val pool = Executors.newScheduledThreadPool(1)
        val task: Runnable = object : Runnable {
            override fun run() {
                val application = application
                if (application == null) {
                    pool.schedule(this, 200, TimeUnit.MILLISECONDS)
                    return
                }
                val looper = Looper.getMainLooper()
                if (looper == null) {
                    pool.schedule(this, 200, TimeUnit.MILLISECONDS)
                    return
                }
                val handler = Handler(looper)
                handler.post(runnable!!)
            }
        }
        pool.submit(task)
    }

    private object MainHandlerHolder {
        val handler = Handler(Looper.getMainLooper())
    }

    private object BackgroundThreadPoolHolder {
        val pool =
            ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 16)
    }
}