package org.session.libsignal.crypto

/**
 * Created by Yaakov on
 * Describe:
 */
data class Wallet(
    var mnemonic: String? = null,
    var pk: String? = null,
    var address: String
)