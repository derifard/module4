package com.example.module4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class CompressImageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val imagePath = inputData.getString("IMAGE_PATH") ?: "default.jpg"

                for (i in 1..10) {
                    delay(300)
                    setProgress(workDataOf("PROGRESS" to i * 10))
                }

                val compressedPath = "compressed_$imagePath"
                val outputData = workDataOf(
                    "COMPRESSED_PATH" to compressedPath,
                    "STEP" to "compress"
                )

                Result.success(outputData)
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}