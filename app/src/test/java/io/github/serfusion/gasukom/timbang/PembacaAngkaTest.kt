package io.github.serfusion.gasukom.timbang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PembacaAngkaTest {

    private val gram = ProfilTimbangan(satuan = SatuanLayar.GRAM, kapasitasGram = 5_000, minimumGram = 10)

    private fun baca(vararg teks: String, profil: ProfilTimbangan = gram): HasilBaca =
        PembacaAngka.baca(teks.map { BarisOcr(it, 80) }, profil)

    private fun gramDari(hasil: HasilBaca): Int {
        assertTrue("Seharusnya terbaca, ternyata $hasil", hasil is HasilBaca.Terbaca)
        return (hasil as HasilBaca.Terbaca).gram
    }

    @Test
    fun angkaGramBiasa() {
        assertEquals(350, gramDari(baca("350")))
        assertEquals(350, gramDari(baca("350 g")))
        assertEquals(350, gramDari(baca("350g")))
        assertEquals(1250, gramDari(baca("1250 gr")))
    }

    @Test
    fun hurufMiripDikembalikanMenjadiAngka() {
        assertEquals(350, gramDari(baca("35O")))
        assertEquals(500, gramDari(baca("S00")))
        assertEquals(1250, gramDari(baca("l25O")))
        assertEquals(88, gramDari(baca("B8")))
        assertEquals(260, gramDari(baca("2b0")))
    }

    @Test
    fun spasiTunggalDiAntaraAngkaDigabung() {
        assertEquals(1250, gramDari(baca("1 250")))
    }

    @Test
    fun kilogramDenganKoma() {
        val kg3 = gram.copy(satuan = SatuanLayar.KG_3)
        assertEquals(350, gramDari(baca("0.350", profil = kg3)))
        assertEquals(350, gramDari(baca("0,350", profil = kg3)))
        assertEquals(350, gramDari(baca("O.35O", profil = kg3)))

        val kg2 = gram.copy(satuan = SatuanLayar.KG_2)
        assertEquals(1250, gramDari(baca("1.25", profil = kg2)))

        val kg1 = gram.copy(satuan = SatuanLayar.KG_1)
        assertEquals(2400, gramDari(baca("2.4", profil = kg1)))
    }

    @Test
    fun komaYangHilangDiambilDariProfil() {
        assertEquals(350, gramDari(baca("0350", profil = gram.copy(satuan = SatuanLayar.KG_3))))
        assertEquals(1250, gramDari(baca("125", profil = gram.copy(satuan = SatuanLayar.KG_2))))
    }

    @Test
    fun satuanDiLayarMengalahkanProfil() {
        assertEquals(1250, gramDari(baca("1.250 kg")))
        assertEquals(2000, gramDari(baca("2 kg")))
        assertEquals(350, gramDari(baca("350 g", profil = gram.copy(satuan = SatuanLayar.KG_3))))
    }

    @Test
    fun barisTertinggiDipilihSebagaiAngkaUtama() {
        val hasil = PembacaAngka.baca(
            listOf(BarisOcr("Max 5kg d=1g", 12), BarisOcr("350", 90), BarisOcr("TARE", 10)),
            gram,
        )
        assertEquals(350, gramDari(hasil))
    }

    @Test
    fun tulisanTanpaAngkaTidakTerbaca() {
        assertTrue(baca("TARE") is HasilBaca.TidakTerbaca)
        assertTrue(baca("ZERO") is HasilBaca.TidakTerbaca)
        assertTrue(baca("Is") is HasilBaca.TidakTerbaca)
        assertTrue(PembacaAngka.baca(emptyList(), gram) is HasilBaca.TidakTerbaca)
    }

    @Test
    fun satuanImperialTidakDianggapAngka() {
        assertTrue(baca("5 lb") is HasilBaca.TidakTerbaca)
        assertTrue(baca("12oz") is HasilBaca.TidakTerbaca)
    }

    @Test
    fun polaUjiLayar8888Ditolak() {
        assertTrue(baca("8.8.8.8") is HasilBaca.TidakTerbaca)
        assertTrue(baca("8888") is HasilBaca.MelebihiKapasitas)
    }

    @Test
    fun beratNegatifAtauTerlaluRingan() {
        assertTrue(baca("-12") is HasilBaca.TerlaluRingan)
        assertTrue(baca("0") is HasilBaca.TerlaluRingan)
        assertTrue(baca("5") is HasilBaca.TerlaluRingan)
    }

    @Test
    fun melebihiKapasitas() {
        val hasil = baca("6200")
        assertTrue(hasil is HasilBaca.MelebihiKapasitas)
        assertEquals(6200, (hasil as HasilBaca.MelebihiKapasitas).gram)
    }

    @Test
    fun teksMentahIkutDisimpanUntukDiagnosis() {
        assertEquals("35O g", baca("35O g").teks)
    }
}
