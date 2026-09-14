package com.example.woldeokmoneyverse.ui.viewmodel

import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject

/** Normalizes community/content responses to the actual backend contract. */
class CommunityRepository {
    private val legacy = com.example.woldeokmoneyverse.data.repository.CommunityRepository()

    suspend fun getBoardPosts(): Result<List<BoardPostDto>> = legacy.getBoardPosts()
    suspend fun getMyProfile(): Result<UserProfileDto> = legacy.getMyProfile()
    suspend fun updateMyProfile(displayName: String): Result<UserProfileDto> = legacy.updateMyProfile(displayName)
    suspend fun getGalleryPhotos(): Result<List<PhotoDto>> = legacy.getGalleryPhotos()
    suspend fun getMyPhotos(): Result<List<PhotoSubmissionDto>> = legacy.getMyPhotos()
    suspend fun submitMemberPhoto(imageBytes: ByteArray, mimeType: String, altText: String): Result<AuthResponse> =
        legacy.submitMemberPhoto(imageBytes, mimeType, altText)
    suspend fun requestPrivacyData(type: String): Result<PrivacyRequestDto> = legacy.requestPrivacyData(type)

    suspend fun getAnnouncements(): Result<List<AnnouncementDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/content/announcements")
        if (!res.isSuccessful || res.body() == null) throw Exception("공지사항 조회 실패 (HTTP ${res.code()})")
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
        if (!res.isSuccessful || res.body() == null) {
            throw Exception("게시글 등록 실패 (HTTP ${res.code()}): ${res.errorBody()?.string().orEmpty()}")
        }
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
        if (!res.isSuccessful || res.body() == null) {
            throw Exception("댓글 등록 실패 (HTTP ${res.code()}): ${res.errorBody()?.string().orEmpty()}")
        }
        val comment = res.body()!!.asJsonObject.getAsJsonObject("comment") ?: JsonObject()
        CommentDto(
            id = comment.string("commentId", "comment_id", "id").orEmpty(),
            authorName = comment.string("authorName", "author_name") ?: "나",
            content = comment.string("body", "content") ?: req.content,
            createdAt = comment.string("createdAt", "created_at").orEmpty()
        )
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
