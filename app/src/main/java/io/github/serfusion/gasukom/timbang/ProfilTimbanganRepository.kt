package io.github.serfusion.gasukom.timbang

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.pengaturan: DataStore<Preferences> by preferencesDataStore(name = "pengaturan")

/** Menyimpan profil timbangan di HP (DataStore), supaya cukup diatur sekali. */
class ProfilTimbanganRepository(private val context: Context) {

    private object Kunci {
        val SATUAN = stringPreferencesKey("timbangan_satuan")
        val KAPASITAS = intPreferencesKey("timbangan_kapasitas_gram")
        val MINIMUM = intPreferencesKey("timbangan_minimum_gram")
        val LED = booleanPreferencesKey("timbangan_layar_led")
    }

    val profil: Flow<ProfilTimbangan> = context.pengaturan.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            val bawaan = ProfilTimbangan()
            ProfilTimbangan(
                satuan = p[Kunci.SATUAN]?.let { nama -> SatuanLayar.entries.firstOrNull { it.name == nama } }
                    ?: bawaan.satuan,
                kapasitasGram = p[Kunci.KAPASITAS] ?: bawaan.kapasitasGram,
                minimumGram = p[Kunci.MINIMUM] ?: bawaan.minimumGram,
                layarLed = p[Kunci.LED] ?: bawaan.layarLed,
            )
        }

    suspend fun simpan(profil: ProfilTimbangan) {
        context.pengaturan.edit { p ->
            p[Kunci.SATUAN] = profil.satuan.name
            p[Kunci.KAPASITAS] = profil.kapasitasGram
            p[Kunci.MINIMUM] = profil.minimumGram
            p[Kunci.LED] = profil.layarLed
        }
    }
}
