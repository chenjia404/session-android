package org.thoughtcrime.securesms.home

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
}