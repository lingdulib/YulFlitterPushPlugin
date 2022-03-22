package cn.ling.yu.mus.yulmus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import cn.ling.yu.mus.yulmus.push.AliPushIntentService
import cn.ling.yu.mus.yulmus.push.YulmusPushHandler
import com.alibaba.sdk.android.push.CloudPushService
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
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private var application: Application? = null

        @JvmStatic
        fun initPushService(application: Application) {
            this.application = application
            configNotificationManager()
            PushServiceFactory.init(application.applicationContext)
            val pushService = PushServiceFactory.getCloudPushService()
            pushService.setLogLevel(CloudPushService.LOG_DEBUG)
            pushService.setPushIntentService(AliPushIntentService::class.java)
        }

       private fun configNotificationManager() {
            application?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val mNotificationManager =
                        it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val id =1.toString()
                    val name = it.applicationInfo.name
                    val description = "收到了一条消息,请点击看一看哟."
                    val importance =NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel(id as String, name as String, importance as Int)
                    channel.description = description as String
                    channel.enableLights(true)
                    channel.lightColor = Color.GREEN
                    // 设置通知出现时的震动（如果 android 设备支持的话）
                    // 设置通知出现时的震动（如果 android 设备支持的话）
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(
                        100,
                        200,
                        300,
                        400,
                        500,
                        400,
                        300,
                        200,
                        400
                    )
                    mNotificationManager.createNotificationChannel(channel)

                }
            }
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

    private fun register() {
        if (application == null) {
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
                }, 500)
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
                }, 500)
            }
        })
        val appInfo = application!!.packageManager
            .getApplicationInfo(application!!.packageName, PackageManager.GET_META_DATA)
        val xiaomiAppId = appInfo.metaData.getString("com.xiaomi.push.client.app_id")
        val xiaomiAppKey = appInfo.metaData.getString("com.xiaomi.push.client.app_key")
        if ((xiaomiAppId != null && xiaomiAppId.isNotBlank())
            && (xiaomiAppKey != null && xiaomiAppKey.isNotBlank())
        ) {
            MiPushRegister.register(
                application!!.applicationContext,
                xiaomiAppId,
                xiaomiAppKey
            )
        }
        val huaweiAppId = appInfo.metaData.getString("com.huawei.hms.client.appid")
        if (huaweiAppId != null && huaweiAppId.toString().isNotBlank()) {
            HuaWeiRegister.register(application!!)
        }
        val oppoAppKey = appInfo.metaData.getString("com.oppo.push.client.app_key")
        val oppoAppSecret = appInfo.metaData.getString("com.oppo.push.client.app_secret")
        if ((oppoAppKey != null && oppoAppKey.isNotBlank())
            && (oppoAppSecret != null && oppoAppSecret.isNotBlank())
        ) {
            OppoRegister.register(application!!.applicationContext, oppoAppKey, oppoAppSecret)
        }
        val meizuAppId = appInfo.metaData.getString("com.meizu.push.client.app_id")
        val meizuAppKey = appInfo.metaData.getString("com.meizu.push.client.app_key")
        if ((meizuAppId != null && meizuAppId.isNotBlank())
            && (meizuAppKey != null && meizuAppKey.isNotBlank())
        ) {
            MeizuRegister.register(application!!.applicationContext, meizuAppId, meizuAppKey)
        }
        val vivoAppId = appInfo.metaData.getString("com.vivo.push.app_id")
        val vivoApiKey = appInfo.metaData.getString("com.vivo.push.api_key")
        if ((vivoAppId != null && vivoAppId.isNotBlank())
            && (vivoApiKey != null && vivoApiKey.isNotBlank())
        ) {
            VivoRegister.register(application!!.applicationContext)
        }
        val gcmSendId = appInfo.metaData.getString("com.gcm.push.send_id")
        val gcmApplicationId = appInfo.metaData.getString("com.gcm.push.app_id")
        if ((gcmSendId != null && gcmSendId.isNotBlank())
            && (gcmApplicationId != null && gcmApplicationId.isNotBlank())
        ) {
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
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun turnOffPushChannel(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.turnOffPushChannel(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun checkPushChannelStatus(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.checkPushChannelStatus(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun bindAccount(call: MethodCall, result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.bindAccount(call.arguments as String?, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }


    private fun unbindAccount(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.unbindAccount(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun bindPhoneNumber(call: MethodCall, result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.bindPhoneNumber(call.arguments as String?, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }


    private fun unbindPhoneNumber(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.unbindPhoneNumber(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
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
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
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
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun listTags(call: MethodCall, result: Result) {
        val target = call.arguments as Int? ?: 1
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.listTags(target, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }


    private fun addAlias(call: MethodCall, result: Result) {
        val alias = call.arguments as String?
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.addAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun removeAlias(call: MethodCall, result: Result) {
        val alias = call.arguments as String?
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.removeAlias(alias, object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

    private fun listAliases(result: Result) {
        val pushService = PushServiceFactory.getCloudPushService()
        pushService.listAliases(object : CommonCallback {
            override fun onSuccess(response: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to true,
                        "response" to response
                    )
                )

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                result.success(
                    mapOf(
                        "isSuccessful" to false,
                        "errorCode" to errorCode,
                        "errorMessage" to errorMessage
                    )
                )
            }
        })
    }

}
