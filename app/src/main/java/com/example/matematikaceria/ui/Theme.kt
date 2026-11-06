package com.example.matematikaceria.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF9FA8DA), secondary = Color(0xFF80CBC4))
    } else {
        lightColorScheme(primary = Color(0xFF3949AB), secondary = Color(0xFF00796B))
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
