package com.hal1ucinogen.systembarsmodernizer.coil

import android.content.pm.PackageInfo
import coil.key.Keyer
import coil.request.Options

class AppIconKeyer : Keyer<PackageInfo> {
    override fun key(data: PackageInfo, options: Options): String {
        return "${data.packageName}_${data.lastUpdateTime}"
    }
}
