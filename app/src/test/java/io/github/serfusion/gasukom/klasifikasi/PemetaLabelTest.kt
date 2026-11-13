package io.github.serfusion.gasukom.klasifikasi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PemetaLabelTest {

    @Test
    fun labelMakananMenjadiOrganik() {
        val hasil = PemetaLabel.petakan(
            listOf(LabelMentah("Vegetable", 0.9f), LabelMentah("Fruit", 0.7f), LabelMentah("Tableware", 0.8f)),
        )
        assertEquals(listOf("Sayur", "Buah"), hasil.organik.map { it.namaTampil })
        assertFalse(hasil.adaNonOrganik)
    }

    @Test
    fun kantongDanKertasMemicuPeringatan() {
        val hasil = PemetaLabel.petakan(listOf(LabelMentah("Bag", 0.75f), LabelMentah("Paper", 0.65f)))
        assertTrue(hasil.adaNonOrganik)
        assertEquals(listOf("Kantong (plastik?)", "Kertas"), hasil.nonOrganik.map { it.namaTampil })
    }

    @Test
    fun keyakinanRendahDiabaikan() {
        val hasil = PemetaLabel.petakan(listOf(LabelMentah("Bag", 0.55f), LabelMentah("Food", 0.4f)))
        assertEquals(RingkasanIsi.KOSONG, hasil)
    }

    @Test
    fun labelBermaknaSamaDigabung() {
        val hasil = PemetaLabel.petakan(listOf(LabelMentah("Twig", 0.6f), LabelMentah("Branch", 0.8f)))
        assertEquals(1, hasil.organik.size)
        assertEquals(0.8f, hasil.organik.single().keyakinan, 0.0001f)
    }
}
