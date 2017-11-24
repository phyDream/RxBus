package com.like.rxbus.sample

import com.like.base.entity.Host
import com.like.logger.Logger
import com.like.recyclerview.presenter.RecyclerViewPresenter
import okhttp3.ResponseBody

class StickyPresenter(host: Host) : RecyclerViewPresenter<ResponseBody, ResponseBody>(host) {

    override fun getMethodName(): String? {
        Logger.w("StickyPresenter getMethodName")
        return "findUserMsg"
    }

    override fun getParams(): MutableMap<String, Any>? = mutableMapOf("unread" to "y")

    override fun onFirstSuccess(t: ResponseBody): ResponseBody? = onSuccess(t)

    fun onSuccess(responseBody: ResponseBody): ResponseBody? = try {
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

}