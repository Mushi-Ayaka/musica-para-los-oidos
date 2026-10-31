package com.musica.paralosoidos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MusicaStyle {
    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 10.dp
        val xl = 12.dp
        val xxl = 14.dp
        val xxxl = 16.dp
        val huge = 24.dp
        val massive = 32.dp
    }

    object Shape {
        val micro = RoundedCornerShape(6.dp)
        val tiny = RoundedCornerShape(8.dp)
        val small = RoundedCornerShape(10.dp)
        val medium = RoundedCornerShape(12.dp)
        val row = RoundedCornerShape(14.dp)
        val card = RoundedCornerShape(16.dp)
        val large = RoundedCornerShape(20.dp)
        val sheet = RoundedCornerShape(28.dp)
        val pill = RoundedCornerShape(999.dp)
    }

    object Size {
        val iconXs = 14.dp
        val iconSm = 16.dp
        val iconMd = 20.dp
        val iconLg = 24.dp
        val iconXl = 28.dp
        val artworkSm = 44.dp
        val artworkMd = 54.dp
        val artworkLg = 64.dp
        val miniPlayer = 72.dp
        val seekBar = 4.dp
        val seekThumb = 16.dp
        val eqBarWidth = 10.dp
        val eqBarMin = 6.dp
        val eqBarMax = 90.dp
    }

    object TextSize {
        val micro = 9.sp
        val caption = 10.sp
        val label = 11.sp
        val small = 12.sp
        val body = 13.sp
        val bodyLarge = 14.sp
        val title = 16.sp
        val titleLarge = 20.sp
        val display = 28.sp
    }

    object Alpha {
        const val disabled = 0.38f
        const val divider = 0.05f
        const val border = 0.08f
        const val borderStrong = 0.10f
        const val subtle = 0.15f
        const val overlay = 0.20f
        const val medium = 0.35f
        const val scrim = 0.80f
    }

    object Colors {
        val border = Color(0x14FFFFFF)
        val borderStrong = Color(0x1AFFFFFF)
        val divider = Color(0x0DFFFFFF)
        val overlay = Color(0x33FFFFFF)
        val overlaySoft = Color(0x26FFFFFF)
        val overlayMedium = Color(0x38FFFFFF)
        val scrim = Color(0xCC000000)
        val menuBackground = Color(0xFF141024)
        val menuBackgroundAlt = Color(0xFF18142A)
        val infoSurface = Color(0xFF071E23)
        val gold = Color(0xFFF59E0B)
        val neutral = Color(0xFFB8B8C2)
        val neutralBorder = Color(0x33888888)
    }

    object Border {
        val hairline = 1.dp
        val thick = 2.dp
    }

    object Motion {
        const val fast = 120
        const val normal = 220
        const val slow = 400
        const val progressTick = 500L
    }

    object Wallpaper {
        val blur = 18.dp
        const val scrimAlpha = 0.45f
    }

    object Player {
        val miniHeight = 72.dp
        val fullArtwork = 280.dp
        val seekBarHeight = 4.dp
    }
}
