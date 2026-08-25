package com.hal1ucinogen.systembarsmodernizer.coil

import android.content.Context
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.hal1ucinogen.systembarsmodernizer.compat.PackageManagerCompat
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem

class SBMItemFetcher(
    private val context: Context,
    private val item: SBMItem,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val pm = context.packageManager
        val drawable = runCatching {
            PackageManagerCompat.getApplicationInfo(item.packageName, 0).loadIcon(pm)
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

    class Factory(private val context: Context) : Fetcher.Factory<SBMItem> {
        override fun create(data: SBMItem, options: Options, imageLoader: ImageLoader): Fetcher {
            return SBMItemFetcher(context, data, options)
        }
    }
}
