package io.github.serfusion.gasukom.timbang

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatBeratTest {

    @Test
    fun formatGramDanKilogram() {
        assertEquals("350 g", FormatBerat.teks(350))
        assertEquals("1 kg", FormatBerat.teks(1000))
        assertEquals("1,25 kg", FormatBerat.teks(1250))
        assertEquals("1,205 kg", FormatBerat.teks(1205))
        assertEquals("12,5 kg", FormatBerat.teks(12500))
    }

    @Test
    fun poinSepuluhPerSeratusGram() {
        assertEquals(35, FormatBerat.perkiraanPoin(350))
        assertEquals(0, FormatBerat.perkiraanPoin(9))
        assertEquals(125, FormatBerat.perkiraanPoin(1259))
    }
}
