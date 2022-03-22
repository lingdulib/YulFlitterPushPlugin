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