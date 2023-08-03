package org.thoughtcrime.securesms.util

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Calendar
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

/**
 * Created by Author on 2020/6/17
 */
object KeyStoreUtils {
    private const val ALIAS = "Wallet"
    private const val x500PrincipalName = "CN=QK, O=Android Authority"
    fun initialize(appContext: Context?) {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            if (!ks.containsAlias(ALIAS)) {
                val startDate = Calendar.getInstance()
                val endDate = Calendar.getInstance()
                endDate.add(Calendar.YEAR, 30)
                val kpGen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    kpGen.initialize(
                        KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .setCertificateSubject(X500Principal(x500PrincipalName))
                            .setCertificateNotBefore(startDate.time)
                            .setCertificateNotAfter(endDate.time)
                            .build()
                    )
                } else {
                    kpGen.initialize(
                        KeyPairGeneratorSpec.Builder(appContext!!)
                            .setAlias(ALIAS)
                            .setSubject(X500Principal(x500PrincipalName))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(startDate.time)
                            .setEndDate(endDate.time)
                            .build()
                    )
                }
                kpGen.generateKeyPair()
            }
        } catch (e: Exception) {
            Logger.e(e.message)
        }
    }

    fun encrypt(data: String): String {
        if (data.isNullOrEmpty()) {
            return ""
        }
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val c = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        c.init(Cipher.ENCRYPT_MODE, ks.getCertificate(ALIAS).publicKey)
        return Base64.encodeToString(c.doFinal(data.toByteArray(charset("UTF-8"))), Base64.DEFAULT)
    }

    fun decrypt(data: String?): String {
        if (data.isNullOrEmpty()) {
            return ""
        }
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val c = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        c.init(Cipher.DECRYPT_MODE, ks.getKey(ALIAS, null))
        return String(c.doFinal(Base64.decode(data, Base64.DEFAULT)))
    }

    fun verity(key1: String, encrypt: String?): Boolean {
        val key2 = decrypt(encrypt)
        return key1.equals(key2, ignoreCase = true)
    }
}