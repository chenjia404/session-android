package org.thoughtcrime.securesms

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.util.coroutine.Coroutine
import kotlin.coroutines.CoroutineContext

/**
 * Created by Yaakov on
 * Describe:
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val context: Context by lazy { this.getApplication<ApplicationContext>() }

    val wallet by lazy {
//        var hexEncodedSeed = IdentityKeyUtil.retrieve(context, IdentityKeyUtil.LOKI_SEED)
//        if (hexEncodedSeed == null) {
//            hexEncodedSeed = IdentityKeyUtil.getIdentityKeyPair(context).hexEncodedPrivateKey // Legacy account
//        }
//        val loadFileContents: (String) -> String = { fileName ->
//            MnemonicUtilities.loadFileContents(context, fileName)
//        }
//        if (hexEncodedSeed.length == 64 && TextSecurePreferences.isImportByPk(context)) {
//            hexEncodedSeed
//        } else {
//            MnemonicCodec(loadFileContents).encode(hexEncodedSeed!!, MnemonicCodec.Language.Configuration.english)
//        }
//        hexEncodedSeed.toWallet()
        AppDataBase.getInstance().walletDao().loadWallet()
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