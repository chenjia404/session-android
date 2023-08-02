package org.thoughtcrime.securesms.et

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.home.web3.TransactionService
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.toastOnUi

/**
 * Created by Yaakov on
 * Describe:
 */
class ETViewModel(application: Application) : BaseViewModel(application) {

    var cursor: String = ""
    var page: Int = 1

    val etsLiveData = MutableLiveData<List<ET>?>()
    val followLiveData = MutableLiveData<List<User>?>()
    val followStatusLiveData = MutableLiveData<Boolean>()
    val likeLiveData = MutableLiveData<ET>()

    private val apiService by lazy {
        ApiService()
    }

    fun loadET(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadET(cursor)
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

    fun login() {
        execute {
            val signAddress = TransactionService.signEthereumMessage(wallet, wallet.address.toByteArray(Charsets.UTF_8), addPrefix = true)
            val nonce = apiService.loadNonce(wallet.address, signAddress)
            if (nonce != null) {
                val signMessage = TransactionService.signEthereumMessage(wallet, nonce.SignMsg.toByteArray(Charsets.UTF_8), addPrefix = true)
                apiService.authorize(nonce.Nonce, signMessage, wallet.address)
            } else {
                null
            }
        }.onSuccess {
            if (it != null) {
                val userJson = Gson().toJson(it)
                TextSecurePreferences.setXToken(context, it.Token ?: "")
                TextSecurePreferences.setUser(context, userJson)
            }

        }.onError {
            context.toastOnUi(it.message)
        }
    }

    fun loadFollowing(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadFollowing(page)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun loadFollowers(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadFollowers(page)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun follow(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.follow(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followStatusLiveData.postValue(it.Code == 0)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun cancelFollow(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.cancelFollow(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followStatusLiveData.postValue(it.Code == 0)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
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