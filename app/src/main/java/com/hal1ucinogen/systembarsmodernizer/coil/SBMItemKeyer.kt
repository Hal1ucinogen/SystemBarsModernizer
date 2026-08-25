package com.hal1ucinogen.systembarsmodernizer.coil

import coil.key.Keyer
import coil.request.Options
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem

class SBMItemKeyer : Keyer<SBMItem> {
    override fun key(data: SBMItem, options: Options): String {
        return "${data.packageName}_${data.lastUpdatedTime}"
    }
}
