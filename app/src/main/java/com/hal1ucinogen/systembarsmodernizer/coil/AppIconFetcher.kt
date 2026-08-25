package com.hal1ucinogen.systembarsmodernizer.coil

import android.content.Context
import android.content.pm.PackageInfo
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.hal1ucinogen.systembarsmodernizer.compat.PackageManagerCompat

class AppIconFetcher(
    private val context: Context,
    private val packageInfo: PackageInfo,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val pm = context.packageManager
        val drawable = runCatching {
            packageInfo.applicationInfo?.loadIcon(pm)
                ?: pm.getApplicationIcon(packageInfo.packageName)
        }.getOrElse {
            ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
                ?: pm.defaultActivityIcon
        }

        return DrawableResult(
            drawable = drawable,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory(private val context: Context) : Fetcher.Factory<PackageInfo> {
        override fun create(data: PackageInfo, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppIconFetcher(context, data, options)
        }
    }
}
