package com.musica.paralosoidos.ui.theme

import com.musica.paralosoidos.data.ThemeSettingsState

data class ThemePreset(
    val id: String,
    val name: String,
    val description: String,
    val settings: ThemeSettingsState
)

data class WallpaperPreset(
    val id: String,
    val name: String,
    val uri: String
)

object ThemePresetCatalog {
    val themes = listOf(
        ThemePreset(
            id = "amoled",
            name = "Deep AMOLED",
            description = "Negro profundo con acentos cian y violeta.",
            settings = ThemeSettingsState()
        ),
        ThemePreset(
            id = "cyberpunk",
            name = "Cyberpunk Neon",
            description = "Magenta y azul sobre tonos violeta.",
            settings = ThemeSettingsState(
                presetId = "cyberpunk",
                backgroundColor = "#080612",
                surfaceColor = "#130d29",
                elevatedColor = "#1e1442",
                playerBackgroundColor = "#170f33",
                playerAccentColor = "#ec4899",
                primaryAccentColor = "#ec4899",
                secondaryAccentColor = "#3b82f6",
                textColor = "#fdf4ff",
                mutedTextColor = "#d8b4fe",
                cornerRadiusDp = 16,
                fontFamily = "system",
                wallpaperUri = "android.resource://com.musica.paralosoidos/drawable/wallpaper_abstract",
                blurDp = 24,
                dimAmount = 0.85f
            )
        ),
        ThemePreset(
            id = "sunset",
            name = "Sunset Vinyl",
            description = "Tonos cálidos ámbar y naranja.",
            settings = ThemeSettingsState(
                presetId = "sunset",
                backgroundColor = "#100d0a",
                surfaceColor = "#1c1611",
                elevatedColor = "#292018",
                playerBackgroundColor = "#221912",
                playerAccentColor = "#f97316",
                primaryAccentColor = "#f97316",
                secondaryAccentColor = "#eab308",
                textColor = "#fffbeb",
                mutedTextColor = "#d6d3d1",
                cornerRadiusDp = 20,
                fontFamily = "jakarta",
                wallpaperUri = "android.resource://com.musica.paralosoidos/drawable/wallpaper_wood",
                blurDp = 20,
                dimAmount = 0.88f
            )
        ),
        ThemePreset(
            id = "emerald",
            name = "Emerald Forest",
            description = "Verde esmeralda y acentos menta.",
            settings = ThemeSettingsState(
                presetId = "emerald",
                backgroundColor = "#05110d",
                surfaceColor = "#0c1f19",
                elevatedColor = "#133027",
                playerBackgroundColor = "#0f261f",
                playerAccentColor = "#10b981",
                primaryAccentColor = "#10b981",
                secondaryAccentColor = "#14b8a6",
                textColor = "#f0fdf4",
                mutedTextColor = "#86efac",
                cornerRadiusDp = 24,
                fontFamily = "jakarta",
                blurDp = 16,
                dimAmount = 0.82f
            )
        ),
        ThemePreset(
            id = "monochrome",
            name = "Minimalist Mono",
            description = "Contraste neutro en escala de grises.",
            settings = ThemeSettingsState(
                presetId = "monochrome",
                backgroundColor = "#09090b",
                surfaceColor = "#141418",
                elevatedColor = "#202026",
                playerBackgroundColor = "#17171d",
                playerAccentColor = "#fafafa",
                primaryAccentColor = "#f4f4f5",
                secondaryAccentColor = "#a1a1aa",
                textColor = "#fafafa",
                mutedTextColor = "#71717a",
                cornerRadiusDp = 14,
                fontFamily = "system",
                blurDp = 12,
                dimAmount = 0.85f
            )
        )
    )

    val wallpapers = listOf(
        WallpaperPreset("none", "Sin imagen", ""),
        WallpaperPreset("galaxy", "Nebulosa espacial", "android.resource://com.musica.paralosoidos/drawable/wallpaper_galaxy"),
        WallpaperPreset("wood", "Madera y acústica", "android.resource://com.musica.paralosoidos/drawable/wallpaper_wood"),
        WallpaperPreset("abstract", "Ondas abstractas", "android.resource://com.musica.paralosoidos/drawable/wallpaper_abstract"),
        WallpaperPreset("ocean", "Océano nocturno", "android.resource://com.musica.paralosoidos/drawable/wallpaper_ocean")
    )
}
