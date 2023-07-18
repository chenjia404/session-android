package org.thoughtcrime.securesms.net.network

import org.thoughtcrime.securesms.home.ET
import retrofit2.http.*

interface Api {

    companion object {
        const val URL_BASE = "url_name:base"
    }

    @Headers(URL_BASE)
    @GET("/api/v0/index")
    suspend fun loadET(@Query("cursor") cursor: String): BaseResponse<List<ET>?>

}