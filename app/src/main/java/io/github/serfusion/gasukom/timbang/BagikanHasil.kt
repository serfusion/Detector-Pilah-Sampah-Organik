package io.github.serfusion.gasukom.timbang

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.serfusion.gasukom.BuildConfig
import java.io.File

/** Teks ringkasan hasil uji, berguna untuk dikirim ke tim pengembang saat menyetel pembacaan. */
fun teksHasilUji(hasil: HasilTimbang): String = buildString {
    appendLine("Uji GASUKOM Digital")
    appendLine("Berat: ${FormatBerat.teks(hasil.gram)} (${hasil.sumber.keterangan})")
    appendLine("Teks terbaca kamera: \"${hasil.teksTerbaca}\"")
    appendLine(
        "Profil timbangan: ${hasil.profil.satuan.judul}, maks ${FormatBerat.teks(hasil.profil.kapasitasGram)}" +
            if (hasil.profil.layarLed) ", layar LED" else "",
    )
    if (hasil.isi.organik.isNotEmpty()) {
        appendLine("Isi terlihat: ${hasil.isi.organik.joinToString { it.namaTampil }}")
    }
    if (hasil.isi.adaNonOrganik) {
        appendLine("Peringatan non-organik: ${hasil.isi.nonOrganik.joinToString { it.namaTampil }}")
    }
    append("Versi aplikasi: ${BuildConfig.VERSION_NAME}")
}

/** Membuka menu "Bagikan" Android berisi foto bukti dan ringkasan hasil uji. */
fun bagikanHasil(context: Context, hasil: HasilTimbang) {
    val kirim = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, teksHasilUji(hasil))
        val berkas = hasil.fotoPath?.let(::File)?.takeIf { it.exists() }
        if (berkas != null) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", berkas)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
    }
    context.startActivity(Intent.createChooser(kirim, "Bagikan hasil uji"))
}
