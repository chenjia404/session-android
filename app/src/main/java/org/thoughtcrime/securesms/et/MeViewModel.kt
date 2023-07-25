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
class MeViewModel(application: Application) : BaseViewModel(application) {

    var cursor: String = ""

    val etsLiveData = MutableLiveData<List<ET>?>()

    private val apiService by lazy {
        ApiService()
    }

    fun loadETFollow(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadETFollow(cursor)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            etsLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }


}