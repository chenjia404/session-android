package org.thoughtcrime.securesms.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Created by Yaakov on
 * Describe:
 */
@Dao
abstract class WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(wallet: Wallet)

    @Delete
    abstract fun delete(wallet: Wallet)

    @Update
    abstract fun update(wallet: Wallet)

    @Query("SELECT * FROM Wallet LIMIT 1")
    abstract fun loadWallet(): Wallet
}