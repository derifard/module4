package com.example.module4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.module4.ui.theme.Module4Theme
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis
import kotlin.random.Random

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
                    Task1Screen()
                }
            }
        }
    }
}

@Composable
fun Task1Screen() {
    val scope = rememberCoroutineScope()
    var resultText by remember { mutableStateOf("Нажмите кнопку для запуска задания 1") }
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
                    resultText = ""
                    val time = measureTimeMillis {
                        runBlocking {
                            val usersDeferred = async { loadUsers() }
                            val salesDeferred = async { loadSales() }
                            val weatherDeferred = async { loadWeather() }

                            val users = usersDeferred.await()
                            val sales = salesDeferred.await()
                            val weather = weatherDeferred.await()

                            resultText = buildString {
                                if (users == null || sales == null || weather == null) {
                                    append("Некоторые задачи завершились с ошибкой\n\n")
                                } else {
                                    append("Все задачи выполнены успешно\n\n")
                                }

                                if (users != null) {
                                    append("Пользователи (${users.size} чел.):\n")
                                    users.forEach { append("   $it\n") }
                                } else {
                                    append("Пользователи: ошибка загрузки\n")
                                }

                                append("\n")

                                if (sales != null) {
                                    append("Продажи:\n")
                                    sales.forEach { (product, quantity) ->
                                        append("   $product: $quantity шт.\n")
                                    }
                                } else {
                                    append("Продажи: ошибка загрузки\n")
                                }

                                append("\n")

                                if (weather != null) {
                                    append("Погода:\n")
                                    weather.forEach { append("   $it\n") }
                                } else {
                                    append("Погода: ошибка загрузки\n")
                                }
                            }
                        }
                    }
                    resultText += "\nВремя: ${time / 1000.0} сек"
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Загрузка..." else "Запустить задание 1")
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

suspend fun loadUsers(): List<String>? {
    return withContext(Dispatchers.IO) {
        println("Загрузка пользователей(${Thread.currentThread().name})")
        delay(1800)
        try {
            if (Random.nextInt(100) < 5) {
                throw RuntimeException("Ошибка загрузки пользователей")
            }
            listOf("Alice", "Bob", "Ivan", "Olga", "Maria", "Petr")
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }
}

suspend fun loadSales(): Map<String, Int>? {
    return withContext(Dispatchers.IO) {
        println("Загрузка продаж(${Thread.currentThread().name})")
        delay(1200)
        try {
            if (Random.nextInt(100) < 6) {
                throw RuntimeException("Ошибка загрузки продаж")
            }
            mapOf(
                "Кофе" to 42,
                "Чай" to 19,
                "Круассан" to 27,
                "Сэндвич" to 15
            )
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }
}

suspend fun loadWeather(): List<String>? {
    return withContext(Dispatchers.IO) {
        println("Загрузка погоды(${Thread.currentThread().name})")
        delay(2500)
        try {
            if (Random.nextInt(100) < 50) {
                throw RuntimeException("Ошибка загрузки погоды")
            }
            listOf(
                "Москва: -3°C",
                "Санкт-Петербург: -5°C",
                "Нью-Йорк: +5°C",
                "Токио: +11°C",
                "Лондон: +2°C"
            )
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }
}