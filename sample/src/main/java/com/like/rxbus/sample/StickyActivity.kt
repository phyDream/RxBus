package com.like.rxbus.sample

import android.databinding.DataBindingUtil
import com.like.base.viewmodel.BaseViewModel
import com.like.logger.Logger
import com.like.recyclerview.view.RecyclerViewActivity
import com.like.rxbus.annotations.RxBusSubscribe
import com.like.rxbus.sample.databinding.ActivityStickyBinding

class StickyActivity : RecyclerViewActivity() {
    val mBinding: ActivityStickyBinding by lazy {
        DataBindingUtil.setContentView<ActivityStickyBinding>(this, R.layout.activity_sticky)
    }

    override fun getViewModel(): BaseViewModel {
        return StickyViewModel(host, mBinding)
    }

    @RxBusSubscribe("stick", isSticky = true)
    fun stick(content: Int) {
        Logger.e("StickyViewModel stick content $content")
    }
}
