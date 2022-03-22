package cn.ling.yu.mus.yulmus.push
import android.os.Handler
import android.os.Looper
import com.alibaba.sdk.android.push.AndroidPopupActivity
import org.json.JSONObject

/**
 * @author Yu L.
 * @date 2022/3/21
 * @email 237881235@qq.com
 */
class ThirdPushPopupActivity :AndroidPopupActivity(){

    private val handler = Handler(Looper.getMainLooper())

    override fun onSysNoticeOpened(title: String, summary: String, extras: MutableMap<String, String>) {
        startActivity(packageManager.getLaunchIntentForPackage(packageName))
        var jsonExtras = JSONObject()
        for (key in extras.keys){
            jsonExtras.putOpt(key, extras[key])
        }
        handler.postDelayed({YulmusPushHandler.methodChannel?.invokeMethod("onNotificationOpened", mapOf(
            "title" to title,
            "summary" to summary,
            "extras" to jsonExtras.toString()
        ))
            finish()
        }, 500)

    }


}