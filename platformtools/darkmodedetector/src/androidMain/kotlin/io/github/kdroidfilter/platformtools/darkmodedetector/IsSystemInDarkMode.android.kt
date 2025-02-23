package io.github.kdroidfilter.platformtools.darkmodedetector

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun isSystemInDarkMode(): Boolean = isSystemInDarkTheme()