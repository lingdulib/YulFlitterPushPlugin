# yulmus

A new Flutter Alipush project.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

> Android 权限配置

在AndroidManifest.xml添加

```xml
  	<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.GET_TASKS" />
    <uses-permission android:name="android.permission.REORDER_TASKS" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

> Android 辅助通道配置

在AndroidManifest.xml添加

```xml
  <activity android:name="cn.ling.yu.mus.yulmus.push.ThirdPushPopupActivity" android:exported="true"/>
```

> Android 阿里推送渠道和厂商通道配置



```xml
  <meta-data
           android:name="com.alibaba.app.appkey"
           android:value="333401873" /> <!-- 请填写你自己的- appKey -->
       <meta-data
           android:name="com.alibaba.app.appsecret"
           android:value="d85f9fa2498f44f9bcf332beebae5bc8" /> <!-- 请填写你自己的appSecret -->
       <meta-data
           android:name="com.huawei.hms.client.appid"
           android:value="" />
       <meta-data
           android:name="com.xiaomi.push.client.app_id"
           android:value=""/>
       <meta-data
           android:name="com.xiaomi.push.client.app_key"
           android:value="" />
       <meta-data
           android:name="com.oppo.push.client.app_key"
           android:value="" />
       <meta-data
           android:name="com.oppo.push.client.app_secret"
           android:value="" />
       <meta-data
           android:name="com.meizu.push.client.app_id"
           android:value="" />
       <meta-data
           android:name="com.meizu.push.client.app_key"
           android:value="" />
       <meta-data
           android:name="com.vivo.push.app_id"
           android:value="" />
       <meta-data
           android:name="com.vivo.push.api_key"
           android:value="" />
       <meta-data
           android:name="com.gcm.push.send_id"
           android:value="" />
       <meta-data
           android:name="com.gcm.push.app_id"
           android:value="" />
```

> Android 端初始化阿里推送

```kotlin
package cn.ling.yu.mus.yulmus_example

import android.app.Application
import cn.ling.yu.mus.yulmus.YulmusPlugin

/**
 * @author Yu L.
 * @date 2022/3/22
 * @email 237881235@qq.com
 */
class AliApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        YulmusPlugin.initPushService(this)
    }
}
```

