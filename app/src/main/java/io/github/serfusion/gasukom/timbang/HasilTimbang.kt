package io.github.serfusion.gasukom.timbang

import io.github.serfusion.gasukom.klasifikasi.RingkasanIsi

/** Dari mana angka berat setoran berasal. */
enum class SumberBerat(val keterangan: String, val perluVerifikasi: Boolean) {
    KAMERA_STABIL("Dibaca kamera, stabil", false),
    KAMERA_DIKUNCI_MANUAL("Dibaca kamera, dikunci manual", true),
    KETIK_MANUAL("Diketik manual", true),
}

/** Hasil satu kali menimbang, siap dikonfirmasi warga. */
data class HasilTimbang(
    val gram: Int,
    val sumber: SumberBerat,
    val jumlahFrameCocok: Int,
    val teksTerbaca: String,
    val isi: RingkasanIsi,
    val fotoPath: String?,
    val profil: ProfilTimbangan,
    val waktuMs: Long,
) {
    /**
     * Aturan verifikasi otomatis (docs/02-rancangan-aplikasi.md bagian 10). Batas jumlah setoran
     * per hari ditambahkan saat setoran mulai disimpan (tahap M2).
     */
    val perluVerifikasiKader: Boolean
        get() = sumber.perluVerifikasi || isi.adaNonOrganik || gram > BATAS_BERAT_OTOMATIS_GRAM

    val alasanVerifikasi: List<String>
        get() = buildList {
            if (sumber == SumberBerat.KETIK_MANUAL) add("berat diketik manual")
            if (sumber == SumberBerat.KAMERA_DIKUNCI_MANUAL) add("angka dikunci sebelum stabil")
            if (isi.adaNonOrganik) add("terlihat sampah non-organik")
            if (gram > BATAS_BERAT_OTOMATIS_GRAM) add("berat di atas ${FormatBerat.teks(BATAS_BERAT_OTOMATIS_GRAM)}")
        }

    companion object {
        const val BATAS_BERAT_OTOMATIS_GRAM = 3_000
    }
}
