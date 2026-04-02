package kr.ac.kaist.aailab.braillemate.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = SurfaceLight,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Gold40,
    onSecondary = Gold10,
    secondaryContainer = Gold90,
    onSecondaryContainer = Gold10,
    tertiary = Green40,
    tertiaryContainer = Green90,
    error = Red40,
    errorContainer = Red90,
    background = Neutral99,
    onBackground = Neutral10,
    surface = SurfaceLight,
    onSurface = Neutral10,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Neutral30,
    outline = Neutral50
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue50,
    onPrimary = Blue10,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Gold50,
    onSecondary = Gold10,
    secondaryContainer = Gold30,
    onSecondaryContainer = Gold90,
    tertiary = Green80,
    tertiaryContainer = Green40,
    error = Red80,
    errorContainer = Red40,
    background = Neutral10,
    onBackground = Neutral90,
    surface = SurfaceDark,
    onSurface = Neutral90,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Neutral80,
    outline = Neutral50
)

@Composable
fun BrailleMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
