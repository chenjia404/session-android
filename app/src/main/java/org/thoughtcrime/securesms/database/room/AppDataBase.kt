package org.thoughtcrime.securesms.database.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.thoughtcrime.securesms.ApplicationContext

/**
 * Created by Yaakov on
 * Describe:
 */
@Database(
    entities = [Wallet::class],
    version = 1,
    autoMigrations = [
        //AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun walletDao(): WalletDao

    companion object {
        private const val DATABASE_NAME = "messenger.db"

        @Volatile
        private var databaseInstance: AppDataBase? = null

        @Synchronized
        open fun getInstance(): AppDataBase {
            if (databaseInstance == null) {
                databaseInstance = Room.databaseBuilder(ApplicationContext.context, AppDataBase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build()
            }
            return databaseInstance!!
        }
    }
}
