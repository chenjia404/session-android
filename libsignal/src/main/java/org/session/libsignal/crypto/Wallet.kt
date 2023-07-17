package org.session.libsignal.crypto

/**
 * Created by Yaakov on
 * Describe:
 */
data class Wallet(
    val mnemonic: String? = null,
    val pk: String? = null,
    val address: String
)