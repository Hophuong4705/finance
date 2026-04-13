package com.example.cuoikyltdd.data.remote.dto

data class RegisterRequest(
    val name:     String,
    val email:    String,
    val phone:    String,
    val password: String
)

data class LoginRequest(
    val email:    String,
    val password: String
)

data class LoginResponse(
    val success: Boolean = true,
    val token:   String? = null,
    val userId:  String? = null,
    val name:    String? = null,
    val error:   String? = null
)