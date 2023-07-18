package org.thoughtcrime.securesms.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Yaakov on
 * Describe:
 */
@Parcelize
data class ET(
    var Attachment: String,
    var CommentCount: Int,
    var Content: String,
    var CreatedAt: String,
    var Cursor: String,
    var DonateCount: Int,
    var ForwardCount: Int,
    var ID: String,
    var LikeCount: Int,
    var OriginTweet: ET,
    var TwAddress: String,
    var UserInfo: UserInfo,
    var isTwLike: Boolean
) : Parcelable

@Parcelize
data class UserInfo(
    var Avatar: String,
    var IsFollow: Boolean,
    var Nickname: String,
    var UserAddress: String
) : Parcelable
