package org.thoughtcrime.securesms.net.network

import okhttp3.MultipartBody
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.et.Comment
import org.thoughtcrime.securesms.et.Create
import org.thoughtcrime.securesms.et.ET
import org.thoughtcrime.securesms.et.Nonce
import org.thoughtcrime.securesms.et.User

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

    suspend fun authorize(nonce: String, sign: String, address: String): User? {
        return api.authorize(nonce, sign, address).Data
    }

    suspend fun releaseComment(address: String, content: String): BaseResponse<Unit?> {
        return api.releaseComment(address, content)
    }

    suspend fun create(content: String, attachment: String = "", forwardId: String = ""): BaseResponse<Create?> {
        return api.create(content, attachment, forwardId)
    }

    suspend fun release(id: String, sign: String): BaseResponse<Unit?> {
        return api.release(id, sign)
    }

    suspend fun uploadFile(part: MultipartBody.Part): IpfsResponse? {
        return api.uploadFile(part)
    }

    suspend fun loadETFollow(cursor: String = ""): List<ET>? {
        return api.loadETFollow(cursor).Data
    }
}