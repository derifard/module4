package com.example.module4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class FactViewModel : ViewModel() {
    private val facts = listOf(
        "Слоны — единственные млекопитающие, которые не умеют прыгать.",
        "У кошек 32 мышцы в каждом ухе.",
        "Дельфины спят с одним открытым глазом.",
        "Ленивцы могут задерживать дыхание дольше, чем дельфины — до 40 минут.",
        "Глаза страуса больше, чем его мозг.",
        "Белые медведи — левши.",
        "Собаки различают до 250 слов и жестов.",
        "Крысы смеются, когда их щекочут.",
        "Осьминоги имеют три сердца.",
        "Колибри — единственные птицы, которые могут летать назад.",
        "Жирафы спят всего 30 минут в день.",
        "Муравьи никогда не спят.",
        "Лошади не могут дышать ртом.",
        "У улиток около 25 000 зубов.",
        "Панды едят до 12 часов в день."
    )

    private val _fact = MutableStateFlow("")
    val fact: StateFlow<String> = _fact.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadRandomFact() {
        viewModelScope.launch {
            _isLoading.value = true
            _fact.value = ""
            delay((1500..3000).random().toLong())
            _fact.value = facts[Random.nextInt(facts.size)]
            _isLoading.value = false
        }
    }
}