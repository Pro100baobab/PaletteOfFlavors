package com.paletteofflavors

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class PaletteOfFlavorsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_KIT_KEY)
    }
}
// TODO: перенести создание репозиториев и провайдеров из MainActivity