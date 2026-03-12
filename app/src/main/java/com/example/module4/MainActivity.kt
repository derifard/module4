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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.module4.ui.theme.Module4Theme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


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
                            Task3Screen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Task3Screen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<GithubRepo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                searchJob?.cancel()
                searchJob = scope.launch {
                    delay(500)
                    isLoading = true
                    searchResults = searchRepositories(context, query)
                    isLoading = false
                }
            },
            label = { Text("Поиск репозиториев") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        isLoading = true
                        searchResults = searchRepositories(context, searchQuery)
                        isLoading = false
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

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
            items(searchResults) { repo ->
                RepoItem(repo = repo)
            }
        }
    }
}

@Composable
fun RepoItem(repo: GithubRepo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = repo.full_name,
                style = MaterialTheme.typography.titleMedium
            )
            if (!repo.description.isNullOrBlank()) {
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Язык: ${repo.language ?: "не указан"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${repo.stargazers_count}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

suspend fun searchRepositories(context: Context, query: String): List<GithubRepo> {
    if (query.isBlank()) return emptyList()

    return withContext(Dispatchers.IO) {
        try {
            delay(800)
            val jsonString = context.resources.openRawResource(R.raw.github_repos)
                .bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<GithubRepo>>() {}.type
            val allRepos: List<GithubRepo> = Gson().fromJson(jsonString, listType)
            val filtered = allRepos.filter { repo ->
                repo.full_name.contains(query, ignoreCase = true) ||
                        (repo.description?.contains(query, ignoreCase = true) == true)
            }
            filtered
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Serializable
data class GithubRepo(
    val id: Int,
    val full_name: String,
    val description: String?,
    val stargazers_count: Int,
    val language: String?
)
