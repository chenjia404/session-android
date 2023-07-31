package org.thoughtcrime.securesms.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Yaakov on
 * Describe:
 */
@Entity
data class Wallet(
    @PrimaryKey(autoGenerate=true)
    var id :Int = 0,
    var mnemonic: String? = null,
    var pk: String? = null,
    var address: String
)