package io.github.serfusion.gasukom.timbang

import kotlin.math.min

/** Status pembacaan angka dari beberapa frame terakhir. */
sealed interface StatusStabil {
    data object Mencari : StatusStabil

    /** Ada angka terbaca tetapi belum cukup stabil. [progres] bernilai 0..1. */
    data class Membaca(val gram: Int, val progres: Float) : StatusStabil

    data class Stabil(val gram: Int, val jumlahCocok: Int) : StatusStabil
}

/**
 * Mengunci angka hanya bila nilai yang sama terbaca berulang kali dan bertahan cukup lama.
 * Satu-dua frame yang salah baca tidak membatalkan penguncian, tetapi berat yang masih
 * berubah (sampah baru ditaruh) tidak akan terkunci.
 *
 * Persetujuan dihitung sejak angka itu pertama kali muncul, sehingga bacaan saat sampah
 * masih ditaruh tidak memperlambat penguncian. Syaratnya berbasis waktu, jadi HP lambat
 * (±1–2 frame per detik) tetap bisa mengunci.
 */
class Penstabil(
    private val jendelaMs: Long = 2_000,
    private val minimumCocok: Int = 3,
    private val minimumPersetujuan: Double = 0.7,
    private val minimumDurasiMs: Long = 600,
) {
    private data class Catatan(val waktuMs: Long, val gram: Int?)

    private val riwayat = ArrayDeque<Catatan>()

    @Synchronized
    fun tambah(waktuMs: Long, gram: Int?): StatusStabil {
        if (riwayat.isNotEmpty() && waktuMs < riwayat.last().waktuMs) riwayat.clear()
        riwayat.addLast(Catatan(waktuMs, gram))
        while (riwayat.isNotEmpty() && waktuMs - riwayat.first().waktuMs > jendelaMs) {
            riwayat.removeFirst()
        }

        val terbaca = riwayat.mapNotNull { it.gram }
        if (terbaca.isEmpty()) return StatusStabil.Mencari

        val hitungan = terbaca.groupingBy { it }.eachCount()
        val terakhir = terbaca.last()
        // Nilai terbanyak; bila seri, utamakan nilai yang paling baru terbaca.
        val modus = hitungan.entries.maxWith(
            compareBy<Map.Entry<Int, Int>>({ it.value }, { if (it.key == terakhir) 1 else 0 }),
        ).key
        val jumlah = hitungan.getValue(modus)
        val indeksPertama = riwayat.indexOfFirst { it.gram == modus }
        val persetujuan = jumlah.toDouble() / (riwayat.size - indeksPertama)
        val durasi = waktuMs - riwayat[indeksPertama].waktuMs

        val stabil = jumlah >= minimumCocok &&
            persetujuan >= minimumPersetujuan &&
            durasi >= minimumDurasiMs &&
            terakhir == modus
        if (stabil) return StatusStabil.Stabil(modus, jumlah)

        val progres = min(1.0, jumlah.toDouble() / minimumCocok) *
            min(1.0, persetujuan / minimumPersetujuan) *
            min(1.0, durasi.toDouble() / minimumDurasiMs)
        return StatusStabil.Membaca(modus, progres.toFloat().coerceIn(0f, 0.99f))
    }

    @Synchronized
    fun reset() = riwayat.clear()
}
