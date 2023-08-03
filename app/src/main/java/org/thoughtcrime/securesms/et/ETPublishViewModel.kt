package org.thoughtcrime.securesms.et

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.home.web3.TransactionService
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.net.network.IpfsResponse
import org.thoughtcrime.securesms.util.IntentType
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.File

/**
 * Created by Yaakov on
 * Describe:
 */
class ETPublishViewModel(application: Application) : BaseViewModel(application) {

    var page: Int = 1

    val publishStatusLiveData = MutableLiveData<Boolean>()
    val ipfsLiveData = MutableLiveData<Media?>()

    private val apiService by lazy {
        ApiService()
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