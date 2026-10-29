// Utilidad para parseo y sincronizacion de archivos de letra LRC (.lrc)

export function parseLRC(lrcText) {
  if (!lrcText || typeof lrcText !== 'string') return [];

  const lines = lrcText.split('\n');
  const result = [];
  // Expresion regular para [mm:ss.xx] o [mm:ss]
  const timeRegex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]/;

  for (const line of lines) {
    const trimmed = line.trim();
    const match = timeRegex.exec(trimmed);
    if (match) {
      const minutes = parseInt(match[1], 10);
      const seconds = parseInt(match[2], 10);
      const milliseconds = match[3] ? parseInt(match[3].padEnd(3, '0').slice(0, 3), 10) : 0;
      const timeInSeconds = minutes * 60 + seconds + milliseconds / 1000;
      const text = trimmed.replace(timeRegex, '').trim();

      if (text) {
        result.push({ time: timeInSeconds, text });
      }
    }
  }

  return result.sort((a, b) => a.time - b.time);
}

export function getCurrentLrcIndex(lrcArray, currentTime) {
  if (!lrcArray || lrcArray.length === 0) return -1;
  for (let i = lrcArray.length - 1; i >= 0; i--) {
    if (currentTime >= lrcArray[i].time) {
      return i;
    }
  }
  return 0;
}

// Mapeo inicial de archivos LRC simulados en el almacenamiento del dispositivo
export const MOCK_DEVICE_LRC_FILES = {
  "Nebulosa Estelar.lrc": `[00:02.00] Luces de neon se encienden al caer el sol
[00:08.50] Un pulso electrico recorre la ciudad entera
[00:16.00] Ondas sonoras viajan en la inmensidad
[00:24.00] Musica para los oidos que despiertan la verdad
[00:32.00] Sigue el compas, siente la gravedad desvanecer
[00:41.00] El eco del manana comienza hoy en cada nota
[00:52.00] Constelaciones que brillan a nuestro compas
[01:05.00] Frecuencia pura en el infinito`,

  "Brisa del Oceano.lrc": `[00:01.50] El sonido de las olas susurra tu nombre
[00:09.00] Cuerdas de nylon bajo el cielo abierto
[00:18.00] La marea se lleva cualquier preocupacion
[00:27.00] Paz en cada acorde, un refugio en la cancion
[00:38.00] Viento suave que acaricia la memoria
[00:49.00] Silencio y calma en el horizonte`,

  "Latidos de Medianoche.lrc": `[00:02.00] Taza caliente y la lluvia en el cristal
[00:10.00] El vinilo gira suave, todo fluye natural
[00:19.00] Ideas que toman forma en la noche sin final
[00:29.00] Musica para los oidos, medicina sensorial
[00:40.00] Acordes suaves para respirar profundo
[00:50.00] Enfoque total en el compas del reloj`
};
