package io.github.serfusion.gasukom.timbang

import io.github.serfusion.gasukom.klasifikasi.KelompokSampah
import io.github.serfusion.gasukom.klasifikasi.LabelSampah
import io.github.serfusion.gasukom.klasifikasi.RingkasanIsi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasilTimbangTest {

    private fun hasil(
        gram: Int = 350,
        sumber: SumberBerat = SumberBerat.KAMERA_STABIL,
        isi: RingkasanIsi = RingkasanIsi.KOSONG,
    ) = HasilTimbang(
        gram = gram,
        sumber = sumber,
        jumlahFrameCocok = 6,
        teksTerbaca = "350",
        isi = isi,
        fotoPath = null,
        profil = ProfilTimbangan(),
        waktuMs = 0L,
    )

    @Test
    fun bacaanKameraStabilLolosOtomatis() {
        assertFalse(hasil().perluVerifikasiKader)
        assertTrue(hasil().alasanVerifikasi.isEmpty())
    }

    @Test
    fun ketikManualPerluVerifikasi() {
        val h = hasil(sumber = SumberBerat.KETIK_MANUAL)
        assertTrue(h.perluVerifikasiKader)
        assertEquals(listOf("berat diketik manual"), h.alasanVerifikasi)
    }

    @Test
    fun dikunciSebelumStabilPerluVerifikasi() {
        assertTrue(hasil(sumber = SumberBerat.KAMERA_DIKUNCI_MANUAL).perluVerifikasiKader)
    }

    @Test
    fun adaNonOrganikPerluVerifikasi() {
        val isi = RingkasanIsi(
            organik = emptyList(),
            nonOrganik = listOf(LabelSampah("Kertas", KelompokSampah.NON_ORGANIK, 0.8f)),
        )
        assertTrue(hasil(isi = isi).perluVerifikasiKader)
    }

    @Test
    fun beratDiAtasBatasPerluVerifikasi() {
        assertFalse(hasil(gram = 3_000).perluVerifikasiKader)
        assertTrue(hasil(gram = 3_001).perluVerifikasiKader)
    }
}
