package io.github.serfusion.gasukom.timbang

/** Pengolahan piksel murni (tanpa API Android) supaya bisa diuji di JVM. */
object OlahPiksel {

    /**
     * Area tengah terbesar dengan perbandingan lebar:tinggi = [rasio] di dalam gambar
     * [lebar]×[tinggi]. Sama dengan area yang terlihat di pratinjau mode FILL_CENTER.
     * Hasil: [kiri, atas, lebar, tinggi].
     */
    fun potonganTengah(lebar: Int, tinggi: Int, rasio: Float): IntArray {
        if (lebar <= 0 || tinggi <= 0 || rasio <= 0f) return intArrayOf(0, 0, lebar, tinggi)
        return if (lebar.toFloat() / tinggi > rasio) {
            val lebarBaru = (tinggi * rasio).toInt().coerceIn(1, lebar)
            intArrayOf((lebar - lebarBaru) / 2, 0, lebarBaru, tinggi)
        } else {
            val tinggiBaru = (lebar / rasio).toInt().coerceIn(1, tinggi)
            intArrayOf(0, (tinggi - tinggiBaru) / 2, lebar, tinggiBaru)
        }
    }

    /**
     * Mengubah piksel ARGB menjadi abu-abu dengan kontras diregangkan (persentil 2%–98%),
     * supaya angka LCD yang pudar lebih tegas sebelum dibaca OCR.
     * Bila [balik] true (layar LED: angka terang di latar gelap), warnanya dibalik.
     */
    fun abuAbuKontras(piksel: IntArray, balik: Boolean): IntArray {
        if (piksel.isEmpty()) return piksel
        val terang = IntArray(piksel.size)
        val histogram = IntArray(256)
        for (i in piksel.indices) {
            val c = piksel[i]
            val y = (((c shr 16) and 0xFF) * 299 + ((c shr 8) and 0xFF) * 587 + (c and 0xFF) * 114) / 1000
            terang[i] = y
            histogram[y]++
        }
        val batas = (piksel.size * 0.02).toInt()
        var bawah = 0
        var jumlah = 0
        while (bawah < 255 && jumlah + histogram[bawah] <= batas) {
            jumlah += histogram[bawah]
            bawah++
        }
        var atas = 255
        jumlah = 0
        while (atas > 0 && jumlah + histogram[atas] <= batas) {
            jumlah += histogram[atas]
            atas--
        }
        if (atas <= bawah) {
            bawah = 0
            atas = 255
        }
        val rentang = atas - bawah
        val hasil = IntArray(piksel.size)
        for (i in piksel.indices) {
            var v = ((terang[i] - bawah) * 255 / rentang).coerceIn(0, 255)
            if (balik) v = 255 - v
            hasil[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        return hasil
    }
}
