package org.thoughtcrime.securesms.et

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.toastOnUi

/**
 * Created by Yaakov on
 * Describe:
 */
class MeViewModel(application: Application) : BaseViewModel(application) {

    var cursor: String = ""

    val etsLiveData = MutableLiveData<List<ET>?>()
    val userInfoLiveData = MutableLiveData<UserInfo?>()

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

    fun loadUserInfo(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.loadUserInfo(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            userInfoLiveData.postValue(it)
            if (it?.user != null) {
                val userJson = Gson().toJson(it.user)
                Logger.d("userJson = $userJson")
                TextSecurePreferences.setUser(context, userJson)
            }

        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }


}