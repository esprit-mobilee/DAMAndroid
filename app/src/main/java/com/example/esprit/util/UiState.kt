package com.example.esprit.util


/**
 * 🔹 Generic sealed class to represent loading / success / error states.
 * Can be reused across all repositories & ViewModels.
 *
 * Example usage:
 *   UiState.Loading
 *   UiState.Success(data)
 *   UiState.Error("Something went wrong")
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
