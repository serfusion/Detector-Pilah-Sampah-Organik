package io.github.serfusion.gasukom.ui.beranda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.serfusion.gasukom.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    onSetorSampah: () -> Unit,
    onPengaturanTimbangan: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GASUKOM Digital", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KartuSapaan()

            Button(
                onClick = onSetorSampah,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Setor Sampah", style = MaterialTheme.typography.titleLarge)
            }

            ItemMenu(
                ikon = Icons.Filled.Settings,
                judul = "Pengaturan Timbangan",
                keterangan = "Atur satuan layar dan kapasitas timbangan",
                onClick = onPengaturanTimbangan,
            )
            ItemMenu(
                ikon = Icons.Filled.Star,
                judul = "Leaderboard",
                keterangan = "Segera hadir",
                onClick = null,
            )
            ItemMenu(
                ikon = Icons.AutoMirrored.Filled.List,
                judul = "Riwayat Setoran",
                keterangan = "Segera hadir",
                onClick = null,
            )

            KartuVersiUji()

            Text(
                text = "Versi ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun KartuSapaan() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Halo, Sahabat Bumi!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Satu Rumah, Satu Komposter, Setiap Sampah Jadi Kontribusi.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ItemMenu(
    ikon: ImageVector,
    judul: String,
    keterangan: String,
    onClick: (() -> Unit)?,
) {
    val aktif = onClick != null
    val isi: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                ikon,
                contentDescription = null,
                tint = if (aktif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(judul, style = MaterialTheme.typography.titleMedium)
                Text(
                    keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (onClick != null) {
        ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) { isi() }
    } else {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) { isi() }
    }
}

@Composable
private fun KartuVersiUji() {
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
                "Versi uji coba. Kamera sudah bisa membaca angka timbangan dan mengenali isi sampah. " +
                    "Menyimpan setoran, poin, dan leaderboard menyusul di tahap berikutnya.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
