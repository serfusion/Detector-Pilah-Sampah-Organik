package io.github.serfusion.gasukom.timbang

/** Cara timbangan menampilkan angka di layarnya. Dipakai agar posisi koma tidak perlu ditebak. */
enum class SatuanLayar(val desimal: Int, val kilogram: Boolean, val judul: String, val contoh: String) {
    GRAM(0, false, "Gram", "350"),
    KG_1(1, true, "Kilogram, 1 angka di belakang koma", "2.4"),
    KG_2(2, true, "Kilogram, 2 angka di belakang koma", "2.45"),
    KG_3(3, true, "Kilogram, 3 angka di belakang koma", "0.350"),
}

/** Pengaturan timbangan yang dipakai di HP atau titik timbang ini. */
data class ProfilTimbangan(
    val satuan: SatuanLayar = SatuanLayar.GRAM,
    val kapasitasGram: Int = 5_000,
    val minimumGram: Int = 10,
    /** true bila angka menyala di latar gelap (LED); gambar dibalik sebelum dibaca. */
    val layarLed: Boolean = false,
)
