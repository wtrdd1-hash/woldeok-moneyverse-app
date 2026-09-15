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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.woldeokmoneyverse.data.model.BoardPostDto
import com.example.woldeokmoneyverse.data.model.PhotoDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.data.model.UserProfileDto
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.BoardComposerViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.CommunityViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.LobbyChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityViewModel: CommunityViewModel,
    boardComposerViewModel: BoardComposerViewModel = viewModel(),
    lobbyChatViewModel: LobbyChatViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("게시판", "갤러리", "실시간 채팅")

    val postsState by communityViewModel.postsState.collectAsState()
    val profileState by communityViewModel.profileState.collectAsState()
    val photosState by communityViewModel.photosState.collectAsState()
    val myPhotosState by communityViewModel.myPhotosState.collectAsState()
    val statusState by communityViewModel.statusState.collectAsState()
    val communityMessage by communityViewModel.communityMessage.collectAsState()
    val composerMessage by boardComposerViewModel.message.collectAsState()
    val composerBusy by boardComposerViewModel.busy.collectAsState()

    LaunchedEffect(communityMessage) {
        communityMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            communityViewModel.clearCommunityMessage()
        }
    }
    LaunchedEffect(composerMessage) {
        composerMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            boardComposerViewModel.clearMessage()
        }
    }

    var showNewPostDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUploadPhotoDialog by remember { mutableStateOf(false) }
    var selectedPostForDetail by remember { mutableStateOf<BoardPostDto?>(null) }
    var selectedPhotoForDetail by remember { mutableStateOf<PhotoDto?>(null) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPostImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedPhotoUri = uri
        showUploadPhotoDialog = uri != null
    }
    val postImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedPostImageUri = uri
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (val sState = statusState) {
            is UiState.Success -> TopServiceStatusBanner(status = sState.data.status, notice = sState.data.notice)
            else -> Unit
        }

        MoneyverseSubTabRow(
            tabs = subTabs,
            selectedTabIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it }
        )

        if (selectedSubTab == 0) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
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
                                        Text("EXP ${p.experience} / ${p.nextLevelExperience} · ${p.jobType ?: "직업 미선택"}", style = MaterialTheme.typography.bodySmall)
                                        Text("자기소개: ${p.bio ?: "소개가 없습니다."}", style = MaterialTheme.typography.bodySmall)
                                        Text("이메일: ${p.email} | 가입일: ${p.joinedAt}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MoneyverseSecondaryButton(text = "프로필 수정", onClick = { showEditProfileDialog = true })
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
                        else -> Unit
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋 머니버스 게시판", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        MoneyverseButton(
                            text = "글쓰기",
                            onClick = {
                                selectedPostImageUri = null
                                showNewPostDialog = true
                            }
                        )
                    }
                    Text("게시글 카드를 누르면 상세 페이지가 열립니다.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (val pState = postsState) {
                    is UiState.Success -> {
                        items(pState.data) { post ->
                            MoneyverseCard(onClick = { selectedPostForDetail = post }) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(post.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("💬 ${post.commentCount.coerceAtLeast(post.comments.size)}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(post.content, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("작성자: ${post.authorName} • ${post.createdAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("상세 보기 ›", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    is UiState.Loading -> item { SkeletonLoader() }
                    is UiState.Error -> item { ErrorBanner(message = pState.message, onRetry = { communityViewModel.loadCommunityData() }) }
                    else -> Unit
                }
            }
        } else if (selectedSubTab == 1) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🖼️ 월덕 머니버스 공개 갤러리", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    MoneyverseButton(text = "사진 업로드", onClick = { photoPicker.launch("image/*") })
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(photoState.data) { photo ->
                                MoneyverseCard(onClick = { selectedPhotoForDetail = photo }) {
                                    if (photo.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = photo.imageUrl,
                                            contentDescription = photo.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Text(photo.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 2)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${photo.category} · ❤️ ${photo.likes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("사진 상세 보기 ›", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    is UiState.Loading -> SkeletonLoader()
                    is UiState.Error -> ErrorBanner(message = photoState.message, onRetry = { communityViewModel.loadCommunityData() })
                    else -> Unit
                }
            }
        } else {
            LobbyChatPanel(lobbyChatViewModel)
        }
    }

    if (showNewPostDialog) {
        CreatePostDialog(
            hasImage = selectedPostImageUri != null,
            busy = composerBusy,
            onPickImage = { postImagePicker.launch("image/*") },
            onRemoveImage = { selectedPostImageUri = null },
            onDismiss = {
                if (!composerBusy) {
                    showNewPostDialog = false
                    selectedPostImageUri = null
                }
            },
            onConfirm = { title, content ->
                val uri = selectedPostImageUri
                val bytes = uri?.let { context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() } }
                val mime = uri?.let { context.contentResolver.getType(it) }
                boardComposerViewModel.createPost(title, content, bytes, mime) {
                    showNewPostDialog = false
                    selectedPostImageUri = null
                    communityViewModel.loadCommunityData()
                }
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
                post.comments.add(com.example.woldeokmoneyverse.data.model.CommentDto("c_new", "나", content, "방금 전"))
            }
        )
    }

    selectedPhotoForDetail?.let { photo ->
        PhotoDetailSheet(photo = photo, onDismiss = { selectedPhotoForDetail = null })
    }
}


@Composable
private fun LobbyChatPanel(viewModel: LobbyChatViewModel) {
    val state by viewModel.state.collectAsState()
    var draft by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.connect() }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("💬 실시간 로비", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("${state.online}명 참여 중 · 메시지는 서버에 저장하지 않습니다.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.messages.isEmpty()) item { Text("아직 대화가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.messages) { msg ->
                Text("${msg.sender}  ${msg.text}", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(180) },
                enabled = state.connected && state.canChat,
                modifier = Modifier.weight(1f),
                label = { Text(if (state.canChat) "메시지" else "로그인/정책 동의 필요") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { viewModel.send(draft); draft = "" }, enabled = state.connected && state.canChat && draft.isNotBlank()) { Text("전송") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailSheet(photo: PhotoDto, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(photo.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            if (photo.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 520.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("카테고리: ${photo.category}", style = MaterialTheme.typography.bodyMedium)
            Text("좋아요 ${photo.likes}", style = MaterialTheme.typography.bodyMedium)
            photo.publishedAt?.let { Text("게시일: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("닫기") }
        }
    }
}

@Composable
fun UploadPhotoDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var caption by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🖼️ 갤러리 사진 업로드") },
        text = {
            Column {
                Text("월덕 머니버스 공개 갤러리에 공유할 사진 설명을 입력하세요.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("사진 제목 및 설명") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = { MoneyverseButton(text = "업로드 등록", onClick = { onConfirm(caption.ifEmpty { "월덕 머니버스 사진" }) }) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun EditProfileDialog(currentProfile: UserProfileDto, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentProfile.displayName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로필 정보 수정") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("닉네임") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { MoneyverseButton(text = "저장", onClick = { onConfirm(name) }) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun CreatePostDialog(
    hasImage: Boolean,
    busy: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
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
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth(), enabled = !busy)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("내용") }, modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = !busy)
                Spacer(modifier = Modifier.height(12.dp))
                if (hasImage) {
                    Text("✅ 게시판 이미지 1장이 선택되었습니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    TextButton(onClick = onRemoveImage, enabled = !busy) { Text("이미지 제거") }
                } else {
                    OutlinedButton(onClick = onPickImage, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("🖼️ 사진 첨부") }
                }
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = if (busy) "등록 중…" else "등록",
                onClick = { onConfirm(title, content) },
                enabled = !busy && title.isNotBlank() && content.isNotBlank()
            )
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("취소") } }
    )
}
