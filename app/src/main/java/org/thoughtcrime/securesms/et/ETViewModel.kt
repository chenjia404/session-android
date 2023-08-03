package org.thoughtcrime.securesms.et

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.home.web3.TransactionService
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.mms.GlideApp
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.net.network.IpfsResponse
import org.thoughtcrime.securesms.util.IntentType
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.util.Date

/**
 * Created by Yaakov on
 * Describe:
 */
class ETViewModel(application: Application) : BaseViewModel(application) {

    var cursor: String = ""
    var page: Int = 1

    val etsLiveData = MutableLiveData<List<ET>?>()
    val followLiveData = MutableLiveData<List<User>?>()
    val followStatusLiveData = MutableLiveData<Boolean>()
    val ipfsLiveData = MutableLiveData<Media?>()
    val updateUserStatusLiveData = MutableLiveData<Boolean>()
    val userLiveData = MutableLiveData<User?>()
    val userInfoLiveData = MutableLiveData<UserInfo?>()
    val commentsLiveData = MutableLiveData<List<Comment>?>()
    val publishStatusLiveData = MutableLiveData<Boolean>()

    private val apiService by lazy {
        ApiService()
    }

    fun loadET(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadET(cursor)
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

    fun login() {
        execute {
            val signAddress = TransactionService.signEthereumMessage(wallet, wallet.address.toByteArray(Charsets.UTF_8), addPrefix = true)
            val nonce = apiService.loadNonce(wallet.address, signAddress)
            if (nonce != null) {
                val signMessage = TransactionService.signEthereumMessage(wallet, nonce.SignMsg.toByteArray(Charsets.UTF_8), addPrefix = true)
                apiService.authorize(nonce.Nonce, signMessage, wallet.address)
            } else {
                null
            }
        }.onSuccess {
            if (it != null) {
                userLiveData.postValue(it)
                val userJson = Gson().toJson(it)
                TextSecurePreferences.setXToken(context, it.Token ?: "")
                TextSecurePreferences.setUser(context, userJson)
            }

        }.onError {
            context.toastOnUi(it.message)
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

    fun loadFollowing(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadFollowing(page)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun loadFollowers(
        onStart: () -> Unit,
        onFinally: () -> Unit,
    ) {
        execute {
            apiService.loadFollowers(page)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            followLiveData.postValue(it)
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

    fun uploadFile(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        media: Media
    ) {
        execute {
            val path = getPathFromUri(context, media.uri)
            val file = File(path)
            val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(IntentType.from(path)?.toMediaType()))
            apiService.uploadFile(part)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            if (!it?.Hash.isNullOrEmpty()) {
                media.url = "${AppConst.URLS.IPFS_SCAN}/${it?.Hash}?filename=${it?.Name}"
                ipfsLiveData.postValue(media)
            }
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun updateUser(
        onStart: () -> Unit, onFinally: () -> Unit, avatar: String, nickname: String, desc: String, sex: String, updateSignUnix: String
    ) {
        execute {
            var list = listOf(wallet.address, nickname, desc, avatar, updateSignUnix)
            val message = list.joinToString("|")
            val signAddress = TransactionService.signEthereumMessage(wallet, message.toByteArray(Charsets.UTF_8), addPrefix = true)
            apiService.updateUser(avatar, nickname, desc, sex, signAddress, updateSignUnix)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            updateUserStatusLiveData.postValue(it.Code == 0)
            context.toastOnUi(it.Msg)
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

    fun loadComments(address: String) {
        execute {
            apiService.loadComments(address, page)
        }.onSuccess {
            commentsLiveData.postValue(it)
        }.onError {
            context.toastOnUi(it.message)
        }
    }

    fun releaseComment(et_address: String, content: String) {
        execute {
            apiService.releaseComment(et_address, content)
        }.onSuccess {
            context.toastOnUi(it.Msg)
        }.onError {
            context.toastOnUi(it.message)
        }
    }

    fun publish(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        content: String,
        medias: List<Media>,
        forwardId: String
    ) {
        execute {
            var attaches = arrayListOf<String>()
            medias.forEach {
                val result = uploadFile(it)
                if (!result?.Hash.isNullOrEmpty()) {
                    attaches.add("${AppConst.URLS.IPFS_SCAN}/${result?.Hash}?filename=${result?.Name}")
                }
            }
            val attachment = attaches.joinToString(",")
            val response = apiService.create(content, attachment, forwardId)
            if (response.Data != null) {
                val signMessage = TransactionService.signEthereumMessage(wallet, response.Data.SignMsg.toByteArray(Charsets.UTF_8), addPrefix = true)
                apiService.release(response.Data.Id, signMessage)
            } else {
                response
            }
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            publishStatusLiveData.postValue(it?.Code == 0)
            context.toastOnUi(it?.Msg)
        }.onError {
            context.toastOnUi(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }




    suspend fun uploadFile(
        media: Media
    ): IpfsResponse? {
        val path = getPathFromUri(context, media.uri)
        val file = File(path)
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(IntentType.from(path)?.toMediaType()))
        return apiService.uploadFile(part)
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

    private fun getPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            return columnIndex?.let { cursor?.getString(it) } ?: ""
        } finally {
            cursor?.close()
        }
    }
}