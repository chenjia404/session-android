package org.thoughtcrime.securesms

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsignal.utilities.Hex
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.util.coroutine.Coroutine
import org.thoughtcrime.securesms.util.toWallet
import org.web3j.crypto.MnemonicUtils
import kotlin.coroutines.CoroutineContext

/**
 * Created by Yaakov on
 * Describe:
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val context: Context by lazy { this.getApplication<ApplicationContext>() }

    val wallet by lazy {
        var seed = IdentityKeyUtil.retrieve(context, IdentityKeyUtil.LOKI_SEED)
        var isPk = TextSecurePreferences.isImportByPk(context)
        val wallet = if (!isPk) {
            val mnemonic = MnemonicUtils.generateMnemonic(Hex.fromStringCondensed(seed))
            mnemonic.toWallet()
        } else {
            seed.toWallet()
        }
        wallet
    }

    fun <T> execute(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ): Coroutine<T> {
        return Coroutine.async(scope, context) { block() }
    }

    fun <R> submit(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> Deferred<R>
    ): Coroutine<R> {
        return Coroutine.async(scope, context) { block().await() }
    }

}