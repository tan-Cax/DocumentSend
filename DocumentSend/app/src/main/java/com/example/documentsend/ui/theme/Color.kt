package com.example.documentsend.ui.theme

import androidx.compose.ui.graphics.Color

// ========== 浅色：背景/表面/卡片 ==========

val LightBackground         = Color(0xFFFFFFFF)
val LightSurface            = Color(0xFFFFFFFF)
val LightCardBackground     = Color(0xFFF5F5F5)
val LightSessionListBg      = Color(0xFFFAFAFA)
val LightInteractiveSurface = Color(0xFFF0F0F0)

// ========== 深色：背景/表面/卡片 ==========

val DarkBackground          = Color(0xFF121212)
val DarkSurface             = Color(0xFF1E1E1E)
val DarkCardBackground      = Color(0xFF2C2C2C)
val DarkSessionListBg       = Color(0xFF1A1A1A)
val DarkInteractiveSurface  = Color(0xFF333333)

// ========== 浅色：文字 ==========

val LightOnBackground       = Color(0xFF000000)
val LightOnSurface          = Color(0xFF000000)
val LightSecondaryText      = Color(0xFF808080)
val LightTertiaryText       = Color(0xFFA9A9A9)

// ========== 深色：文字 ==========

val DarkOnBackground        = Color(0xFFE0E0E0)
val DarkOnSurface           = Color(0xFFE0E0E0)
val DarkSecondaryText       = Color(0xFFAAAAAA)
val DarkTertiaryText        = Color(0xFF888888)

// ========== 浅色：状态/交互 ==========

val LightSuccess            = Color(0xFF388E3C)
val LightError              = Color(0xFFD32F2F)
val LightSelectedHighlight  = Color(0xFFE3F2FD)
val LightUnselectedBorder   = Color(0xFFD3D3D3)
val LightUnselectedIcon     = Color(0xFFA9A9A9)
val LightSwitchOn           = Color(0xFF4CAF50)
val LightSwitchOff          = Color(0xFFBDBDBD)
val LightDivider            = Color(0xFFE0E0E0)
val LightWarningText        = Color(0xFFFF0000)

// ========== 深色：状态/交互 ==========

val DarkSuccess             = Color(0xFF66BB6A)
val DarkError               = Color(0xFFEF5350)
val DarkSelectedHighlight   = Color(0xFF1A3A5A)
val DarkUnselectedBorder    = Color(0xFF444444)
val DarkUnselectedIcon      = Color(0xFF888888)
val DarkSwitchOn            = Color(0xFF66BB6A)
val DarkSwitchOff           = Color(0xFF555555)
val DarkDivider             = Color(0xFF333333)
val DarkWarningText         = Color(0xFFFF6666)

// ========== 浅色-默认（蓝色）==========

val BluePrimary       = Color(0xFF2196F3)
val BlueBrand         = Color(0xFF4169E1)
val BlueAccent        = Color(0xFF0000FF)
val BlueLightAccent   = Color(0xFFADD8E6)
val BlueDarkAccent    = Color(0xFF00008B)

// ========== 浅色-红色 ==========

val RedPrimary        = Color(0xFFF44336)
val RedBrand          = Color(0xFFD32F2F)
val RedAccent         = Color(0xFFCC0000)
val RedLightAccent    = Color(0xFFF8BBD0)
val RedDarkAccent     = Color(0xFF8B0000)

// ========== 浅色-绿色 ==========

val GreenPrimary      = Color(0xFF4CAF50)
val GreenBrand        = Color(0xFF388E3C)
val GreenAccent       = Color(0xFF008000)
val GreenLightAccent  = Color(0xFFC8E6C9)
val GreenDarkAccent   = Color(0xFF006400)

// ========== 夜晚模式固定主题色（蓝色）==========

val DarkPrimary       = Color(0xFF90CAF9)   // 蓝200
val DarkBrand         = Color(0xFF6B8FE6)   // 蓝紫
val DarkAccent        = Color(0xFF6666FF)   // 中蓝
val DarkLightAccent   = Color(0xFF3A5A8A)   // 暗蓝
val DarkDarkAccent    = Color(0xFF5555AA)   // 暗紫蓝

// ========== 夜晚模式-红色 ==========

val DarkRedPrimary       = Color(0xFFEF9A9A)   // 红200
val DarkRedBrand         = Color(0xFFE57373)   // 红300
val DarkRedAccent        = Color(0xFFFF5252)   // 红A200
val DarkRedLightAccent   = Color(0xFF4A1515)   // 暗红背景
val DarkRedDarkAccent    = Color(0xFFC62828)   // 红800

// ========== 夜晚模式-绿色 ==========

val DarkGreenPrimary       = Color(0xFFA5D6A7)   // 绿200
val DarkGreenBrand         = Color(0xFF81C784)   // 绿300
val DarkGreenAccent        = Color(0xFF69F0AE)   // 绿A200
val DarkGreenLightAccent   = Color(0xFF1B3A1B)   // 暗绿背景
val DarkGreenDarkAccent    = Color(0xFF2E7D32)   // 绿800

// ========== 通用：白色/黑色文字 ==========

val OnPrimaryWhite    = Color(0xFFFFFFFF)
val OnPrimaryBlack    = Color(0xFF000000)

// ========== 日志专用（不随主题变化）==========

val LogDebug          = Color(0xFF0000FF)
val LogWarning        = Color(0xFFFFFF00)
val LogInfo           = Color(0xFF008000)
val LogError          = Color(0xFFFFFF00)
val LogDefault        = Color(0xFF808080)

// ========== 补充用色（无对应主题色）==========

val BlueAccentVariant  = Color(0xFF1976D2)   // 蓝700，图标强调
val GreenVeryLight     = Color(0xFFF1F8E9)   // 绿50，接收图标背景
val AmberAccent        = Color(0xFFFFA000)   // 琥珀色，警告/续传用

// ========== 简名别名（兼容旧引用，供 UI 文件直接使用）==========

val white             = OnPrimaryWhite         // 0xFFFFFFFF
val black             = OnPrimaryBlack         // 0xFF000000
val blue              = Color(0xFF0000FF)      // 纯蓝
val green             = Color(0xFF008000)      // 纯绿
val red               = Color(0xFFFF0000)      // 纯红
val yellow            = Color(0xFFFFFF00)      // 纯黄
val gray              = Color(0xFF808080)      // 灰色
val light_gray        = Color(0xFFD3D3D3)      // 浅灰
val very_light_gray   = Color(0xFFF5F5F5)      // 极浅灰
val dark_gray         = Color(0xFF444444)      // 深灰
val dark_blue         = Color(0xFF00008B)      // 深蓝
val royal_blue        = Color(0xFF4169E1)      // 皇家蓝
val light_blue        = Color(0xFFADD8E6)      // 浅蓝
