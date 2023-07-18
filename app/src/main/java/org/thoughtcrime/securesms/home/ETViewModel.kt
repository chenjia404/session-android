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
class ETViewModel(application: Application) : BaseViewModel(application) {

    var cursor: String = ""

    val etsLiveData = MutableLiveData<List<ET>?>()

    private val apiService by lazy {
        ApiService()
    }

    fun loadET() {
        execute {
            apiService.loadET(cursor)
        }.onSuccess {
            cursor = it?.last()?.Cursor ?: ""
            etsLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }
    }
}