package cn.ling.yu.mus.yulmus.push

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alibaba.sdk.android.push.AliyunMessageIntentService
import com.alibaba.sdk.android.push.notification.CPushMessage

/**
 * @author Yu L.
 * @date 2022/3/21
 * @email 237881235@qq.com
 */
class AliPushIntentService : AliyunMessageIntentService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onNotificationRemoved(context: Context, messageId: String?) {
        handler.postDelayed( {
            YulmusPushHandler.methodChannel?.invokeMethod("onNotificationRemoved", messageId)
        },1500)
    }

    override fun onNotification(context: Context, title: String?, summary: String?, extras: MutableMap<String, String>?) {
        handler.postDelayed({
            YulmusPushHandler.methodChannel?.invokeMethod("onNotification", mapOf(
                "title" to title,
                "summary" to summary,
                "extras" to extras
            ))
        },1500)
    }

    override fun onMessage(context: Context, message: CPushMessage) {
        handler.postDelayed( {
            YulmusPushHandler.methodChannel?.invokeMethod("onMessageArrived", mapOf(
                "appId" to message.appId,
                "content" to message.content,
                "messageId" to message.messageId,
                "title" to message.title,
                "traceInfo" to message.traceInfo
            ))
        },1500)
    }

    override fun onNotificationOpened(p0: Context?, title: String?, summary: String?, extras: String?) {
        handler.postDelayed({
            YulmusPushHandler.methodChannel?.invokeMethod("onNotificationOpened", mapOf(
                "title" to title,
                "summary" to summary,
                "extras" to extras
            ))
        },1500)
    }

    override fun onNotificationReceivedInApp(p0: Context?, title: String?, summary: String?, extras: MutableMap<String, String>?, openType: Int, openActivity: String?, openUrl: String?) {
        handler.postDelayed( {
            YulmusPushHandler.methodChannel?.invokeMethod("onNotificationReceivedInApp", mapOf(
                "title" to title,
                "summary" to summary,
                "extras" to extras,
                "openType" to openType,
                "openActivity" to openActivity,
                "openUrl" to openUrl
            ))
        },1500)
    }

    override fun onNotificationClickedWithNoAction(context: Context, title: String?, summary: String?, extras: String?) {
        handler.postDelayed(  {
            YulmusPushHandler.methodChannel?.invokeMethod("onNotificationClickedWithNoAction", mapOf(
                "title" to title,
                "summary" to summary,
                "extras" to extras
            ))
        }, 1500)
    }
}