package com.example.module4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class AddWatermarkWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val compressedPath = inputData.getString("COMPRESSED_PATH") ?: "default.jpg"

                for (i in 1..10) {
                    delay(300)
                    setProgress(workDataOf("PROGRESS" to i * 10))
                }

                val watermarkedPath = "watermarked_$compressedPath"
                val outputData = workDataOf(
                    "WATERMARKED_PATH" to watermarkedPath,
                    "STEP" to "watermark"
                )

                Result.success(outputData)
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}