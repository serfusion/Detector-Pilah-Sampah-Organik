package io.github.serfusion.gasukom.klasifikasi

/** Label mentah dari ML Kit Image Labeling. */
data class LabelMentah(val nama: String, val keyakinan: Float)

enum class KelompokSampah { ORGANIK, NON_ORGANIK }

/** Label yang sudah diterjemahkan dan dikelompokkan untuk ditampilkan ke warga. */
data class LabelSampah(val namaTampil: String, val kelompok: KelompokSampah, val keyakinan: Float)

data class RingkasanIsi(val organik: List<LabelSampah>, val nonOrganik: List<LabelSampah>) {
    val adaNonOrganik: Boolean get() = nonOrganik.isNotEmpty()

    companion object {
        val KOSONG = RingkasanIsi(emptyList(), emptyList())
    }
}

/**
 * Memetakan label umum ML Kit (447 label bawaan model dasar) ke kelompok sampah.
 *
 * Catatan: model dasar ML Kit tidak punya label "plastik" atau "botol". Peringatan
 * non-organik di sini hanya pengingat; deteksi plastik yang andal butuh model khusus
 * (lihat docs/02-rancangan-aplikasi.md bagian 8).
 */
object PemetaLabel {

    const val AMBANG_ORGANIK = 0.5f
    const val AMBANG_NON_ORGANIK = 0.6f

    private val ORGANIK: Map<String, String> = mapOf(
        "Food" to "Makanan",
        "Fruit" to "Buah",
        "Vegetable" to "Sayur",
        "Plant" to "Tanaman",
        "Flora" to "Tumbuhan",
        "Flower" to "Bunga",
        "Petal" to "Bunga",
        "Twig" to "Ranting",
        "Branch" to "Ranting",
        "Bread" to "Roti",
        "Cake" to "Kue",
        "Cookie" to "Kue",
        "Pie" to "Kue",
        "Pasteles" to "Kue",
        "Cuisine" to "Masakan",
        "Meal" to "Masakan",
        "Lunch" to "Masakan",
        "Supper" to "Masakan",
        "Fast food" to "Masakan",
        "Hot dog" to "Masakan",
        "Cheeseburger" to "Masakan",
        "Pizza" to "Masakan",
        "Sushi" to "Masakan",
        "Pho" to "Masakan",
        "Bento" to "Nasi",
        "Couscous" to "Nasi/biji-bijian",
        "Coffee" to "Ampas kopi",
        "Soil" to "Tanah/kompos",
        "Shell" to "Cangkang",
    )

    private val NON_ORGANIK: Map<String, String> = mapOf(
        "Bag" to "Kantong (plastik?)",
        "Paper" to "Kertas",
        "Newspaper" to "Kertas",
        "Receipt" to "Kertas",
        "Cola" to "Minuman kemasan",
        "Juice" to "Minuman kemasan",
        "Wine" to "Botol",
        "Alcohol" to "Botol",
        "Junk" to "Sampah campur",
        "Cutlery" to "Sendok/garpu",
        "Textile" to "Kain",
        "Tire" to "Karet",
        "Balloon" to "Balon",
        "Toy" to "Mainan",
    )

    fun petakan(label: List<LabelMentah>): RingkasanIsi {
        val hasil = label.mapNotNull { l ->
            when {
                l.nama in ORGANIK && l.keyakinan >= AMBANG_ORGANIK ->
                    LabelSampah(ORGANIK.getValue(l.nama), KelompokSampah.ORGANIK, l.keyakinan)
                l.nama in NON_ORGANIK && l.keyakinan >= AMBANG_NON_ORGANIK ->
                    LabelSampah(NON_ORGANIK.getValue(l.nama), KelompokSampah.NON_ORGANIK, l.keyakinan)
                else -> null
            }
        }
            // Beberapa label bisa bermakna sama (mis. Twig & Branch); simpan yang paling yakin.
            .groupBy { it.namaTampil }
            .map { (_, sama) -> sama.maxBy { it.keyakinan } }
            .sortedByDescending { it.keyakinan }
        return RingkasanIsi(
            organik = hasil.filter { it.kelompok == KelompokSampah.ORGANIK },
            nonOrganik = hasil.filter { it.kelompok == KelompokSampah.NON_ORGANIK },
        )
    }
}
