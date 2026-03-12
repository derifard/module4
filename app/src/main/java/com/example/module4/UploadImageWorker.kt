package com.example.module4


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class UploadImageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val watermarkedPath = inputData.getString("WATERMARKED_PATH") ?: "default.jpg"

                for (i in 1..10) {
                    delay(300)
                    setProgress(workDataOf("PROGRESS" to i * 10))
                }

                val uploadedPath = "uploaded_$watermarkedPath"
                val outputData = workDataOf(
                    "UPLOADED_PATH" to uploadedPath,
                    "STEP" to "upload"
                )

                Result.success(outputData)
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}