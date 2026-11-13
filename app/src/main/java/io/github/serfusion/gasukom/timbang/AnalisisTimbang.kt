package io.github.serfusion.gasukom.timbang

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.serfusion.gasukom.klasifikasi.LabelMentah
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Letak kotak panduan sebagai pecahan (0..1) dari area pratinjau. Dipakai bersama oleh overlay dan analyzer. */
object KotakPanduan {
    val PECAHAN = RectF(0.08f, 0.56f, 0.92f, 0.76f)
}

/** Hasil analisis satu frame kamera. */
class FrameTimbang(
    val waktuMs: Long,
    val baris: List<BarisOcr>,
    /** null bila label isi sampah tidak dihitung ulang pada frame ini. */
    val label: List<LabelMentah>?,
    /** Seluruh area yang terlihat di layar (tegak); dipakai sebagai foto bukti. */
    val terlihat: Bitmap,
    /** Potongan kotak panduan yang sudah diolah, persis seperti yang dibaca OCR. */
    val potonganOcr: Bitmap,
)

/**
 * Menganalisis frame CameraX: membaca angka di kotak panduan dengan ML Kit Text Recognition
 * dan mengenali isi sampah dengan ML Kit Image Labeling. Semua berjalan di HP (offline).
 */
class AnalisisTimbang(
    private val layarLed: () -> Boolean,
    private val onFrame: (FrameTimbang) -> Unit,
) : ImageAnalysis.Analyzer, Closeable {

    private val pengenalTeks = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val pelabel = ImageLabeling.getClient(
        ImageLabelerOptions.Builder().setConfidenceThreshold(0.45f).build(),
    )
    private var terakhirOcrMs = 0L
    private var terakhirLabelMs = 0L

    @Volatile
    private var ditutup = false

    /** Perbandingan lebar:tinggi area pratinjau di layar; diisi oleh layar kamera. */
    @Volatile
    var rasioTampilan: Float = 0f

    override fun analyze(gambar: ImageProxy) {
        try {
            if (ditutup) return
            val sekarang = SystemClock.elapsedRealtime()
            if (sekarang - terakhirOcrMs < JEDA_OCR_MS) return
            terakhirOcrMs = sekarang

            val terlihat = gambar.keBitmapTerlihat(rasioTampilan)
            val potongan = siapkanUntukOcr(potongPecahan(terlihat, KotakPanduan.PECAHAN), layarLed())
            val teks = Tasks.await(pengenalTeks.process(InputImage.fromBitmap(potongan, 0)))
            val baris = teks.textBlocks.flatMap { blok ->
                blok.lines.map { b -> BarisOcr(b.text, b.boundingBox?.height() ?: 0) }
            }

            var label: List<LabelMentah>? = null
            if (sekarang - terakhirLabelMs >= JEDA_LABEL_MS) {
                terakhirLabelMs = sekarang
                label = Tasks.await(pelabel.process(InputImage.fromBitmap(terlihat, 0)))
                    .map { LabelMentah(it.text, it.confidence) }
            }
            if (!ditutup) onFrame(FrameTimbang(sekarang, baris, label, terlihat, potongan))
        } catch (e: Exception) {
            Log.w(TAG, "Frame gagal dianalisis", e)
        } finally {
            gambar.close()
        }
    }

    override fun close() {
        ditutup = true
        pengenalTeks.close()
        pelabel.close()
    }

    private companion object {
        const val TAG = "AnalisisTimbang"
        const val JEDA_OCR_MS = 200L
        const val JEDA_LABEL_MS = 800L
    }
}

private const val TINGGI_MINIMUM_OCR = 160

/**
 * Frame kamera → bitmap tegak berisi hanya area yang terlihat di layar.
 *
 * Utamanya memakai crop rect dari ViewPort CameraX. Sebagai pengaman, hasilnya dicek dengan
 * [rasioTampilan] (lebar:tinggi pratinjau); bila tidak cocok, area terlihat dihitung sendiri
 * dengan potong-tengah (sama seperti tampilan FILL_CENTER), supaya kotak panduan tidak meleset.
 */
