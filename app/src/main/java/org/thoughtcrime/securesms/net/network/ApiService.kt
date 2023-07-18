package org.thoughtcrime.securesms.net.network

import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.home.ET

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
}