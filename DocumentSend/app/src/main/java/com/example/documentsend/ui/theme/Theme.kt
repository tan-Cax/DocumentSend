package com.example.documentsend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 浅色-默认（蓝色）
private val DefaultLightScheme = lightColorScheme(
    primary            = BluePrimary,
    onPrimary          = OnPrimaryWhite,
    background         = LightBackground,
    surface            = LightSurface,
    onBackground       = LightOnBackground,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightCardBackground,
    outline            = LightDivider,
)

// 浅色-红色
private val RedLightScheme = lightColorScheme(
    primary            = RedPrimary,
    onPrimary          = OnPrimaryWhite,
    background         = LightBackground,
    surface            = LightSurface,
    onBackground       = LightOnBackground,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightCardBackground,
    outline            = LightDivider,
)

// 浅色-绿色
private val GreenLightScheme = lightColorScheme(
    primary            = GreenPrimary,
    onPrimary          = OnPrimaryWhite,
    background         = LightBackground,
    surface            = LightSurface,
    onBackground       = LightOnBackground,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightCardBackground,
    outline            = LightDivider,
)

// 夜晚模式-蓝色（默认）
private val DarkScheme = darkColorScheme(
    primary            = DarkPrimary,
    onPrimary          = OnPrimaryBlack,
    background         = DarkBackground,
    surface            = DarkSurface,
    onBackground       = DarkOnBackground,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkCardBackground,
    outline            = DarkDivider,
)

// 夜晚模式-红色
private val RedDarkScheme = darkColorScheme(
    primary            = DarkRedPrimary,
    onPrimary          = OnPrimaryBlack,
    background         = DarkBackground,
    surface            = DarkSurface,
    onBackground       = DarkOnBackground,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkCardBackground,
    outline            = DarkDivider,
)

// 夜晚模式-绿色
private val GreenDarkScheme = darkColorScheme(
    primary            = DarkGreenPrimary,
    onPrimary          = OnPrimaryBlack,
    background         = DarkBackground,
    surface            = DarkSurface,
    onBackground       = DarkOnBackground,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkCardBackground,
    outline            = DarkDivider,
)

@Composable
fun DocumentSendTheme(
    themeMode: Int = 0,
    colorScheme: String = "默认",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val scheme = if (isDark) {
        when (colorScheme) {
            "红色" -> RedDarkScheme
            "绿色" -> GreenDarkScheme
            else  -> DarkScheme
        }
    } else {
        when (colorScheme) {
            "红色" -> RedLightScheme
            "绿色" -> GreenLightScheme
            else  -> DefaultLightScheme
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content
    )
}
