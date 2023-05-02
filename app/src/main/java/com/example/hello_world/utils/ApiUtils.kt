package com.example.hello_world

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException



suspend fun <T> withExponentialBackoff(
    context: Context,
    snackbarHostState: SnackbarHostState,
    apiRequest: suspend () -> T,
    coroutineScope: CoroutineScope,
    onRetry: suspend () -> Unit = {}
): T? {
    var result: T? = null
    var currentDelay = 1000L // Initial delay
    val maxRetries = 3
    var lastErrorMessage: String? = null

    for (retryCount in 0 until maxRetries) {
        try {
            result = apiRequest()
            break
        } catch (e: IOException) {
            lastErrorMessage = e.message
            if (retryCount < maxRetries - 1) {
                coroutineScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Network error, retrying... (${retryCount + 1})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                delay(currentDelay)
                currentDelay *= 2
            } else {
                Log.d("log: withExponentialBackoff", "Network error: $lastErrorMessage")
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = "Network error: $lastErrorMessage",
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Indefinite
                )
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    onRetry()
                }
            }
        }
    }
    return result
}
