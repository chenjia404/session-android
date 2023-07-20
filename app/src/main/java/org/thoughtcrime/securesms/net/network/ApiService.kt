package org.thoughtcrime.securesms.net.network

import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.home.Authorize
import org.thoughtcrime.securesms.home.Comment
import org.thoughtcrime.securesms.home.ET
import org.thoughtcrime.securesms.home.Nonce

/**
 * Created by Yaakov on
 * Describe:
 */
class ApiService {

    val api: Api by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkApi.INSTANCE.getApi(Api::class.java, AppConst.URLS.BASE)
    }

    suspend fun loadET(cursor: String = ""): List<ET>? {
        return api.loadET(cursor).Data
    }

    suspend fun loadComments(address: String, page: Int): List<Comment>? {
        return api.loadComments(address, page).Data
    }

    suspend fun loadNonce(address: String, sign: String): Nonce? {
        return api.loadNonce(address, sign).Data
    }

    suspend fun authorize(nonce: String, sign: String, address: String): Authorize? {
        return api.authorize(nonce, sign, address).Data
    }
}