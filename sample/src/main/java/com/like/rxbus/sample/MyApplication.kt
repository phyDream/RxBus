package com.copticomm.jujiahe

import com.like.base.context.BaseApplication
import com.like.logger.Logger
import com.like.retrofit.request.IRetrofitApplication
import com.like.retrofit.request.RetrofitUtils
import com.like.retrofit.request.di.component.RetrofitAppComponent
import com.like.rxbus.RxBus

class MyApplication : BaseApplication(), IRetrofitApplication {

    override fun onCreate() {
        super.onCreate()
        RxBus.register(this)
//        Logger.close()
    }

    override fun onTerminate() {
        super.onTerminate()
        Logger.e("MyApplication终止啦！~~")
        RxBus.unregister(this)
    }

    override val retrofitAppComponent: RetrofitAppComponent by lazy {
        getComponent()
    }

    override fun getApplication() = this

    override fun getScheme() = RetrofitUtils.SCHEME_HTTP // 默认是RetrofitUtils.SCHEME_HTTP

    // "172.16.103.14",   8081 蒋晓龙本地测试服务器
    // "172.16.103.5",    8081 廖宇本地测试服务器
    // "172.16.103.226",  8088 罗庆斌本地测试服务器
    // "172.16.103.227",  8081 内网测试服务器
    // "39.108.151.81",   8080 外网测试服务器
    // "www.jujiahe.com", 443  外网测试服务器
    // "www.gdep.com.cn", 8081 桂东线上服务器
    override fun getIp() = "172.16.103.14"

    override fun getPort() = 8081

    override fun getApiClass() = MyApplication::class.java

}
