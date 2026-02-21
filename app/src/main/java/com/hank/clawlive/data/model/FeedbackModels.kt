package com.hank.clawlive.data.model

data class FeedbackResponse(
    val success: Boolean,
    val message: String? = null,
    val feedbackId: Int? = null,
    val severity: String? = null,
    val tags: List<String>? = null,
    val diagnosis: String? = null,
    val githubIssue: GithubIssueInfo? = null,
    val logsCaptured: LogsCaptured? = null
)

data class GithubIssueInfo(
    val url: String,
    val number: Int
)

data class LogsCaptured(
    val telemetry: Int = 0,
    val serverLogs: Int = 0,
    val windowMs: Long = 0
)

data class FeedbackListResponse(
    val success: Boolean,
    val count: Int = 0,
    val feedback: List<FeedbackItem> = emptyList()
)

data class FeedbackItem(
    val id: Int,
    val device_id: String? = null,
    val message: String? = null,
    val category: String? = null,
    val severity: String? = null,
    val status: String? = null,
    val tags: List<String>? = null,
    val app_version: String? = null,
    val github_issue_url: String? = null,
    val created_at: String? = null
)

data class FeedbackPhotoUploadResponse(
    val success: Boolean,
    val photos: List<FeedbackPhotoSaved>? = null,
    val count: Int = 0,
    val message: String? = null
)

data class FeedbackPhotoSaved(
    val id: Int,
    val fileName: String? = null
)

data class FeedbackPhotosResponse(
    val success: Boolean,
    val photos: List<FeedbackPhotoInfo> = emptyList()
)

data class FeedbackPhotoInfo(
    val id: Int,
    val feedbackId: Int,
    val contentType: String? = null,
    val fileName: String? = null,
    val size: Int = 0,
    val url: String? = null
)
