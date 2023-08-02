package org.thoughtcrime.securesms.et

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.toastOnUi

/**
 * Created by Yaakov on
 * Describe:
 */
class ETDetailViewModel(application: Application) : BaseViewModel(application) {

    var page: Int = 1

    val commentsLiveData = MutableLiveData<List<Comment>?>()
    val likeLiveData = MutableLiveData<ET>()

    private val apiService by lazy {
        ApiService()
    }

    fun loadComments(address: String) {
        execute {
            apiService.loadComments(address, page)
        }.onSuccess {
            commentsLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }
    }

    fun releaseComment(et_address: String, content: String) {
        execute {
            apiService.releaseComment(et_address, content)
        }.onSuccess {
            context.toastOnUi(it.Msg)
        }.onError {
            context.toastOnUi(it.message)
        }
    }

    fun like(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        et: ET
    ) {
        execute {
            apiService.like(et.TwAddress ?: "")
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            if (it.Code == 0) {
                et.isTwLike = !et.isTwLike
                if (et.isTwLike) {
                    et.LikeCount = et.LikeCount?.plus(1)
                } else {
                    et.LikeCount = et.LikeCount?.minus(1)
                }
            }
            likeLiveData.postValue(et)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }
}