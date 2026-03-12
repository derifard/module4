package com.example.module4

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.module4.ui.theme.Module4Theme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {

                        Box(modifier = Modifier.weight(1f)) {
                            Task4Screen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Task4Screen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<PostWithData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var loadJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                loadJob?.cancel()
                loadJob = scope.launch {
                    isLoading = true
                    posts = loadPostsWithData(context)
                    isLoading = false
                }
            }
        ) {
            Text("Загрузить ленту")
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(posts) { postWithData ->
                PostCard(postWithData = postWithData)
            }
        }
    }
}

@Composable
fun PostCard(postWithData: PostWithData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(postWithData.post.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_menu_report_image)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = postWithData.post.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = postWithData.post.body,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (postWithData.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else if (postWithData.error != null) {
                Text(
                    text = "Ошибка загрузки комментариев: ${postWithData.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Комментарии (${postWithData.comments.size}):",
                    style = MaterialTheme.typography.titleSmall
                )
                postWithData.comments.take(2).forEach { comment ->
                    Text(
                        text = "${comment.name}: ${comment.body}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                if (postWithData.comments.size > 2) {
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

suspend fun loadPostsWithData(context: Context): List<PostWithData> = supervisorScope {
    val posts = loadPosts(context)

    posts.map { post ->
        async {
            try {
                val comments = loadCommentsForPost(context, post.id)
                PostWithData(
                    post = post,
                    comments = comments,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                PostWithData(
                    post = post,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }.awaitAll()
}

suspend fun loadPosts(context: Context): List<Post> {
    return withContext(Dispatchers.IO) {
        try {
            delay(1000)
            val jsonString = context.resources.openRawResource(R.raw.posts)
                .bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Post>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            println("Ошибка загрузки постов: ${e.message}")
            emptyList()
        }
    }
}

suspend fun loadCommentsForPost(context: Context, postId: Int): List<Comment> {
    return withContext(Dispatchers.IO) {
        try {
            delay(1500)
            val jsonString = context.resources.openRawResource(R.raw.comments)
                .bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Comment>>() {}.type
            val allComments: List<Comment> = Gson().fromJson(jsonString, listType)
            allComments.filter { it.postId == postId }
        } catch (e: Exception) {
            println("Ошибка загрузки комментариев: ${e.message}")
            emptyList()
        }
    }
}

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val avatarUrl: String
)

data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val body: String
)

data class PostWithData(
    val post: Post,
    var comments: List<Comment> = emptyList(),
    var isLoading: Boolean = true,
    var error: String? = null
)