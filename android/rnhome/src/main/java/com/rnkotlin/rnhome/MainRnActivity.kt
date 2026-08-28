package com.rnkotlin.rnhome

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.soloader.SoLoader;
import com.facebook.react.shell.MainReactPackage
import com.facebook.react.soloader.OpenSourceMergedSoMapping

class MainRnActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {
    private var mReactRootView: ReactRootView? = null
    private var mReactInstanceManager: ReactInstanceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoLoader.init(this, OpenSourceMergedSoMapping)

        mReactRootView = ReactRootView(this)
        val packages = listOf<ReactPackage>(
            MainReactPackage(),
            NativePackage(),
            // 在这里添加您的其他 ReactPackage
        )

        mReactInstanceManager = ReactInstanceManager.builder()
            .setApplication(application)
            .setCurrentActivity(this)
            .setBundleAssetName("index.android.bundle")
            .setJSMainModulePath("index")
            .addPackages(packages) //
            //.setUseDeveloperSupport(true)
            .setInitialLifecycleState(LifecycleState.RESUMED)
            .build()
        // 注意这里的MyReactNativeApp 必须对应"index.js"中的
        // "AppRegistry.registerComponent()"的第一个参数
        mReactRootView?.startReactApplication(mReactInstanceManager, "MyReactNativeApp", null)

        setContentView(mReactRootView)
    }

    override fun onPause() {
        super.onPause()
        // 把一些 activity 的生命周期回调传递给ReactInstanceManager
        if (mReactInstanceManager != null) {
            mReactInstanceManager?.onHostPause(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // 把一些 activity 的生命周期回调传递给ReactInstanceManager
        if (mReactInstanceManager != null) {
            mReactInstanceManager?.onHostResume(this, this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 把一些 activity 的生命周期回调传递给ReactInstanceManager
        if (mReactInstanceManager != null) {
            mReactInstanceManager?.onHostDestroy(this)
        }
        if (mReactRootView != null) {
            mReactRootView?.unmountReactApplication()
        }
    }

    override fun onBackPressed() {
        // 后退按钮事件传递给 React Native
        if (mReactInstanceManager != null) {
            mReactInstanceManager?.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            mReactInstanceManager?.showDevOptionsDialog()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 最后，必须重写 onActivityResult() 方法（如下面的代码所示）以处理权限 Accepted 或 Denied 情况以实现一致的 UX。
        // 此外，为了集成使用 startActivityForResult 的 Native Modules，我们需要将结果传递给我们的 ReactInstanceManager
        mReactInstanceManager?.onActivityResult(this, requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }
}
