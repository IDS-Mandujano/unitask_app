package com.example.unitask_app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.unitask_app.BuildConfig
import com.example.unitask_app.data.model.Attachment
import com.example.unitask_app.data.model.AttachmentRequest
import com.example.unitask_app.data.api.UniTaskApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
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
    private val httpClient = OkHttpClient()
    private val cloudinaryUrl = "https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/upload"

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
        
        val downloadUrl = uploadToCloudinary(uri, storagePath)
        
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

    private suspend fun uploadToCloudinary(uri: Uri, publicId: String): String {
        return withContext(Dispatchers.IO) {
            val file = uriToFile(uri)
            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
                    .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                    .addFormDataPart("public_id", publicId)
                    .addFormDataPart("folder", "unitask")
                    .build()

                val request = Request.Builder()
                    .url(cloudinaryUrl)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Cloudinary upload failed: ${response.code}")
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val secureUrl = jsonResponse.optString("secure_url", "")
                if (secureUrl.isEmpty()) {
                    throw Exception("No URL en respuesta de Cloudinary")
                }
                secureUrl
            } finally {
                file.delete()
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val tempFile = File.createTempFile("unitask_${UUID.randomUUID()}", ".tmp", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
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