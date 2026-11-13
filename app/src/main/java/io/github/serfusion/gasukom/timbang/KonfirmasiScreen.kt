package io.github.serfusion.gasukom.timbang

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.serfusion.gasukom.ui.theme.WarnaStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonfirmasiScreen(
    sesi: SesiTimbangViewModel,
    onUlangi: () -> Unit,
    onSelesai: () -> Unit,
) {
    val hasilSesi by sesi.hasil.collectAsStateWithLifecycle()
    val hasil = hasilSesi ?: run {
        LaunchedEffect(Unit) { onSelesai() }
        return
    }
    val context = LocalContext.current
    var dialogManual by remember { mutableStateOf(false) }
    val foto = remember(hasil.fotoPath) {
        hasil.fotoPath?.let { path -> runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfirmasi Setoran") },
                navigationIcon = {
                    IconButton(onClick = onUlangi) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Ulangi menimbang")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (foto != null) {
                Image(
                    bitmap = foto,
                    contentDescription = "Foto bukti setoran",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                )
            } else {
                Text(
                    "Foto bukti tidak tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            KartuBerat(hasil)

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Isi sampah (perkiraan AI)", style = MaterialTheme.typography.titleMedium)
                    BarisIsiSampah(hasil.isi)
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Perkiraan poin", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "+${FormatBerat.perkiraanPoin(hasil.gram)} poin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        if (hasil.perluVerifikasiKader) {
                            "Poin baru masuk setelah setoran disetujui kader."
                        } else {
                            "10 poin untuk setiap 100 g sampah organik."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Versi uji coba: setoran ini belum disimpan dan poin belum dihitung. " +
                            "Bagikan hasil uji (foto dan angka terbaca) agar pembacaan timbangan bisa disempurnakan.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onUlangi, modifier = Modifier.weight(1f)) { Text("Ulangi") }
                OutlinedButton(onClick = { dialogManual = true }, modifier = Modifier.weight(1f)) {
                    Text("Ketik manual")
                }
            }
            Button(onClick = { bagikanHasil(context, hasil) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Bagikan hasil uji")
            }
            FilledTonalButton(onClick = onSelesai, modifier = Modifier.fillMaxWidth()) {
                Text("Selesai")
            }
        }
    }

    if (dialogManual) {
        DialogKetikManual(
            nilaiAwal = hasil.gram,
            onBatal = { dialogManual = false },
            onSimpan = { gram ->
                dialogManual = false
                sesi.atur(hasil.copy(gram = gram, sumber = SumberBerat.KETIK_MANUAL, jumlahFrameCocok = 0))
            },
        )
    }
}

@Composable
private fun KartuBerat(hasil: HasilTimbang) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Berat",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                FormatBerat.teks(hasil.gram),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                hasil.sumber.keterangan +
                    if (hasil.sumber == SumberBerat.KAMERA_STABIL) " (${hasil.jumlahFrameCocok} kali terbaca sama)" else "",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hasil.perluVerifikasiKader) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = WarnaStatus.Peringatan,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Perlu verifikasi kader: ${hasil.alasanVerifikasi.joinToString()}.",
                        color = WarnaStatus.Peringatan,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = WarnaStatus.Aman,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Lolos verifikasi otomatis",
                        color = WarnaStatus.Aman,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
