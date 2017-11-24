package com.like.rxbus.sample

import com.like.base.entity.Host
import com.like.logger.Logger
import com.like.recyclerview.viewmodel.RecyclerViewModel
import com.like.rxbus.annotations.RxBusSubscribe
import com.like.rxbus.sample.databinding.ActivityStickyBinding
import okhttp3.ResponseBody

class StickyViewModel(host: Host, val binding: ActivityStickyBinding)
    : RecyclerViewModel<ResponseBody>(StickyPresenter(host), binding.recyclerviewBinding) {

    @RxBusSubscribe("stick", isSticky = true)
    fun stick(content: Int) {
        Logger.e("StickyViewModel stick content $content")
    }
}
