package ru.snowmaze.barstats

import android.app.Application
import android.content.Context
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BarStatsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BarStatsApplication)
            modules(sharedModule)
            modules(module {
                single<DataPath> {
                    DataPath(get<Context>().cacheDir.toOkioPath())
                }
                mokoViewModel {
                    MainViewModel(get())
                }
            })
        }
    }
}