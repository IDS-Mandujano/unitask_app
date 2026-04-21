package com.example.unitask_app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.unitask_app.data.model.Attachment
import com.example.unitask_app.data.model.AttachmentRequest
import com.example.unitask_app.data.api.UniTaskApiService
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: UniTaskApiService
) {
    private val storage by lazy {
        val bucket = FirebaseApp.getInstance().options.storageBucket
        if (!bucket.isNullOrBlank()) {
            FirebaseStorage.getInstance("gs://$bucket")
        } else {
            FirebaseStorage.getInstance()
        }
    }

    suspend fun uploadBitmap(
        bitmap: Bitmap,
        attachmentType: String,
        subjectId: Int? = null,
        taskId: Int? = null,
    ): Attachment {
        val fileName = "${UUID.randomUUID()}.jpg"
        val tempFile = File.createTempFile("unitask_${UUID.randomUUID()}", ".jpg", context.cacheDir)
        return try {
            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            uploadUri(
                uri = Uri.fromFile(tempFile),
                attachmentType = attachmentType,
                subjectId = subjectId,
                taskId = taskId,
                mimeTypeOverride = "image/jpeg",
                fileNameOverride = fileName
            )
        } finally {
            tempFile.delete()
        }
    }

    suspend fun uploadUri(
        uri: Uri,
        attachmentType: String,
        subjectId: Int? = null,
        taskId: Int? = null,
        mimeTypeOverride: String? = null,
        fileNameOverride: String? = null,
    ): Attachment {
        val resolver = context.contentResolver
        val mimeType = mimeTypeOverride ?: resolver.getType(uri) ?: "application/octet-stream"
        val fileName = fileNameOverride ?: queryDisplayName(uri) ?: "${UUID.randomUUID()}"
        val storagePath = buildStoragePath(attachmentType, fileName, taskId, subjectId)
        val ref = storage.reference.child(storagePath)
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        return registerAttachment(
            fileName = fileName,
            mimeType = mimeType,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            attachmentType = attachmentType,
            subjectId = subjectId,
            taskId = taskId
        )
    }

    suspend fun getAttachments(taskId: Int? = null, subjectId: Int? = null): List<Attachment> {
        return apiService.getAttachments(taskId = taskId, subjectId = subjectId).body().orEmpty()
    }

    private suspend fun registerAttachment(
        fileName: String,
        mimeType: String?,
        storagePath: String,
        downloadUrl: String,
        attachmentType: String,
        subjectId: Int?,
        taskId: Int?
    ): Attachment {
        val response = apiService.registerAttachment(
            AttachmentRequest(
                subjectId = subjectId,
                taskId = taskId,
                fileName = fileName,
                mimeType = mimeType,
                storagePath = storagePath,
                downloadUrl = downloadUrl,
                attachmentType = attachmentType
            )
        )
        if (!response.isSuccessful || response.body() == null) {
            try {
                storage.reference.child(storagePath).delete().await()
            } catch (_: Exception) {
            }
            throw IllegalStateException("No se pudo registrar el adjunto en el backend")
        }
        return response.body()!!
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
        return cursor.use {
            val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) it.getString(index) else null
        }
    }

    private fun buildStoragePath(
        attachmentType: String,
        fileName: String,
        taskId: Int?,
        subjectId: Int?
    ): String {
        val bucket = when {
            taskId != null -> "tasks/$taskId"
            subjectId != null -> "subjects/$subjectId"
            else -> "users/general"
        }
        return "$bucket/$attachmentType/$fileName"
    }
}