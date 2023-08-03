package org.thoughtcrime.securesms.et

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import okio.Buffer
import org.session.libsession.avatars.AvatarHelper
import org.session.libsession.utilities.Address
import org.session.libsession.utilities.ProfileKeyUtil
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsignal.streams.DigestingRequestBody
import org.session.libsignal.streams.ProfileCipherOutputStream
import org.session.libsignal.streams.ProfileCipherOutputStreamFactory
import org.session.libsignal.utilities.ProfileAvatarData
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.mms.GlideApp
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.Date

/**
 * Created by Yaakov on
 * Describe:
 */
class MeViewModel(application: Application) : BaseViewModel(application) {

    var cursor: String = ""

    val etsLiveData = MutableLiveData<List<ET>?>()
    val userInfoLiveData = MutableLiveData<UserInfo?>()
    val followStatusLiveData = MutableLiveData<Boolean>()

    private val apiService by lazy {
        ApiService()
    }

    fun loadETFollow(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadETFollow(cursor)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            etsLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun loadUserInfo(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.loadUserInfo(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            userInfoLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun loadETTimeline(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.loadETTimeline(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            etsLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun follow(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.follow(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followStatusLiveData.postValue(it.Code == 0)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun cancelFollow(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        address: String
    ) {
        execute {
            apiService.cancelFollow(address)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followStatusLiveData.postValue(it.Code == 0)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun like(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        et: ET
    ) {
        execute {
            apiService.like(et.TwAddress ?: "")
        }.onStart {
            onStart.invoke()
        }.onSuccess {
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun updateLocalUser(user: User) {
        execute {
            // update nickname
            val localNickname = TextSecurePreferences.getProfileName(context)
            if (!localNickname.equals(user.Nickname)) {
                TextSecurePreferences.setProfileName(context, user.Nickname)
            }
            // update avatar
            user.Avatar?.let {
                val localEncodedProfileKey = TextSecurePreferences.getProfileKey(context)
                val userJson = TextSecurePreferences.getUser(context)
                val localUser = Gson().fromJson(userJson, User::class.java)
                if (!localEncodedProfileKey.equals(localUser.EncodedProfileKey)) {
                    GlideApp.with(context).asBitmap().load(user.Avatar).into(object : SimpleTarget<Bitmap>() {

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val baos = ByteArrayOutputStream()
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val byteArray = baos.toByteArray()
                            val encodedProfileKey = ProfileKeyUtil.generateEncodedProfileKey(context)
                            val inputStream = ByteArrayInputStream(byteArray)
                            val outputStream = ProfileCipherOutputStream.getCiphertextLength(byteArray.size.toLong())
                            val profileKey = ProfileKeyUtil.getProfileKeyFromEncodedString(encodedProfileKey)
                            val pad = ProfileAvatarData(inputStream, outputStream, "image/jpeg", ProfileCipherOutputStreamFactory(profileKey))
                            val drb = DigestingRequestBody(pad.data, pad.outputStreamFactory, pad.contentType, pad.dataLength, null)
                            val b = Buffer()
                            drb.writeTo(b)
                            TextSecurePreferences.setLastProfilePictureUpload(context, Date().time)
                            TextSecurePreferences.setProfilePictureURL(context, user.Avatar)

                            AvatarHelper.setAvatar(
                                context,
                                Address.fromSerialized(TextSecurePreferences.getLocalNumber(context)!!),
                                byteArray
                            )
                            TextSecurePreferences.setProfileAvatarId(
                                context,
                                byteArray?.let { SecureRandom().nextInt() } ?: 0)
                            TextSecurePreferences.setLastProfilePictureUpload(context, Date().time)
                            ProfileKeyUtil.setEncodedProfileKey(context, encodedProfileKey)

                            user.EncodedProfileKey = encodedProfileKey
                            val userJson = Gson().toJson(user)
                            TextSecurePreferences.setUser(context, userJson)
                        }
                    })
                }
            }
        }.onSuccess {
            Logger.d("update local user success")
        }.onError {
            Logger.e(it.message)
        }
    }
}
