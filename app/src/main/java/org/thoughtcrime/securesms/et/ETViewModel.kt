package org.thoughtcrime.securesms.et

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.home.web3.TransactionService
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.IntentType
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.File

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
    val likeLiveData = MutableLiveData<ET>()
    val ipfsLiveData = MutableLiveData<Media?>()
    val updateUserStatusLiveData = MutableLiveData<Boolean>()
    val userLiveData = MutableLiveData<User?>()

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
            if (it.Code == 0) {
                et.isTwLike = !et.isTwLike
                if (et.isTwLike) {
                    et.LikeCount = et.LikeCount?.plus(1)
                } else {
                    et.LikeCount = et.LikeCount?.minus(1)
                }
            }
            likeLiveData.postValue(et)
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