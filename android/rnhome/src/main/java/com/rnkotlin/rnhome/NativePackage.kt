package com.rnkotlin.rnhome

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import java.util.ArrayList

class NativePackage : ReactPackage {
//可以任意一个return emptyList()  //这样可将ui与module分开不同的package
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(
            SimpleNativeText()  // 注册带属性的 UI 组件
        )
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<com.facebook.react.bridge.NativeModule> {
        val modules: MutableList<NativeModule> = ArrayList()
        modules.add(NativeModule(reactContext))
        return modules
    }
}