package io.github.serfusion.gasukom.timbang

import java.util.Locale

object FormatBerat {
    /** 350 → "350 g", 1250 → "1,25 kg", 1000 → "1 kg". */
    fun teks(gram: Int): String {
        if (gram < 1000) return "$gram g"
        val kg = String.format(Locale.ROOT, "%.3f", gram / 1000.0).trimEnd('0').trimEnd('.')
        return kg.replace('.', ',') + " kg"
    }

    /** Usulan poin: 10 poin per 100 g sampah organik terverifikasi. */
    fun perkiraanPoin(gram: Int): Int = gram / 10
}
