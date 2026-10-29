# Música para los Oídos

Una aplicación de reproducción musical. Este proyecto es una alternativa a aplicaciones de musica que incluyen anuncios y layout molestos.

La web es el prototipo visual y de interacción. La aplicación principal se está desarrollando como app Android nativa con Kotlin, Jetpack Compose y Media3.

## Estado

- `web/`: preview del diseño y sus interacciones.
- `mobile/`: app Android nativa con biblioteca local mediante Room/MediaStore, reproducción en segundo plano con Media3, álbumes, listas editables, cola modificable, ecualizador y ajustes de estilo persistentes.
- El reproductor expandido permite deslizar entre portada y letras LRC sincronizadas. La app busca automáticamente un archivo `.lrc` con el nombre base del audio mediante MediaStore; Android puede ocultar archivos de letras no indexados por las restricciones de almacenamiento.
- En Ajustes se pueden exportar los nombres de las canciones a TXT e importar listas JSON con el formato `{"name":"Favoritas","songs":["Cancion 1","Cancion 2"]}`. Los nombres deben coincidir de forma única con pistas de la biblioteca.
- La cola se reordena manteniendo pulsada una pista y arrastrándola. Las pistas dentro de una lista se pueden agregar, quitar y reordenar.
- Esta versión no ofrece rastreo de YouTube ni descargas. La paridad visual completa con el prototipo web y la validación funcional en distintos dispositivos siguen en desarrollo.

La biblioteca reproduce archivos locales del dispositivo. Las listas, los presets del ecualizador y las preferencias visuales se guardan localmente; los archivos de audio y letras no se copian al almacenamiento interno de la aplicación.

## Requisitos para compilar Android

- JDK 17
- Android SDK con API 35

Desde Windows, en la raíz del repositorio:

```powershell
.\mobile\gradlew.bat -p mobile :app:assembleDebug
```

Los APKs de depuración, separados por arquitectura, se generan en `mobile/app/build/outputs/apk/debug/` (por ejemplo, `app-arm64-v8a-debug.apk`).
