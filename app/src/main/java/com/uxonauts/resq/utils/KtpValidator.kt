package com.uxonauts.resq.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object KtpValidator {

    private val KTP_KEYWORDS = listOf(
        "kartu tanda penduduk",
        "provinsi",
        "kabupaten",
        "kota",
        "nik",
        "nama",
        "tempat",
        "lahir",
        "jenis kelamin",
        "alamat",
        "agama",
        "pekerjaan",
        "kewarganegaraan",
        "berlaku hingga",
        "gol. darah",
        "gol.darah",
        "rt/rw",
        "kel/desa",
        "kecamatan",
        "status perkawinan",
        "wni",
        "seumur hidup"
    )

    private const val MIN_MATCH_COUNT = 3

    fun validate(
        context: Context,
        uri: Uri,
        onResult: (isValid: Boolean, message: String) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text.lowercase()

                    val matchCount = KTP_KEYWORDS.count { keyword ->
                        fullText.contains(keyword)
                    }

                    val hasNikPattern = Regex("\\d{16}").containsMatchIn(fullText)

                    if (matchCount >= MIN_MATCH_COUNT || (matchCount >= 2 && hasNikPattern)) {
                        onResult(true, "KTP terdeteksi")
                    } else {
                        onResult(
                            false,
                            "Foto yang diupload bukan KTP. Pastikan foto menampilkan seluruh bagian KTP dengan jelas."
                        )
                    }
                }
                .addOnFailureListener {
                    onResult(
                        false,
                        "Tidak dapat membaca foto. Pastikan foto KTP jelas dan tidak blur."
                    )
                }
        } catch (e: Exception) {
            onResult(false, "Gagal memproses foto: ${e.localizedMessage}")
        }
    }
}