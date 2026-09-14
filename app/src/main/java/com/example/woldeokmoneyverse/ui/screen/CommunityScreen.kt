package com.example.woldeokmoneyverse.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.BoardPostDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.data.model.UserProfileDto
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityViewModel: CommunityViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("게시판", "갤러리")

    val postsState by communityViewModel.postsState.collectAsState()
    val profileState by communityViewModel.profileState.collectAsState()
    val photosState by communityViewModel.photosState.collectAsState()
    val myPhotosState by communityViewModel.myPhotosState.collectAsState()
    val statusState by communityViewModel.statusState.collectAsState()
    val communityMessage by communityViewModel.communityMessage.collectAsState()

    LaunchedEffect(communityMessage) {
        communityMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            communityViewModel.clearCommunityMessage()
        }
    }

    var showNewPostDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUploadPhotoDialog by remember { mutableStateOf(false) }
    var selectedPostForDetail by remember { mutableStateOf<BoardPostDto?>(null) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedPhotoUri = uri
        showUploadPhotoDialog = uri != null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Service Status Banner ---
        when (val sState = statusState) {
            is UiState.Success -> {
                TopServiceStatusBanner(status = sState.data.status, notice = sState.data.notice)
            }
            else -> {}
        }

        MoneyverseSubTabRow(
            tabs = subTabs,
            selectedTabIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it }
        )

        if (selectedSubTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // --- My Profile Summary Card with Dynamic Server Data ---
                item {
                    Text("💬 커뮤니티 & 프로필", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))

                    when (val pState = profileState) {
                        is UiState.Success -> {
                            val p = pState.data
                            MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("내 프로필: ${p.displayName}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("칭호: ${p.title} (Lv.${p.level})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        Text("자기소개: ${p.bio ?: "소개가 없습니다."}", style = MaterialTheme.typography.bodySmall)
                                    Text("이메일: ${p.email} | 가입일: ${p.joinedAt}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MoneyverseSecondaryButton(
                                        text = "프로필 수정",
                                        onClick = { showEditProfileDialog = true }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { communityViewModel.requestPrivacyData("EXPORT") }) {
                                        Text("내 데이터 받기")
                                    }
                                    OutlinedButton(onClick = { communityViewModel.requestPrivacyData("DELETE") }) {
                                        Text("데이터 삭제 요청")
                                    }
                                }
                            }

                            if (showEditProfileDialog) {
                                EditProfileDialog(
                                    currentProfile = p,
                                    onDismiss = { showEditProfileDialog = false },
                                    onConfirm = { name ->
                                        communityViewModel.updateProfile(name)
                                        showEditProfileDialog = false
                                    }
                                )
                            }
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋 머니버스 게시판 (클릭 시 상세/댓글)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        MoneyverseButton(
                            text = "글쓰기",
                            onClick = { showNewPostDialog = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- Board Posts List ---
                when (val pState = postsState) {
                    is UiState.Success -> {
                        items(pState.data) { post ->
                            MoneyverseCard(onClick = { selectedPostForDetail = post }) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(post.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("💬 ${post.comments.size}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(post.content, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("작성자: ${post.authorName} • ${post.createdAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    is UiState.Loading -> item { SkeletonLoader() }
                    else -> {}
                }
            }
        } else {
            // --- Gallery Photos Grid & Member Upload ---
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🖼️ 월덕 머니버스 공개 갤러리", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    MoneyverseButton(
                        text = "사진 업로드",
                        onClick = { photoPicker.launch("image/*") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (myPhotosState is UiState.Success) {
                    val myPhotos = (myPhotosState as UiState.Success).data
                    if (myPhotos.isNotEmpty()) {
                        Text("내가 올린 사진 ${myPhotos.size}장", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                when (val photoState = photosState) {
                    is UiState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(photoState.data) { photo ->
                                MoneyverseCard {
                                    Text(photo.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("카테고리: ${photo.category}", style = MaterialTheme.typography.bodySmall)
                                    Text("❤️ 좋아요 ${photo.likes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                    is UiState.Loading -> SkeletonLoader()
                    else -> {}
                }
            }
        }
    }

    if (showNewPostDialog) {
        CreatePostDialog(
            onDismiss = { showNewPostDialog = false },
            onConfirm = { title, content ->
                communityViewModel.createPost(title, content)
                showNewPostDialog = false
            }
        )
    }

    if (selectedPhotoUri != null && showUploadPhotoDialog) {
        UploadPhotoDialog(
            onDismiss = { showUploadPhotoDialog = false; selectedPhotoUri = null },
            onConfirm = { caption ->
                val uri = selectedPhotoUri ?: return@UploadPhotoDialog
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    communityViewModel.uploadPhoto(bytes, context.contentResolver.getType(uri) ?: "image/*", caption)
                }
                selectedPhotoUri = null
                showUploadPhotoDialog = false
            }
        )
    }

    selectedPostForDetail?.let { post ->
        PostDetailSheet(
            post = post,
            onDismiss = { selectedPostForDetail = null },
            onAddComment = { content ->
                communityViewModel.addComment(post.id, content)
                post.comments.add(com.example.woldeokmoneyverse.data.model.CommentDto("c_new", "wtrdd", content, "방금 전"))
            }
        )
    }
}

@Composable
fun UploadPhotoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var caption by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🖼️ 갤러리 사진 업로드 (`POST /photos/uploads`)") },
        text = {
            Column {
                Text("월덕 머니버스 공개 갤러리에 공유할 사진 설명(캡션)을 입력하세요.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("사진 제목 및 설명") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = "업로드 등록",
                onClick = { onConfirm(caption.ifEmpty { "월덕 머니버스 사진" }) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfileDto,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.displayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로필 정보 수정") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("닉네임") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            MoneyverseButton(text = "저장", onClick = { onConfirm(name) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 게시글 작성") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("내용") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            MoneyverseButton(text = "등록", onClick = { onConfirm(title, content) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
