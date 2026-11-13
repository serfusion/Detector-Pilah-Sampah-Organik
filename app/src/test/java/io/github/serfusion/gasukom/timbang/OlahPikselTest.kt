package io.github.serfusion.gasukom.timbang

import org.junit.Assert.assertEquals
import org.junit.Test

class OlahPikselTest {

    private fun abu(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    private fun nilai(piksel: Int): Int = piksel and 0xFF

    @Test
    fun kontrasDiregangkanPenuh() {
        // LCD pudar: latar abu-abu muda (170), angka abu-abu tua (110).
        val masuk = IntArray(100) { if (it < 70) abu(170) else abu(110) }
        val keluar = OlahPiksel.abuAbuKontras(masuk, balik = false)
        assertEquals(255, nilai(keluar[0]))
        assertEquals(0, nilai(keluar[99]))
    }

    @Test
    fun layarLedDibalik() {
        // LED: latar gelap (20), angka terang (230) → setelah dibalik angka menjadi gelap.
        val masuk = IntArray(100) { if (it < 70) abu(20) else abu(230) }
        val keluar = OlahPiksel.abuAbuKontras(masuk, balik = true)
        assertEquals(255, nilai(keluar[0]))
        assertEquals(0, nilai(keluar[99]))
    }

    @Test
    fun potonganTengahMengikutiRasioLayar() {
        // Frame tegak 720x1280 ditampilkan di layar yang lebih ramping (rasio 0,5): sisi kiri-kanan terpotong.
        assertEquals(listOf(40, 0, 640, 1280), OlahPiksel.potonganTengah(720, 1280, 0.5f).toList())
        // Layar lebih lebar dari frame: atas-bawah terpotong.
        assertEquals(listOf(0, 160, 720, 960), OlahPiksel.potonganTengah(720, 1280, 0.75f).toList())
        // Rasio tidak valid: gambar utuh.
        assertEquals(listOf(0, 0, 720, 1280), OlahPiksel.potonganTengah(720, 1280, 0f).toList())
    }

    @Test
    fun gambarSeragamTidakError() {
        val keluar = OlahPiksel.abuAbuKontras(IntArray(10) { abu(128) }, balik = false)
        assertEquals(10, keluar.size)
    }
}
