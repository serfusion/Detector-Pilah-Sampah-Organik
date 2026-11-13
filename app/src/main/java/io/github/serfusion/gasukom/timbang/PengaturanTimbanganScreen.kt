package io.github.serfusion.gasukom.timbang

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.serfusion.gasukom.GasukomApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PengaturanTimbanganViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as GasukomApplication).profilTimbangan

    val profil: StateFlow<ProfilTimbangan> = repo.profil
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfilTimbangan())

    fun ubah(profilBaru: ProfilTimbangan) {
        viewModelScope.launch { repo.simpan(profilBaru) }
    }
}

private val PILIHAN_KAPASITAS = listOf(3_000, 5_000, 10_000, 30_000, 100_000)
private val PILIHAN_MINIMUM = listOf(5, 10, 20, 50)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PengaturanTimbanganScreen(
    onKembali: () -> Unit,
    vm: PengaturanTimbanganViewModel = viewModel(),
) {
    val profil by vm.profil.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Timbangan") },
                navigationIcon = {
                    IconButton(onClick = onKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Satuan di layar timbangan", style = MaterialTheme.typography.titleMedium)
            Text(
                "Pilih sesuai tampilan angka saat ada beban. Dengan begitu aplikasi tahu letak koma " +
                    "walau titiknya tidak terbaca kamera.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(Modifier.selectableGroup()) {
                SatuanLayar.entries.forEach { satuan ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = profil.satuan == satuan,
                                onClick = { vm.ubah(profil.copy(satuan = satuan)) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = profil.satuan == satuan, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(satuan.judul, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Contoh tampilan: ${satuan.contoh}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            Text("Kapasitas maksimum timbangan", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PILIHAN_KAPASITAS.forEach { gram ->
                    FilterChip(
                        selected = profil.kapasitasGram == gram,
                        onClick = { vm.ubah(profil.copy(kapasitasGram = gram)) },
                        label = { Text(FormatBerat.teks(gram)) },
                    )
                }
            }

            Text("Berat minimum yang dihitung", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PILIHAN_MINIMUM.forEach { gram ->
                    FilterChip(
                        selected = profil.minimumGram == gram,
                        onClick = { vm.ubah(profil.copy(minimumGram = gram)) },
                        label = { Text(FormatBerat.teks(gram)) },
                    )
                }
            }

            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Layar LED", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Aktifkan bila angkanya menyala (merah/hijau) di latar gelap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = profil.layarLed, onCheckedChange = { vm.ubah(profil.copy(layarLed = it)) })
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tips agar angka mudah terbaca", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "• Tekan tombol TARE/ZERO dengan wadah kosong sebelum menimbang.\n" +
                            "• Hindari pantulan lampu atau sinar matahari di layar timbangan.\n" +
                            "• Pilih timbangan berlayar LCD dengan lampu latar dan angka besar.\n" +
                            "• Di titik timbang bersama, pasang HP pada dudukan tetap.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
