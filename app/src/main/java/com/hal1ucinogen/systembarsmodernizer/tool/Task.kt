package com.hal1ucinogen.systembarsmodernizer.tool

import android.os.Handler
import android.os.Looper
import android.util.Log
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
                Log.e("Task", "onMain", e)
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
                Log.e("Task", "onBackground", e)
            }
        }
        if (delay > 0) {
            BackgroundThreadPoolHolder.pool.schedule(run, delay, TimeUnit.MILLISECONDS)
        } else {
            BackgroundThreadPoolHolder.pool.submit(run)
        }
    }

    private object MainHandlerHolder {
        val handler = Handler(Looper.getMainLooper())
    }

    private object BackgroundThreadPoolHolder {
        val pool =
            ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 16)
    }
}