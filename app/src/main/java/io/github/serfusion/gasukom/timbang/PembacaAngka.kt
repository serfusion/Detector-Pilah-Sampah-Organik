package io.github.serfusion.gasukom.timbang

import kotlin.math.pow
import kotlin.math.roundToLong

/** Satu baris teks hasil OCR beserta tinggi kotaknya dalam piksel. */
data class BarisOcr(val teks: String, val tinggi: Int)

/** Hasil membaca satu frame kamera. */
sealed interface HasilBaca {
    /** Seluruh teks yang terbaca OCR (untuk diagnosis). */
    val teks: String

    data class Terbaca(val gram: Int, override val teks: String) : HasilBaca
    data class TerlaluRingan(val gram: Int, override val teks: String) : HasilBaca
    data class MelebihiKapasitas(val gram: Int, override val teks: String) : HasilBaca
    data class TidakTerbaca(override val teks: String) : HasilBaca
}

/**
 * Mengubah teks OCR dari layar timbangan menjadi berat dalam gram.
 *
 * Layar timbangan memakai angka 7-segmen yang sering salah dibaca sebagai huruf
 * (mis. 0→O, 5→S, 1→l). Huruf-huruf mirip itu dikembalikan menjadi angka, tetapi hanya
 * pada deretan yang memuat minimal satu angka asli, supaya kata seperti "TARE" atau
 * "ZERO" tidak ikut terbaca.
 */
object PembacaAngka {

    private val HURUF_MIRIP: Map<Char, Char> = mapOf(
        'O' to '0', 'o' to '0', 'D' to '0', 'Q' to '0',
        'I' to '1', 'l' to '1', '|' to '1', '!' to '1', 'i' to '1',
        'Z' to '2', 'z' to '2',
        'S' to '5', 's' to '5',
        'b' to '6',
        'B' to '8',
        'q' to '9',
    )

    private const val KARAKTER_ANGKA = "0-9ODQIZSBbilosqz|!"

    /** Deretan angka/huruf mirip angka; boleh berisi titik, koma, dan satu spasi di antara angka. */
    private val POLA_DERET = Regex("[$KARAKTER_ANGKA](?:[$KARAKTER_ANGKA.,]|\\s(?=[$KARAKTER_ANGKA.,]))*")
    private val POLA_KILOGRAM = Regex("^\\s*kg", RegexOption.IGNORE_CASE)
    private val POLA_GRAM = Regex("^\\s*(g|gr|gram)\\b", RegexOption.IGNORE_CASE)
    private val POLA_SATUAN_LAIN = Regex("^\\s*#?(lbs|lb|oz|ml)(?![a-z])", RegexOption.IGNORE_CASE)

    /** "lb"/"oz" tersusun dari huruf mirip angka (l→1, b→6, o→0, z→2), jadi ditandai dulu dengan '#'. */
    private val POLA_SATUAN_IMPERIAL = Regex("(?<![a-z])(lbs|lb|oz)(?![a-z])", RegexOption.IGNORE_CASE)

    private const val MAKS_DIGIT = 7

    private enum class Satuan { GRAM, KILOGRAM, LAIN }

    private data class Kandidat(
        val digit: String,
        /** Banyak angka di belakang koma; null bila tidak ada koma, -1 bila komanya lebih dari satu. */
        val jumlahDesimal: Int?,
        val negatif: Boolean,
        val satuan: Satuan?,
        val tinggi: Int,
    )

    fun baca(baris: List<BarisOcr>, profil: ProfilTimbangan): HasilBaca {
        val semuaTeks = baris.joinToString(" | ") { it.teks.trim() }
        // Angka utama di layar timbangan adalah tulisan paling besar di dalam kotak panduan.
        val terbaik = baris.flatMap(::kandidatDari)
            .maxWithOrNull(compareBy<Kandidat>({ it.tinggi }, { it.digit.length }))
            ?: return HasilBaca.TidakTerbaca(semuaTeks)
        val gram = keGram(terbaik, profil) ?: return HasilBaca.TidakTerbaca(semuaTeks)
        return when {
            terbaik.negatif -> HasilBaca.TerlaluRingan(0, semuaTeks)
            gram < profil.minimumGram -> HasilBaca.TerlaluRingan(gram, semuaTeks)
            gram > profil.kapasitasGram -> HasilBaca.MelebihiKapasitas(gram, semuaTeks)
            else -> HasilBaca.Terbaca(gram, semuaTeks)
        }
    }

    private fun kandidatDari(baris: BarisOcr): List<Kandidat> {
        val teks = POLA_SATUAN_IMPERIAL.replace(baris.teks, "#$1")
        return POLA_DERET.findAll(teks).mapNotNull { cocok ->
            val mentah = cocok.value.trimEnd('.', ',')
            if (mentah.none { it.isDigit() }) return@mapNotNull null
            val normal = mentah.filterNot { it.isWhitespace() }
                .map { HURUF_MIRIP[it] ?: it }
                .joinToString("")
            val digit = normal.filter { it.isDigit() }
            if (digit.isEmpty() || digit.length > MAKS_DIGIT) return@mapNotNull null
            val posisiPemisah = normal.indexOfFirst { it == '.' || it == ',' }
            val jumlahDesimal = when (normal.count { it == '.' || it == ',' }) {
                0 -> null
                1 -> normal.length - 1 - posisiPemisah
                else -> -1
            }
            val sebelum = teks.substring(0, cocok.range.first).trimEnd()
            val sesudah = teks.substring(cocok.range.last + 1)
            val satuan = when {
                POLA_KILOGRAM.containsMatchIn(sesudah) -> Satuan.KILOGRAM
                POLA_GRAM.containsMatchIn(sesudah) -> Satuan.GRAM
                POLA_SATUAN_LAIN.containsMatchIn(sesudah) -> Satuan.LAIN
                else -> null
            }
            Kandidat(
                digit = digit,
                jumlahDesimal = jumlahDesimal,
                negatif = sebelum.endsWith("-"),
                satuan = satuan,
                tinggi = baris.tinggi,
            )
        }.toList()
    }

    private fun keGram(k: Kandidat, profil: ProfilTimbangan): Int? {
        if (k.jumlahDesimal == -1 || k.satuan == Satuan.LAIN) return null
        val kilogram = when (k.satuan) {
            Satuan.KILOGRAM -> true
            Satuan.GRAM -> false
            else -> profil.satuan.kilogram
        }
        // Bila titik desimal tidak terbaca, posisinya diambil dari profil timbangan.
        val desimal = k.jumlahDesimal ?: if (kilogram && profil.satuan.kilogram) profil.satuan.desimal else 0
        val nilai = k.digit.toLong() / 10.0.pow(desimal)
        val gram = if (kilogram) nilai * 1000 else nilai
        return gram.roundToLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
