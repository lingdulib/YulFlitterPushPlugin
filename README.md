# yulmus

一个flutter阿里推送插件,仅支持Android和Ios系统.

阿里推送文档地址 [ali-push](https://help.aliyun.com/document_detail/52906.html)

### Flutter 应用

> Flutter 使用

1. 导入推送插件

```yaml
 yulmus:
    git:  https://github.com/lingdulib/YulFlitterPushPlugin.git
```

  ```dart
  import 'package:yulmus/yulmus.dart' as yulalipush;
  ```

2. 在initState方法注册push

```dart
 yulalipush.invokeChannelCallBack();
    if (!Platform.isAndroid) {
      yulalipush.configureNotificationPresentationOption();
    }
    yulalipush.register();
 yulalipush.initCloudChannelResult.listen((event) {
      print("----------->init successful ,deviceId:${yulalipush.deviceId}");
    });
```

3. 如果需要获得通知内容，可以再initState方法注册如下方法

```dart

    yulalipush.onNotification.listen((data) {
      print("----------->接收到通知 ${data.summary}");
    });
    yulalipush.onNotificationOpened.listen((data) {
      print("----------->点击了通知,内容: ${data.summary} ");
    });

    yulalipush.onNotificationRemoved.listen((data) {
      print("----------->通知被移除, $data");
    });

    yulalipush.onNotificationReceivedInApp.listen((data) {
      print("----------->应用内接收到通知, ${data.summary}");
    });

    yulalipush.onNotificationClickedWithNoAction.listen((data) {
      print("----------->,无点击事件通知,${data.summary}");
    });

    yulalipush.onMessageArrived.listen((data) {
      print("------------>,接收消息, ${data.content}");
    });
```

### Android 端配置

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

> Android Gradle文件配置

```groovy
    implementation 'com.aliyun.ams:alicloud-android-third-push-huawei:3.7.4'
    implementation 'com.aliyun.ams:alicloud-android-third-push-xiaomi:3.7.4'
    implementation 'com.aliyun.ams:alicloud-android-third-push-oppo:3.7.4'
    implementation 'com.aliyun.ams:alicloud-android-third-push-vivo:3.7.4'
    implementation 'com.aliyun.ams:alicloud-android-third-push-meizu:3.7.4'
    implementation 'com.aliyun.ams:alicloud-android-third-push-fcm:3.7.4'
```

### IOS 端配置

> Pod集成

1. 在Podfile中添加source，指定Master仓库和阿里云仓库。

```ruby
source 'https://github.com/CocoaPods/Specs.git'
source 'https://github.com/aliyun/aliyun-specs.git'
```

2. 在终端执行 `pod repo add`命令，拉取阿里云Pod仓库到本地 。

```ruby
pod repo add AliyunRepo https://github.com/aliyun/aliyun-specs.git
```

3. 在您的工程中添加以下系统依赖库。

```ruby
pod 'AlicloudPush', '~> 1.9.9'
```

4. 从阿里云控制台下载AliyunEmasServices-Info.plist配置文件，并正确拖入工程

> 推送权限 info.plist

```objectivec

<key>NSAppTransportSecurity</key>
	<dict>
		<key>NSAllowsArbitraryLoads</key>
		<true/>
	</dict>
    <key>NSExtension</key>
	<dict>
		<key>NSExtensionPointIdentifier</key>
		<string>com.apple.usernotifications.service</string>
		<key>NSExtensionPrincipalClass</key>
		<string>NotificationService</string>
	</dict>

```

