package org.thoughtcrime.securesms.net.network

import okhttp3.MultipartBody
import org.thoughtcrime.securesms.et.Comment
import org.thoughtcrime.securesms.et.Create
import org.thoughtcrime.securesms.et.ET
import org.thoughtcrime.securesms.et.Nonce
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.et.UserInfo
import retrofit2.http.*

interface Api {

    companion object {
        const val URL_BASE = "url_name:base"
        const val URL_IPFS = "url_name:ipfs"
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
    suspend fun authorize(@Field("nonce") nonce: String, @Field("sign") sign: String, @Field("user_address") address: String): BaseResponse<User?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/comment/release")
    suspend fun releaseComment(@Field("tw_address") address: String, @Field("content") content: String): BaseResponse<Unit?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/tweets/create")
    suspend fun create(@Field("content") content: String, @Field("attachment") attachment: String, @Field("forwardId") forwardId: String): BaseResponse<Create?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/tweets/release")
    suspend fun release(@Field("id") id: String, @Field("sign") sign: String): BaseResponse<Unit?>

    @Headers(URL_IPFS)
    @Multipart
    @POST("/api/v0/add")
    suspend fun uploadFile(@Part part: MultipartBody.Part): IpfsResponse?

    @Headers(URL_BASE)
    @GET("/api/v0/tweets/follow")
    suspend fun loadETFollow(@Query("cursor") cursor: String): BaseResponse<List<ET>?>

    @Headers(URL_BASE)
    @GET("/api/v0/users/main")
    suspend fun loadUserInfo(@Query("user_address") address: String): BaseResponse<UserInfo?>

    @Headers(URL_BASE)
    @GET("/api/v0/tweets/timeline")
    suspend fun loadETTimeline(@Query("user_address") address: String): BaseResponse<List<ET>?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/users/follow")
    suspend fun follow(@Field("user_address") address: String): BaseResponse<Unit?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/users/cancelFollow")
    suspend fun cancelFollow(@Field("user_address") address: String): BaseResponse<Unit?>

    @Headers(URL_BASE)
    @GET("/api/v0/users/follow")
    suspend fun loadFollowing(@Query("page") page: Int): BaseResponse<List<User>?>

    @Headers(URL_BASE)
    @GET("/api/v0/users/fans")
    suspend fun loadFollowers(@Query("page") page: Int): BaseResponse<List<User>?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/tweets/like")
    suspend fun like(@Field("tw_address") twAddress: String): BaseResponse<Unit?>

    @Headers(URL_BASE)
    @FormUrlEncoded
    @POST("/api/v0/users/update")
    suspend fun updateUser(@Field("avatar") avatar: String, @Field("nickname") nickname: String, @Field("desc") desc: String, @Field("sex") sex: String, @Field("sign") sign: String, @Field("updateSignUnix") updateSignUnix: String): BaseResponse<Unit?>

}