package com.example.woldeokmoneyverse.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.woldeokmoneyverse.data.local.LocalLedgerDatabase
import com.example.woldeokmoneyverse.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class OfflineLedgerSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = LocalLedgerDatabase.getInstance(applicationContext)

            // 1. 지갑 및 은행 잔고 silent fetch & 캐싱
            runCatching {
                val walletRes = ApiClient.api.getWalletOverview()
                if (walletRes.isSuccessful && walletRes.body() != null) {
                    db.saveWallet(walletRes.body()!!)
                    Log.d(TAG, "Offline sync: Wallet saved to local ledger.")
                }
            }

            // 2. 보유/시장 주식 목록 silent fetch & 캐싱
            runCatching {
                val stocksRes = ApiClient.api.getStocks()
                if (stocksRes.isSuccessful && stocksRes.body() != null) {
                    db.saveStocks(stocksRes.body()!!)
                    Log.d(TAG, "Offline sync: Stocks saved to local ledger.")
                }
            }

            // 3. 부동산 랜드 목록 silent fetch & 캐싱
            runCatching {
                val landsRes = ApiClient.api.getRealEstateLands()
                if (landsRes.isSuccessful && landsRes.body() != null) {
                    db.saveLands(landsRes.body()!!)
                    Log.d(TAG, "Offline sync: Lands saved to local ledger.")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Offline sync worker exception", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "OfflineLedgerSyncWorker"
        private const val PERIODIC_WORK_NAME = "woldeok_ledger_periodic_sync"
        private const val ONETIME_WORK_NAME = "woldeok_ledger_immediate_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<OfflineLedgerSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<OfflineLedgerSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                ONETIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }
}
