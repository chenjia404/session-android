package org.thoughtcrime.securesms.et

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.home.web3.TransactionService
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.Logger
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
                Logger.d("userJson = $userJson")
                TextSecurePreferences.setXToken(context, it.Token)
                TextSecurePreferences.setUser(context, userJson)
            }

        }.onError {
            context.toastOnUi(it.message)
        }
    }
}