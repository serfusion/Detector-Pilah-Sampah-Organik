package io.github.serfusion.gasukom.timbang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PenstabilTest {

    /** Memasukkan deretan bacaan dengan jeda 200 ms (±5 frame per detik). */
    private fun Penstabil.isi(vararg gram: Int?, mulai: Long = 0L): StatusStabil {
        var status: StatusStabil = StatusStabil.Mencari
        gram.forEachIndexed { i, g -> status = tambah(mulai + i * 200L, g) }
        return status
    }

    @Test
    fun tanpaBacaanMasihMencari() {
        assertEquals(StatusStabil.Mencari, Penstabil().isi(null, null, null))
    }

    @Test
    fun angkaSamaBeberapaKaliMenjadiStabil() {
        val status = Penstabil().isi(350, 350, 350, 350, 350)
        assertTrue("Seharusnya stabil: $status", status is StatusStabil.Stabil)
        assertEquals(350, (status as StatusStabil.Stabil).gram)
    }

    @Test
    fun terlaluSingkatBelumStabil() {
        val status = Penstabil().isi(350, 350, 350)
        assertTrue(status is StatusStabil.Membaca)
    }

    @Test
    fun satuSalahBacaTidakMembatalkan() {
        val status = Penstabil().isi(350, 350, 850, 350, 350, 350, 350)
        assertTrue("Seharusnya stabil: $status", status is StatusStabil.Stabil)
        assertEquals(350, (status as StatusStabil.Stabil).gram)
    }

    @Test
    fun beratYangMasihBerubahTidakTerkunci() {
        val status = Penstabil().isi(120, 180, 240, 300, 330, 345, 350)
        assertTrue("Seharusnya belum stabil: $status", status !is StatusStabil.Stabil)
    }

    @Test
    fun bacaanTerakhirBerbedaMenundaPenguncian() {
        val status = Penstabil().isi(350, 350, 350, 350, 350, 420)
        assertTrue("Seharusnya belum stabil: $status", status !is StatusStabil.Stabil)
    }

    @Test
    fun terlaluBanyakFrameKosongMenundaPenguncian() {
        val status = Penstabil().isi(350, null, null, 350, null, 350, null, 350)
        assertTrue("Seharusnya belum stabil: $status", status !is StatusStabil.Stabil)
    }

    @Test
    fun progresNaikSaatAngkaBerulang() {
        val penstabil = Penstabil()
        val awal = penstabil.isi(350) as StatusStabil.Membaca
        val lanjut = penstabil.tambah(200L, 350) as StatusStabil.Membaca
        assertTrue(lanjut.progres > awal.progres)
    }

    @Test
    fun setelahBeratDiamLangsungTerkunci() {
        // Bacaan saat sampah masih ditaruh tidak boleh memperlambat penguncian.
        val status = Penstabil().isi(120, 180, 240, 350, 350, 350, 350)
        assertTrue("Seharusnya stabil: $status", status is StatusStabil.Stabil)
        assertEquals(350, (status as StatusStabil.Stabil).gram)
    }

    @Test
    fun hpLambatTetapBisaTerkunci() {
        // HP lambat: OCR hanya ±2 frame per detik.
        val penstabil = Penstabil()
        penstabil.tambah(0L, 350)
        penstabil.tambah(500L, 350)
        val status = penstabil.tambah(1_000L, 350)
        assertTrue("Seharusnya stabil: $status", status is StatusStabil.Stabil)
    }

    @Test
    fun resetMenghapusRiwayat() {
        val penstabil = Penstabil()
        penstabil.isi(350, 350, 350, 350)
        penstabil.reset()
        assertTrue(penstabil.tambah(5_000L, 350) is StatusStabil.Membaca)
    }
}
