package org.thoughtcrime.securesms.net.network

data class BaseResponse<T> (var Code: Int, val Msg: String, val Data: T)