package com.example.module4

data class City(
    val name: String,
    var temperature: Int? = null,
    var status: String = "pending"
)