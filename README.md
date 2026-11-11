# Música para los Oídos

Una aplicación de reproducción musical. Este proyecto es una alternativa a aplicaciones de música que incluyen anuncios y layout molestos.

La web es el prototipo visual y de interacción. La aplicación principal se está desarrollando como app Android nativa con Kotlin, Jetpack Compose y Media3.

## Estado

- `web/`: preview del diseño y sus interacciones.
- `mobile/`: app Android nativa con biblioteca local mediante Room/MediaStore, reproducción en segundo plano con Media3, álbumes, listas editables, cola modificable, ecualizador y ajustes de estilo persistentes.
- **Pestaña Carpetas:** navegación por directorios locales de música con capacidad de ocultar y mostrar carpetas específicas en la biblioteca.
- **Descargador de audio:** módulo de búsqueda y descarga con selección de formato y calidad de bitrate (MP3/Original), detección de enlaces en portapapeles y protección automática por conectividad de red.
- **Cola y notificaciones en segundo plano:** control interactivo de descargas activas, pausadas o en progreso mediante notificaciones del sistema con acciones integradas.
- **Sincronización de letras LRC:** descarga paralela opcional de letras sincronizadas vía LRCLIB y auto-indexación en MediaStore mediante MediaScannerConnection al instante.
- **Tracker de YouTube:** omite títulos numéricos o identificadores cortos, no procesa pistas de carpetas ocultas y puede cancelarse desde Ajustes. La búsqueda manual de títulos sospechosos requiere confirmar el video antes de guardar el vínculo.
- **Importación de listas:** Ajustes permite importar JSON desde el botón dedicado; las coincidencias ambiguas se presentan para resolver antes de crear la lista.
- El reproductor expandido permite deslizar entre portada y letras LRC sincronizadas. La app busca automáticamente un archivo `.lrc` con el nombre base del audio mediante MediaStore.
- En Ajustes se pueden exportar los nombres de las canciones a TXT e importar listas JSON con el formato `{"name":"Favoritas","songs":["Cancion 1","Cancion 2"]}`. Los nombres deben coincidir de forma única con pistas de la biblioteca.
- La app guarda una copia automática de listas, tema y presets del ecualizador en `Descargas/MusicaParaLosOidos/`. Al reinstalar intenta restaurarla automáticamente; también se puede exportar o restaurar manualmente desde Ajustes.
- La cola se reordena manteniendo pulsada una pista y arrastrándola. Las pistas dentro de una lista se pueden agregar, quitar y reordenar.
- En la pestaña Inicio se incluye un indicador de actualizaciones que comprueba versiones periódicamente contra GitHub Releases y permite descargar e instalar directamente nuevas versiones.

La biblioteca reproduce archivos locales del dispositivo. Las listas, los presets del ecualizador y las preferencias visuales se guardan localmente; los archivos de audio y letras se guardan en el almacenamiento del dispositivo según la configuración de salida.

## Arquitectura Android

- `mobile/app`: `MainActivity`, `MusicaApp` y la raíz de navegación Compose.
- `mobile/core/model`: modelos serializables y constantes de perfiles de audio.
- `mobile/core/data`: Room, DataStore, repositorios, copias de seguridad, motores de descarga y `AppContainer`.
- `mobile/core/playback`: Media3, servicio de reproducción, ecualizador y letras LRC.
- `mobile/core/designsystem`: tema, tipografías, estilos y componentes compartidos.
- `mobile/feature/library`: biblioteca, listas, carpetas y componentes de pistas.
- `mobile/feature/downloader`: interfaz de búsqueda, configuración rápida, tarjetas de resultados y hoja de cola de descargas.
- `mobile/feature/player`: reproductor compacto/expandido y estado de reproducción.
- `mobile/feature/equalizer`: pantalla y controles del ecualizador.
- `mobile/feature/settings`: ajustes de tema y estilo.

## Requisitos para compilar Android

- JDK 17
- Android SDK con API 35

Desde Windows, en la raíz del repositorio:

```powershell
.\mobile\gradlew.bat -p mobile :app:assembleDebug
```

Los APKs de depuración, separados por arquitectura, se generan en `mobile/app/build/outputs/apk/debug/` (por ejemplo, `app-arm64-v8a-debug.apk`).

## Licencia

Este proyecto está disponible bajo la [MIT License](LICENSE).
