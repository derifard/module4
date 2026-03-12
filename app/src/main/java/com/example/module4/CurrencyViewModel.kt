package com.example.module4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class CurrencyViewModel : ViewModel() {
    private val _rate = MutableStateFlow(90.5)
    val rate: StateFlow<Double> = _rate.asStateFlow()

    private val _trend = MutableStateFlow(0)
    val trend: StateFlow<Int> = _trend.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                generateNewRate()
            }
        }
    }

    fun refresh() {
        generateNewRate()
    }

    private fun generateNewRate() {
        val oldRate = _rate.value
        val change = Random.nextDouble() * 4.0 - 2.0
        val newRate = oldRate + change
        _rate.value = newRate
        _trend.value = when {
            newRate > oldRate -> 1
            newRate < oldRate -> -1
            else -> 0
        }
    }
}