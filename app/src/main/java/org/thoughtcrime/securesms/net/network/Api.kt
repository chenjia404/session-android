package org.thoughtcrime.securesms.net.network

import org.thoughtcrime.securesms.home.Authorize
import org.thoughtcrime.securesms.home.Comment
import org.thoughtcrime.securesms.home.ET
import org.thoughtcrime.securesms.home.Nonce
import retrofit2.http.*

interface Api {

    companion object {
        const val URL_BASE = "url_name:base"
    }

    @Headers(URL_BASE)
    @GET("/api/v0/index")
    suspend fun loadET(@Query("cursor") cursor: String): BaseResponse<List<ET>?>

    @Headers(URL_BASE)
    @GET("/api/v0/comment/list")
    suspend fun loadComments(@Query("tw_address") address: String, @Query("page") page: Int): BaseResponse<List<Comment>?>

    @Headers(URL_BASE)
    @GET("/api/v0/nonce")
    suspend fun loadNonce(@Query("user_address") address: String, @Query("sign") sign: String): BaseResponse<Nonce?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/authorize")
    suspend fun authorize(@Field("nonce") nonce: String, @Field("sign") sign: String, @Field("user_address") address: String): BaseResponse<Authorize?>

}