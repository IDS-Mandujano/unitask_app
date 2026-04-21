package com.example.unitask_app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("career") val career: String,
    @SerializedName("university") val university: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("career") val career: String,
    @SerializedName("university") val university: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: User
)

data class Subject(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("teacher_email") val teacherEmail: String? = null
)

data class Task(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("subject_id") val subjectId: Int = 0,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("teacher_name") val teacherName: String? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("due_date") val dueDate: String = "", // ISO8601
    @SerializedName("is_completed") val isCompleted: Boolean = false
)

data class Attachment(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("subject_id") val subjectId: Int? = null,
    @SerializedName("task_id") val taskId: Int? = null,
    @SerializedName("file_name") val fileName: String = "",
    @SerializedName("mime_type") val mimeType: String? = null,
    @SerializedName("storage_path") val storagePath: String = "",
    @SerializedName("download_url") val downloadUrl: String = "",
    @SerializedName("attachment_type") val attachmentType: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class AttachmentRequest(
    @SerializedName("subject_id") val subjectId: Int? = null,
    @SerializedName("task_id") val taskId: Int? = null,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("mime_type") val mimeType: String? = null,
    @SerializedName("storage_path") val storagePath: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("attachment_type") val attachmentType: String? = null
)

data class DeviceTokenRequest(
    @SerializedName("device_token") val deviceToken: String,
    @SerializedName("platform") val platform: String = "android",
    @SerializedName("device_id") val deviceId: String = ""
)
