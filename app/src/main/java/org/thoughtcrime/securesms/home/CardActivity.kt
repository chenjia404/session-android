package org.thoughtcrime.securesms.home

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.view.isVisible
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityCardBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.util.FileProviderUtil
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.QRCodeUtilities
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.toPx
import org.thoughtcrime.securesms.util.toastOnUi
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class CardActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityCardBinding

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, true, false, R.color.core_white)
        initView()

    }


    private fun initView() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            var sessionId = TextSecurePreferences.getLocalNumber(this@CardActivity)
            val userStr = TextSecurePreferences.getUser(this@CardActivity)
            var user = Gson().fromJson(userStr, User::class.java)
            tvName.text = getString(R.string.card)
            GlideHelper.showImage(
                ivAvatar,
                user?.Avatar ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            val size = toPx(102, resources)
            val qrCode = QRCodeUtilities.encode(sessionId!!, size, isInverted = false, hasTransparentBackground = false)
            ivQR.setImageBitmap(qrCode)
            tvUserName.text = user.Nickname
            tvId.text = sessionId
            tvFollowNum.text = "${user.FollowCount}"
            tvFollowerNum.text = "${user.FansCount}"
            tvCopy.setOnClickListener {
                sendToClip(sessionId)
            }
            llSave.setOnClickListener {
                Permissions.with(this@CardActivity)
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .maxSdkVersion(Build.VERSION_CODES.P)
                    .withPermanentDenialDialog(getString(R.string.MediaPreviewActivity_signal_needs_the_storage_permission_in_order_to_write_to_external_storage_but_it_has_been_permanently_denied))
                    .onAnyDenied { toastOnUi(R.string.MediaPreviewActivity_unable_to_write_to_external_storage_without_permission) }
                    .onAllGranted {
                        var bitmap = getBitmapFromView(binding.root)
                        val success = saveBitmapGallery(bitmap)
                        if (success) {
                            toastOnUi(getString(R.string.save_success))
                        }
                    }
                    .execute()
            }
            llShare.setOnClickListener {
                share(sessionId)
            }
        }

    }

    private fun getBitmapFromView(view: View): Bitmap {
        binding.toolbar.isVisible = false
        binding.llSave.isVisible = false
        binding.llShare.isVisible = false
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        binding.toolbar.isVisible = true
        binding.llSave.isVisible = true
        binding.llShare.isVisible = true
        return bitmap
    }

    private fun share(sessionId: String) {
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "$sessionId.png"
        val file = File(directory, fileName)
        file.createNewFile()
        val fos = FileOutputStream(file)
        var bitmap = getBitmapFromView(binding.root)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, FileProviderUtil.getUriFor(this, file))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(Intent.createChooser(intent, resources.getString(R.string.fragment_view_my_qr_code_share_title)))
    }

    private fun saveBitmapGallery(bitmap: Bitmap): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val insert = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            ) ?: return false
            contentResolver.openOutputStream(insert).use {
                it ?: return false
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            return true
        } else {
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, "", "")
            return true
        }
    }


}