internal fun ImageProxy.keBitmapTerlihat(rasioTampilan: Float): Bitmap {
    val penuh = toBitmap()
    val rotasi = imageInfo.rotationDegrees
    val crop = cropRect
    val perluPotong = penuh.width == width && penuh.height == height &&
        crop.width() > 0 && crop.height() > 0 &&
        (crop.width() < penuh.width || crop.height() < penuh.height)
    val terpotong = if (perluPotong) {
        val kiri = crop.left.coerceIn(0, penuh.width - 1)
        val atas = crop.top.coerceIn(0, penuh.height - 1)
        Bitmap.createBitmap(
            penuh, kiri, atas,
            min(crop.width(), penuh.width - kiri),
            min(crop.height(), penuh.height - atas),
        )
    } else {
        penuh
    }
    val hasil = putar(terpotong, rotasi)
    if (rasioTampilan <= 0f) return hasil

    val selisih = abs(hasil.width.toFloat() / hasil.height - rasioTampilan) / rasioTampilan
    if (selisih <= TOLERANSI_RASIO) return hasil

    val tegak = putar(penuh, rotasi)
    val (kiri, atas, lebar, tinggi) = OlahPiksel.potonganTengah(tegak.width, tegak.height, rasioTampilan)
    return Bitmap.createBitmap(tegak, kiri, atas, lebar, tinggi)
}

private const val TOLERANSI_RASIO = 0.05f

private fun putar(bitmap: Bitmap, derajat: Int): Bitmap {
    if (derajat == 0) return bitmap
    val matriks = Matrix().apply { postRotate(derajat.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriks, true)
}

internal fun potongPecahan(sumber: Bitmap, pecahan: RectF): Bitmap {
    val kiri = (pecahan.left * sumber.width).roundToInt().coerceIn(0, sumber.width - 1)
    val atas = (pecahan.top * sumber.height).roundToInt().coerceIn(0, sumber.height - 1)
    val kanan = (pecahan.right * sumber.width).roundToInt().coerceIn(kiri + 1, sumber.width)
    val bawah = (pecahan.bottom * sumber.height).roundToInt().coerceIn(atas + 1, sumber.height)
    return Bitmap.createBitmap(sumber, kiri, atas, kanan - kiri, bawah - atas)
}

/** Perbesar bila terlalu kecil, lalu ubah ke abu-abu berkontras tinggi (dibalik untuk layar LED). */
internal fun siapkanUntukOcr(sumber: Bitmap, layarLed: Boolean): Bitmap {
    val skala = if (sumber.height < TINGGI_MINIMUM_OCR) {
        min(3f, TINGGI_MINIMUM_OCR.toFloat() / sumber.height)
    } else {
        1f
    }
    val gambar = if (skala > 1f) {
        Bitmap.createScaledBitmap(
            sumber,
            (sumber.width * skala).roundToInt(),
            (sumber.height * skala).roundToInt(),
            true,
        )
    } else {
        sumber
    }
    val lebar = gambar.width
    val tinggi = gambar.height
    val piksel = IntArray(lebar * tinggi)
    gambar.getPixels(piksel, 0, lebar, 0, 0, lebar, tinggi)
    return Bitmap.createBitmap(OlahPiksel.abuAbuKontras(piksel, layarLed), lebar, tinggi, Bitmap.Config.ARGB_8888)
}

private const val SISI_TERPANJANG_FOTO = 1280
private const val MAKS_FOTO_TERSIMPAN = 30

/** Simpan foto bukti (JPEG, maks. 1280 px) di cache aplikasi; foto lama dibersihkan. */
internal fun simpanFotoBukti(context: Context, bitmap: Bitmap): File {
    val folder = File(context.cacheDir, "bukti").apply { mkdirs() }
    folder.listFiles()
        ?.sortedByDescending { it.lastModified() }
        ?.drop(MAKS_FOTO_TERSIMPAN - 1)
        ?.forEach { it.delete() }

    val skala = min(1f, SISI_TERPANJANG_FOTO.toFloat() / max(bitmap.width, bitmap.height))
    val kecil = if (skala < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * skala).roundToInt(),
            (bitmap.height * skala).roundToInt(),
            true,
        )
    } else {
        bitmap
    }
    val berkas = File(folder, "setoran_${System.currentTimeMillis()}.jpg")
    FileOutputStream(berkas).use { kecil.compress(Bitmap.CompressFormat.JPEG, 85, it) }
    return berkas
}
