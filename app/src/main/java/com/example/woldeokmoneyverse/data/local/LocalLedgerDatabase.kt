package com.example.woldeokmoneyverse.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.woldeokmoneyverse.data.model.RealEstateLandDto
import com.example.woldeokmoneyverse.data.model.StockDto
import com.example.woldeokmoneyverse.data.model.WalletOverviewResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalLedgerDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_wallet (
                id TEXT PRIMARY KEY,
                available_wld TEXT,
                locked_stocks TEXT,
                locked_derivatives TEXT,
                total_net_worth TEXT,
                last_updated TEXT,
                raw_json TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_stocks (
                id TEXT PRIMARY KEY,
                symbol TEXT,
                name TEXT,
                current_price TEXT,
                change_pct REAL,
                raw_json TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_lands (
                id TEXT PRIMARY KEY,
                district_id TEXT,
                title TEXT,
                address TEXT,
                owner_id TEXT,
                purchase_price INTEGER,
                daily_rent INTEGER,
                is_auction INTEGER,
                raw_json TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_meta (
                key TEXT PRIMARY KEY,
                value TEXT,
                updated_at INTEGER
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cached_wallet")
        db.execSQL("DROP TABLE IF EXISTS cached_stocks")
        db.execSQL("DROP TABLE IF EXISTS cached_lands")
        db.execSQL("DROP TABLE IF EXISTS cached_meta")
        onCreate(db)
    }

    fun saveWallet(wallet: WalletOverviewResponse) {
        runCatching {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("id", "CURRENT_USER_WALLET")
                put("available_wld", wallet.cashBalance)
                put("locked_stocks", wallet.bankBalance)
                put("locked_derivatives", "0")
                put("total_net_worth", wallet.netWorth)
                put("last_updated", System.currentTimeMillis().toString())
                put("raw_json", gson.toJson(wallet))
            }
            db.insertWithOnConflict("cached_wallet", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun getCachedWallet(): WalletOverviewResponse? {
        return runCatching {
            val db = readableDatabase
            db.query(
                "cached_wallet",
                arrayOf("raw_json"),
                "id = ?",
                arrayOf("CURRENT_USER_WALLET"),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val json = cursor.getString(0)
                    gson.fromJson(json, WalletOverviewResponse::class.java)
                } else null
            }
        }.getOrNull()
    }

    fun saveStocks(stocks: List<StockDto>) {
        runCatching {
            val db = writableDatabase
            db.beginTransaction()
            try {
                db.delete("cached_stocks", null, null)
                stocks.forEach { stock ->
                    val values = ContentValues().apply {
                        put("id", stock.id)
                        put("symbol", stock.symbol)
                        put("name", stock.name)
                        put("current_price", stock.currentPrice)
                        put("change_pct", stock.priceChangePercent)
                        put("raw_json", gson.toJson(stock))
                    }
                    db.insertWithOnConflict("cached_stocks", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun getCachedStocks(): List<StockDto> {
        return runCatching {
            val db = readableDatabase
            val list = mutableListOf<StockDto>()
            db.query("cached_stocks", arrayOf("raw_json"), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val json = cursor.getString(0)
                    gson.fromJson(json, StockDto::class.java)?.let { list.add(it) }
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    fun saveLands(lands: List<RealEstateLandDto>) {
        runCatching {
            val db = writableDatabase
            db.beginTransaction()
            try {
                db.delete("cached_lands", null, null)
                lands.forEach { land ->
                    val values = ContentValues().apply {
                        put("id", land.id)
                        put("district_id", land.districtId)
                        put("title", land.title)
                        put("address", land.address)
                        put("owner_id", land.ownerId)
                        put("purchase_price", land.purchasePrice)
                        put("daily_rent", land.dailyRent)
                        put("is_auction", if (land.isAuctionActive) 1 else 0)
                        put("raw_json", gson.toJson(land))
                    }
                    db.insertWithOnConflict("cached_lands", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun getCachedLands(): List<RealEstateLandDto> {
        return runCatching {
            val db = readableDatabase
            val list = mutableListOf<RealEstateLandDto>()
            db.query("cached_lands", arrayOf("raw_json"), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val json = cursor.getString(0)
                    gson.fromJson(json, RealEstateLandDto::class.java)?.let { list.add(it) }
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val DB_NAME = "woldeok_offline_ledger.db"
        private const val DB_VERSION = 1

        @Volatile
        private var instance: LocalLedgerDatabase? = null

        fun getInstance(context: Context): LocalLedgerDatabase =
            instance ?: synchronized(this) {
                instance ?: LocalLedgerDatabase(context).also { instance = it }
            }
    }
}
