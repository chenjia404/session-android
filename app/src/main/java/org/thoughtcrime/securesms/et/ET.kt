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
    var UserInfo: User?,
    var isTwLike: Boolean
) : Parcelable

@Parcelize
data class UserInfo(
    var Tweets: List<ET>,
    var user: User
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
    var ReplyUserInfo: User,
    var UserAddress: String,
    var UserInfo: User,
    var Uuid: String
) : Parcelable


data class Nonce(
    var Nonce: String,
    var SignMsg: String,
    var UserAddress: String
)

@Parcelize
data class User(
    var ID: String?,
    var UserAddress: String?,
    var PubKey: String?,
    var Nickname: String?,
    var Token: String?,
    var Avatar: String?,
    var Desc: String?,
    var FollowCount: String?,
    var FansCount: String?,
    var TokenStatus: Int?,
    var IpfsHash: String?,
    var TwCount: String?,
    var IsFollow: Boolean?
) : Parcelable

data class Create(
    var Id: String,
    var SignMsg: String
)