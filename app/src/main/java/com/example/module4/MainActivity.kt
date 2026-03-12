package com.example.module4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.module4.ui.theme.Module4Theme
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
                        Task2Screen()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Module4Theme {
        Greeting("Android")
    }
}@Composable
fun Task2Screen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var resultText by remember { mutableStateOf("Нажмите кнопку для поиска дубликатов") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    resultText = "Поиск дубликатов..."

                    val time = measureTimeMillis {
                        val duplicates = findDuplicateFilesWithTimeout(context)
                        resultText = buildString {
                            if (duplicates.isEmpty()) {
                                append("Дубликаты не найдены или поиск прерван по таймауту")
                            } else {
                                append("Найдены группы дубликатов:\n\n")
                                duplicates.forEachIndexed { index, group ->
                                    append("Группа ${index + 1}:\n")
                                    group.forEach { file ->
                                        append("  $file\n")
                                    }
                                    append("\n")
                                }
                            }
                        }
                    }
                    resultText += "\nВремя поиска: ${time / 1000.0} сек"
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Поиск..." else "Запустить задание 2")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = resultText,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            fontSize = 14.sp
        )
    }
}

suspend fun findDuplicateFilesWithTimeout(context: Context): List<List<String>> {
    val directory = File(context.filesDir, "testdata")
    return withTimeoutOrNull(5000) {
        findDuplicateFiles(directory)
    } ?: run {
        println("Поиск прерван по таймауту")
        emptyList()
    }
}

suspend fun findDuplicateFiles(directory: File): List<List<String>> = coroutineScope {
    val jsonFiles = directory.walk()
        .filter { it.isFile && it.extension == "json" }
        .toList()

    if (jsonFiles.isEmpty()) {
        return@coroutineScope emptyList()
    }

    val deferredResults = jsonFiles.map { file ->
        async {
            file.absolutePath to computeFileHash(file)
        }
    }

    val results = deferredResults.awaitAll()
    val hashToFiles = mutableMapOf<String, MutableList<String>>()

    results.forEach { (path, hash) ->
        if (hash != null) {
            hashToFiles.getOrPut(hash) { mutableListOf() }.add(path)
        }
    }

    hashToFiles.values
        .filter { it.size > 1 }
        .map { it.sorted() }
}

suspend fun computeFileHash(file: File): String? {
    return withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = file.readBytes()
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            println("Ошибка при обработке файла ${file.name}: ${e.message}")
            null
        }
    }
}
