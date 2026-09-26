package com.example.woldeokmoneyverse.ui.viewmodel

import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject

/** Normalizes community/content responses to the actual backend contract. */
class CommunityRepository {
    private val legacy = com.example.woldeokmoneyverse.data.repository.CommunityRepository()

    suspend fun getBoardPosts(): Result<List<BoardPostDto>> = legacy.getBoardPosts()
    suspend fun getPostDetail(postId: String): Result<BoardPostDto> = legacy.getPostDetail(postId)
    suspend fun updatePost(postId: String, req: UpdatePostRequest): Result<BoardPostDto> = legacy.updatePost(postId, req)
    suspend fun deletePost(postId: String): Result<AuthResponse> = legacy.deletePost(postId)
    suspend fun getComments(postId: String): Result<List<BoardCommentDto>> = legacy.getComments(postId)
    suspend fun deleteComment(postId: String, commentId: String): Result<AuthResponse> = legacy.deleteComment(postId, commentId)
    suspend fun getMyProfile(): Result<UserProfileDto> = legacy.getMyProfile()
    suspend fun updateMyProfile(displayName: String): Result<UserProfileDto> = legacy.updateMyProfile(displayName)

    suspend fun getGalleryPhotos(): Result<List<PhotoDto>> = runCatching {
        val response = ApiClient.api.getGalleryPhotos()
        if (!response.isSuccessful || response.body() == null) {
            throw Exception(response.code().toString())
        }
        response.body()!!.photos.map { photo ->
            photo.copy(imageUrl = absoluteMediaUrl(photo.imageUrl))
        }
    }

    suspend fun getMyPhotos(): Result<List<PhotoSubmissionDto>> = legacy.getMyPhotos()
    suspend fun submitMemberPhoto(imageBytes: ByteArray, mimeType: String, altText: String): Result<AuthResponse> =
        legacy.submitMemberPhoto(imageBytes, mimeType, altText)
    suspend fun requestPrivacyData(type: String): Result<PrivacyRequestDto> = legacy.requestPrivacyData(type)

    suspend fun getAnnouncements(): Result<List<AnnouncementDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/content/announcements")
        if (!res.isSuccessful || res.body() == null) throw Exception(res.code().toString())
        res.body()!!.asJsonObject.getAsJsonArray("announcements")?.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            AnnouncementDto(
                id = item.string("id", "announcementId", "announcement_id").orEmpty(),
                title = item.string("title") ?: "공지",
                content = item.string("content", "body") ?: "",
                isImportant = item.boolean("isImportant", "is_important", "pinned") ?: false,
                createdAt = item.string("createdAt", "created_at", "publishedAt", "published_at").orEmpty()
            )
        }.orEmpty()
    }

    suspend fun getServiceStatus(): Result<ServiceStatusDto> = legacy.getServiceStatus().map { status ->
        status.copy(status = status.status.uppercase())
    }

    suspend fun createPost(req: CreatePostRequest): Result<BoardPostDto> = runCatching {
        val body = JsonObject().apply {
            addProperty("title", req.title)
            addProperty("body", req.content)
            addProperty("idempotencyKey", req.idempotencyKey)
        }
        val res = ApiClient.api.contractPost("app-api/v1/board/posts", body)
        if (!res.isSuccessful || res.body() == null) throw Exception(res.code().toString())
        val post = res.body()!!.asJsonObject.getAsJsonObject("post") ?: JsonObject()
        BoardPostDto(
            id = post.string("postId", "post_id", "id").orEmpty(),
            authorId = post.string("authorId", "author_id").orEmpty(),
            authorName = post.string("authorName", "author_name") ?: "나",
            title = post.string("title") ?: req.title,
            content = post.string("body", "content") ?: req.content,
            commentCount = post.int("commentCount", "comment_count") ?: 0,
            createdAt = post.string("createdAt", "created_at").orEmpty()
        )
    }

    suspend fun addComment(postId: String, req: AddCommentRequest): Result<CommentDto> = runCatching {
        val body = JsonObject().apply {
            addProperty("body", req.content)
            addProperty("idempotencyKey", req.idempotencyKey)
        }
        val res = ApiClient.api.contractPost("app-api/v1/board/posts/$postId/comments", body)
        if (!res.isSuccessful || res.body() == null) throw Exception(res.code().toString())
        val comment = res.body()!!.asJsonObject.getAsJsonObject("comment") ?: JsonObject()
        CommentDto(
            id = comment.string("commentId", "comment_id", "id").orEmpty(),
            authorName = comment.string("authorName", "author_name") ?: "나",
            content = comment.string("body", "content") ?: req.content,
            createdAt = comment.string("createdAt", "created_at").orEmpty()
        )
    }

    private fun absoluteMediaUrl(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return ""
        if (trimmed.startsWith("https://", ignoreCase = true)) return trimmed
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return trimmed.replaceFirst("http://", "https://", ignoreCase = true)
        }
        if (trimmed.startsWith("//")) return "https:$trimmed"
        return ApiClient.BASE_URL.trimEnd('/') + "/" + trimmed.trimStart('/')
    }

    private fun JsonObject.string(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asString }.getOrNull() }
    }
    private fun JsonObject.int(vararg names: String): Int? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asInt }.getOrNull() }
    }
    private fun JsonObject.boolean(vararg names: String): Boolean? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asBoolean }.getOrNull() }
    }
}
