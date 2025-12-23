package com.example.bai10

// Data class: Cấu trúc gọn nhẹ, tự động sinh ra getter/setter/toString
data class User(
    val id: Int = -1,
    val username: String,
    val password: String
)

data class Task(
    val id: Int = -1,
    val userId: Int,
    val content: String,
    var isCompleted: Boolean = false
)