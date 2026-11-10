package com.shiwa.onearun

import android.app.Application
import com.shiwa.onearun.data.container.AppContainer
import com.shiwa.onearun.data.container.DefaultAppContainer

class OneARunApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
