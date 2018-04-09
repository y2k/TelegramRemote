package com.telegramremote

import android.app.Application
import com.facebook.react.ReactActivity

import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.shell.MainReactPackage
import com.facebook.react.uimanager.ViewManager
import com.facebook.soloader.SoLoader

import java.util.Collections.emptyList

class MainActivity : ReactActivity() {

    override fun getMainComponentName() = "TelegramRemote"

    override fun onStart() {
        super.onStart()
        TelegramLister.start(applicationContext)
    }

    override fun onStop() {
        super.onStop()
        TelegramLister.stop()
    }
}

class MainApplication : Application(), ReactApplication {

    private val mReactNativeHost = object : ReactNativeHost(this) {

        override fun getPackages(): List<ReactPackage> =
            listOf(
                MainReactPackage(),
                object : ReactPackage {

                    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
                        emptyList<ViewManager<*, *>>()

                    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
                        listOf(NativeModule(reactContext))
                })

        override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
    }

    override fun getReactNativeHost(): ReactNativeHost = mReactNativeHost

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
    }
}