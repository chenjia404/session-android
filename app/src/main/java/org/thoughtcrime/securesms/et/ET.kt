package org.thoughtcrime.securesms.et

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Yaakov on
 * Describe:
 */
@Parcelize
data class ET(
    var Attachment: String?,
    var CommentCount: Int?,
    var Content: String?,
    var CreatedAt: String?,
    var Cursor: String?,
    var DonateCount: Int?,
    var ForwardCount: Int?,
    var ID: String?,
    var LikeCount: Int?,
    var OriginTweet: ET?,
    var TwAddress: String?,
    var UserInfo: UserInfo?,
    var isTwLike: Boolean
) : Parcelable

@Parcelize
data class UserInfo(
    var Avatar: String,
    var IsFollow: Boolean,
    var Nickname: String,
    var UserAddress: String
) : Parcelable


@Parcelize
data class Media(
    // 0 pic 1 video 2 more
    var type: Int,
    var url: String
) : Parcelable


@Parcelize
data class Comment(
    var Content: String,
    var CreatedAt: String,
    var ReplyNum: Int,
    var ReplyUserInfo: UserInfo,
    var UserAddress: String,
    var UserInfo: UserInfo,
    var Uuid: String
) : Parcelable


data class Nonce(
    var Nonce: String,
    var SignMsg: String,
    var UserAddress: String
)

data class Authorize(
    var ID: String,
    var UserAddress: String,
    var PubKey: String,
    var Nickname: String,
    var Token: String
)

data class Create(
    var Id: String,
    var SignMsg: String
)