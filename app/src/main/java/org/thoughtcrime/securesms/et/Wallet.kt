package org.thoughtcrime.securesms.et

/**
 * Created by Yaakov on
 * Describe:
 */
data class Wallet(
    var mnemonic: String? = null,
    var pk: String? = null,
    var address: String
)