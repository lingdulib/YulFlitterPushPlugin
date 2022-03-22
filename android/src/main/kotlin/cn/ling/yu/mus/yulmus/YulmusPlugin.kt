package cn.ling.yu.mus.yulmus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import cn.ling.yu.mus.yulmus.push.AliPushIntentService
import cn.ling.yu.mus.yulmus.push.YulmusPushHandler
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.register.*

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** YulmusPlugin */
class YulmusPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private val TAG: String = YulmusPlugin::javaClass.name
    private lateinit var channel: MethodChannel
    private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    private val handler=Handler(Looper.getMainLooper())

    companion object{
        private var application:Application?=null
        @JvmStatic
        fun initPushService(application: Application){
            this.application = application
            PushServiceFactory.init(application.applicationContext)
            val pushService = PushServiceFactory.getCloudPushService()
            pushService.setPushIntentService(AliPushIntentService::class.java)
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "yulmus")
        this.flutterPluginBinding = flutterPluginBinding
        YulmusPushHandler.methodChannel = channel
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "register" -> register()
            "deviceId" -> result.success(PushServiceFactory.getCloudPushService().deviceId)
            "turnOnPushChannel" -> turnOnPushChannel(result)
            "turnOffPushChannel" -> turnOffPushChannel(result)
            "checkPushChannelStatus" -> checkPushChannelStatus(result)
            "bindAccount" -> bindAccount(call, result)
            "unbindAccount" -> unbindAccount(result)
            "bindTag" -> bindTag(call, result)
            "unbindTag" -> unbindTag(call, result)
            "listTags" -> listTags(call, result)
            "addAlias" -> addAlias(call, result)
            "removeAlias" -> removeAlias(call, result)
            "listAliases" -> listAliases(result)
            "setupNotificationManager" -> setupNotificationManager(call, result)
            "bindPhoneNumber" -> bindPhoneNumber(call, result)
            "unbindPhoneNumber" -> unbindPhoneNumber(result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
    }

    private fun setupNotificationManager(call: MethodCall, result: Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = call.arguments as List<Map<String, Any?>>
            val mNotificationManager =
                flutterPluginBinding.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannels = mutableListOf<NotificationChannel>()
            for (channel in channels) {
                // 通知渠道的id
                val id = channel["id"] ?: flutterPluginBinding.applicationContext.packageName
                // 用户可以看到的通知渠道的名字.
                val name = channel["name"] ?: flutterPluginBinding.applicationContext.packageName
                // 用户可以看到的通知渠道的描述
                val description =
                    channel["description"] ?: flutterPluginBinding.applicationContext.packageName
                val importance = channel["importance"] ?: NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(id as String, name as String, importance as Int)
                // 配置通知渠道的属性
                mChannel.description = description as String
                mChannel.enableLights(true)
                mChannel.enableVibration(true)
                notificationChannels.add(mChannel)
            }
            if (notificationChannels.isNotEmpty()) {
                mNotificationManager.createNotificationChannels(notificationChannels)
            }
        }
        result.success(true)
    }

    private fun register() {
        if (application == null) {
            Log.e(TAG, "注册推送服务失败，请检查是否在运行本语句前执行了`YulmusPlugin.initPushService`.")
            return;
        }
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.register(application!!.applicationContext, object : CommonCallback {
            override fun onSuccess(response: String?) {
                handler.postDelayed({
                    YulmusPushHandler.methodChannel?.invokeMethod(
                        "initCloudChannelResult", mapOf(
                            "isSuccessful" to true,
                            "response" to response
                        )
                    )
                }, 2000)
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                handler.postDelayed({
                    YulmusPushHandler.methodChannel?.invokeMethod(
                        "initCloudChannelResult", mapOf(
                            "isSuccessful" to false,
                            "errorCode" to errorCode,
                            "errorMessage" to errorMessage
                        )
                    )
                }, 2000)
            }
        })
        val appInfo = application!!.packageManager
            .getApplicationInfo(application!!.packageName, PackageManager.GET_META_DATA)
        val xiaomiAppId = appInfo.metaData.getString("com.xiaomi.push.client.app_id")
        val xiaomiAppKey = appInfo.metaData.getString("com.xiaomi.push.client.app_key")
        if ((xiaomiAppId != null && xiaomiAppId.isNotBlank())
            && (xiaomiAppKey != null && xiaomiAppKey.isNotBlank())
        ) {
            Log.d(TAG, "正在注册小米推送服务...")
            MiPushRegister.register(
                application!!.applicationContext,
                xiaomiAppId,
                xiaomiAppKey
            )
        }
        val huaweiAppId = appInfo.metaData.getString("com.huawei.hms.client.appid")
        if (huaweiAppId != null && huaweiAppId.toString().isNotBlank()) {
            Log.d(TAG, "正在注册华为推送服务...")
            HuaWeiRegister.register(application!!)
        }
        val oppoAppKey = appInfo.metaData.getString("com.oppo.push.client.app_key")
        val oppoAppSecret = appInfo.metaData.getString("com.oppo.push.client.app_secret")
        if ((oppoAppKey != null && oppoAppKey.isNotBlank())
            && (oppoAppSecret != null && oppoAppSecret.isNotBlank())
        ) {
            Log.d(TAG, "正在注册Oppo推送服务...")
            OppoRegister.register(application!!.applicationContext, oppoAppKey, oppoAppSecret)
        }
        val meizuAppId = appInfo.metaData.getString("com.meizu.push.client.app_id")
        val meizuAppKey = appInfo.metaData.getString("com.meizu.push.client.app_key")
        if ((meizuAppId != null && meizuAppId.isNotBlank())
            && (meizuAppKey != null && meizuAppKey.isNotBlank())
        ) {
            Log.d(TAG, "正在注册魅族推送服务...")
            MeizuRegister.register(application!!.applicationContext, meizuAppId, meizuAppKey)
        }
        val vivoAppId = appInfo.metaData.getString("com.vivo.push.app_id")
        val vivoApiKey = appInfo.metaData.getString("com.vivo.push.api_key")
        if ((vivoAppId != null && vivoAppId.isNotBlank())
            && (vivoApiKey != null && vivoApiKey.isNotBlank())
        ) {
            Log.d(TAG, "正在注册Vivo推送服务...")
            VivoRegister.register(application!!.applicationContext)
        }
        val gcmSendId = appInfo.metaData.getString("com.gcm.push.send_id")
        val gcmApplicationId = appInfo.metaData.getString("com.gcm.push.app_id")
        if ((gcmSendId != null && gcmSendId.isNotBlank())
            && (gcmApplicationId != null && gcmApplicationId.isNotBlank())
        ) {
            Log.d(TAG, "正在注册Gcm推送服务...")
            GcmRegister.register(
                application!!.applicationContext,
                gcmSendId,
                gcmApplicationId
            )
        }
    }

    private fun turnOnPushChannel(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.turnOnPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun turnOffPushChannel(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.turnOffPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun checkPushChannelStatus(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.checkPushChannelStatus(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun bindAccount(call: MethodCall, result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.bindAccount(call.arguments as String?, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun unbindAccount(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.unbindAccount(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun bindPhoneNumber(call: MethodCall, result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.bindPhoneNumber(call.arguments as String?, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun unbindPhoneNumber(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.unbindPhoneNumber(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun bindTag(call: MethodCall, result: Result) {
//        target: Int, tags: Array<String>, alias: String, callback: CommonCallback
        val target = call.argument("target") ?: 1
        val tagsInArrayList = call.argument("tags") ?: arrayListOf<String>()
        val alias = call.argument<String?>("alias")

        val arr = arrayOfNulls<String>(tagsInArrayList.size)
        val tags: Array<String> = tagsInArrayList.toArray(arr)

        val pushService = PushServiceFactory.getCloudPushService()

        pushService.bindTag(target, tags, alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun unbindTag(call: MethodCall, result: Result) {
//        target: Int, tags: Array<String>, alias: String, callback: CommonCallback
        val target = call.argument("target") ?: 1
        val tagsInArrayList = call.argument("tags") ?: arrayListOf<String>()
        val alias = call.argument<String?>("alias")

        val arr = arrayOfNulls<String>(tagsInArrayList.size)
        val tags: Array<String> = tagsInArrayList.toArray(arr)

        val pushService = PushServiceFactory.getCloudPushService()

        pushService.unbindTag(target, tags, alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun listTags(call: MethodCall, result: Result) {
        val target = call.arguments as Int? ?: 1
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.listTags(target, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }


    private fun addAlias(call: MethodCall, result: Result) {
        val alias = call.arguments as String?
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.addAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun removeAlias(call: MethodCall, result: Result) {
        val alias = call.arguments as String?
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.removeAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

    private fun listAliases(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.listAliases(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(mapOf(
                    "isSuccessful" to true,
                    "response" to response
                ))

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(mapOf(
                    "isSuccessful" to false,
                    "errorCode" to errorCode,
                    "errorMessage" to errorMessage
                ))
            }
        })
    }

}
