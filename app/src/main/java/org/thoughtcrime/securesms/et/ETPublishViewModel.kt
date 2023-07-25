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
import org.thoughtcrime.securesms.home.web3.TransactionService
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.net.network.IpfsResponse
import org.thoughtcrime.securesms.util.IntentType
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.File

/**
 * Created by Yaakov on
 * Describe:
 */
class ETPublishViewModel(application: Application) : BaseViewModel(application) {

    var page: Int = 1

    val publishStatusLiveData = MutableLiveData<Boolean>()
    val ipfsLiveData = MutableLiveData<IpfsResponse?>()

    private val apiService by lazy {
        ApiService()
    }

    fun publish(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        content: String,
        attachment: String,
        forwardId: String
    ) {
        execute {
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

    fun uploadFile(
        onStart: () -> Unit,
        onFinally: () -> Unit,
        uri: Uri
    ) {
        execute {
            val path = getPathFromUri(context, uri)
            val file = File(path)
            Logger.d("path2 = ${file.path}")
            val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(IntentType.from(path)?.toMediaType()))
            apiService.uploadFile(part)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            Logger.d("uploadFile = $it")
            ipfsLiveData.postValue(it)
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