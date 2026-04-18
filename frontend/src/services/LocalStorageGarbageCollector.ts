/**
 * CA-92: Local Storage Garbage Collector
 * Servicio encargado de limpiar objetos huérfanos estancados en el storage por más de 7 días 
 * o cuando el volumen excede el límite de 50MB. (Aplicable a llaves ibpms_draft_ y ibpms_snapshot_).
 */

export class LocalStorageGarbageCollector {
  private static MAX_STORAGE_BYTES = 50 * 1024 * 1024; // 50MB
  private static MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000; // 7 días

  public static async run() {
      try {
          let totalBytes = 0;
          const cacheKeys: { key: string; bytes: number; timestamp: number }[] = [];

          // 1. Escanear y calcular el peso
          for (let i = 0; i < localStorage.length; i++) {
              const key = localStorage.key(i);
              if (!key || (!key.startsWith('ibpms_draft_') && !key.startsWith('ibpms_snapshot_'))) continue;

              const val = localStorage.getItem(key) || '';
              // Aproximación simplificada del peso en bytes (UTF-16 chars = 2 bytes)
              const bytes = (key.length + val.length) * 2;
              totalBytes += bytes;

              try {
                  const parsed = JSON.parse(val);
                  const timestamp = parsed._timestamp || parsed.updatedAt ? new Date(parsed.updatedAt).getTime() : 0;
                  cacheKeys.push({ key, bytes, timestamp });
              } catch (e) {
                  // Si no se puede parsear o no tiene timestamp, se asume 0 para ser candidato a limpieza
                  cacheKeys.push({ key, bytes, timestamp: 0 });
              }
          }

          let purgedCount = 0;
          let freedBytes = 0;
          const now = Date.now();

          // 2. Condición 1: Time-To-Live (Purgar viejos > 7 días automáticos)
          for (let i = cacheKeys.length - 1; i >= 0; i--) {
              const item = cacheKeys[i];
              if (item.timestamp !== 0 && (now - item.timestamp > this.MAX_AGE_MS) || item.timestamp === 0) {
                  localStorage.removeItem(item.key);
                  freedBytes += item.bytes;
                  totalBytes -= item.bytes;
                  purgedCount++;
                  cacheKeys.splice(i, 1);
              }
          }

          // 3. Condición 2: Cuota Excedida (Ordenar por más viejos y purgar hasta bajar de 50MB)
          if (totalBytes > this.MAX_STORAGE_BYTES) {
              cacheKeys.sort((a, b) => a.timestamp - b.timestamp); // Más antiguos primero
              
              for (const item of cacheKeys) {
                  if (totalBytes <= this.MAX_STORAGE_BYTES) break;
                  
                  localStorage.removeItem(item.key);
                  freedBytes += item.bytes;
                  totalBytes -= item.bytes;
                  purgedCount++;
              }
          }

          if (purgedCount > 0) {
              console.log(`[GC] Purged ${purgedCount} stale drafts (${(freedBytes / 1024).toFixed(2)} KB freed)`);
          }
      } catch (err) {
          console.error('[GC] Error running LocalStorageGarbageCollector', err);
      }
  }
}
