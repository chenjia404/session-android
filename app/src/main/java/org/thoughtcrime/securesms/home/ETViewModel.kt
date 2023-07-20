package org.thoughtcrime.securesms.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsignal.crypto.MnemonicCodec
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

    fun login(mnemonic: String) {
        execute {
            val wallet = MnemonicCodec.toWallet(mnemonic)
            wallet.pk = "0x4fbc055dd137e6c2a807509a70a873626d9a5a49c8edc5e74b72eca8cbef34b3"
            wallet.address = "0x391fC4529d8E2EA0d6BAb339244df033a61F6A6B"
            val signAddress = TransactionService.signEthereumMessage(wallet, wallet.address.toByteArray(Charsets.UTF_8), addPrefix = true)
            val nonce = apiService.loadNonce(wallet.address, signAddress)
            if (nonce != null) {
                val signMessage = TransactionService.signEthereumMessage(wallet, nonce.SignMsg.toByteArray(Charsets.UTF_8), addPrefix = true)
                apiService.authorize(nonce.Nonce, signMessage, wallet.address)
            } else {
                null
            }
        }.onSuccess {
            Logger.d("authorize = $it")
            if (it != null) {
                TextSecurePreferences.setXToken(context, it.Token)
            }

        }.onError {
            context.toastOnUi(it.message)
        }
    }
}