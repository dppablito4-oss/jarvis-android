# Auditoría Técnica: openWakeWord

## Información

- **Repositorio:** `https://github.com/dscripka/openWakeWord.git`
- **Commit:** `368c03716d1e92591906a84949bc477f3a834455`
- **Licencia:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje:** Python
- **Build:** Setuptools / PyProject (`setup.py`)
- **Estado:** Activo, estándar abierto líder en detección de wake words ligeros y eficientes

---

## Componentes interesantes

1. **`openwakeword.model.Model`**
   - **Archivo:** `openwakeword/model.py`
   - **Responsabilidad:** Orquestador principal de inferencia en Python. Carga modelos de preprocesamiento (`melspectrogram.onnx` o `.tflite`), extracción de embeddings (`embedding_model.onnx` o `.tflite`) y modelos clasificadores. Gestiona el buffer de predicciones y suavizado.

2. **Pipeline de Generación de Datos Sintéticos**
   - **Archivo:** `docs/synthetic_data_generation.md` y notebooks asociados
   - **Responsabilidad:** Flujo automatizado para generar miles de muestras de entrenamiento usando múltiples voces TTS (Piper, ElevenLabs, Google TTS) con variaciones de velocidad, tono y ruido acústico de fondo (Room Impulse Responses - RIR, ruido de ventiladores, TV).

3. **`openwakeword.train.train_model`**
   - **Archivo:** `openwakeword/train.py`
   - **Responsabilidad:** Entrenamiento de redes neuronales densas/convolucionales de clasificación sobre las capas congeladas del extractor de embeddings.

4. **`openwakeword.custom_verifier_model`**
   - **Archivo:** `openwakeword/custom_verifier_model.py`
   - **Responsabilidad:** Segundo nivel de verificación acústica para rechazar falsos positivos de palabras fonéticamente similares.

5. **`openwakeword.vad.VAD`**
   - **Archivo:** `openwakeword/vad.py`
   - **Responsabilidad:** Voice Activity Detection integrado (utilizando Silero VAD) para evitar correr inferencia en silencio.

---

## Flujo interno

```text
Audio PCM 16kHz Mono
       │
       ▼
 [VAD Check] ──(Silencio)──► Descarte rápido
       │ (Voz presente)
       ▼
[Melspectrogram (TFLite/ONNX)] (Ventana 1280 muestras)
       │
       ▼
[Embedding Extractor (TFLite/ONNX)] (Ventana 76x32)
       │
       ▼
[Jarvis Classifier (TFLite)] (Capas densas finales)
       │
       ▼
Score > Umbral (ej. 0.6) ──► Activación Wake Word
```

---

## Dependencias relevantes

- `onnxruntime`
- `ai-edge-litert` (TensorFlow Lite runtime)
- `speexdsp-ns` (Noise suppression)
- `scipy`, `numpy`, `torchaudio`

---

## Qué queremos rescatar

- **Modelos pre-entrenados base:** `COPY_ALLOWED` (bajo Apache-2.0)
  - `melspectrogram.tflite`
  - `embedding_model.tflite`
- **Pipeline de entrenamiento para `jarvis.tflite`:** `ADAPT`
  - Diseñar el script de entrenamiento para la Fase 2 que genere los pesos finales para la palabra clave "Jarvis" / "Hey Jarvis".
- **Lógica de buffer y normalización:** `ADAPT`
  - La normalización de espectrograma `(val / 10.0) + 2.0` y tamaños de ventana (1280 muestras = 80 ms).

---

## Riesgos

- **Falsos positivos:** Modelos ligeros de clasificación pueden dispararse con palabras de fonética similar si no se entrenan suficientes datos negativos y verificador acústico.
- **Entrenamiento fuera de Android:** Requiere un entorno Python con GPU en PC/servidor para sintetizar datos y entrenar el modelo antes de empaquetar el `.tflite` en el APK de Android.

---

## Decisión

**UTILIZAR (`TRAINING_PIPELINE` / `MODEL_PROVIDER`)**
Usar para entrenar el modelo `jarvis.tflite` en Fase 2 y utilizar los artefactos de preprocesamiento TFLite base en Android.
