package io.github.serfusion.gasukom.timbang

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.serfusion.gasukom.GasukomApplication
import io.github.serfusion.gasukom.klasifikasi.LabelMentah
import io.github.serfusion.gasukom.klasifikasi.PemetaLabel
import io.github.serfusion.gasukom.klasifikasi.RingkasanIsi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/** Keadaan layar kamera timbang. */
data class TimbangUi(
    val hasilBaca: HasilBaca? = null,
    val status: StatusStabil = StatusStabil.Mencari,
    val isi: RingkasanIsi = RingkasanIsi.KOSONG,
    val labelMentah: List<LabelMentah> = emptyList(),
    val potonganOcr: Bitmap? = null,
)

class TimbangViewModel(application: Application) : AndroidViewModel(application) {

    val profil: StateFlow<ProfilTimbangan> = (application as GasukomApplication).profilTimbangan.profil
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProfilTimbangan())

    private val penstabil = Penstabil()
    private val _ui = MutableStateFlow(TimbangUi())
    val ui: StateFlow<TimbangUi> = _ui.asStateFlow()

    private val _terkunci = Channel<HasilTimbang>(Channel.BUFFERED)

    /** Dipancarkan sekali setiap kali angka terkunci; layar lalu pindah ke konfirmasi. */
    val terkunci: Flow<HasilTimbang> = _terkunci.receiveAsFlow()

    private val sudahTerkunci = AtomicBoolean(false)

    @Volatile
    private var frameTerakhir: FrameTimbang? = null

    @Volatile
    private var isiTerakhir: RingkasanIsi = RingkasanIsi.KOSONG

    @Volatile
    private var labelTerakhir: List<LabelMentah> = emptyList()

    val analyzer = AnalisisTimbang(
        layarLed = { profil.value.layarLed },
        onFrame = ::prosesFrame,
    )

    /** Dipanggil dari thread analisis kamera untuk setiap frame yang dibaca. */
    private fun prosesFrame(frame: FrameTimbang) {
        if (sudahTerkunci.get()) return
        frameTerakhir = frame
        frame.label?.let {
            labelTerakhir = it
            isiTerakhir = PemetaLabel.petakan(it)
        }
        val hasil = PembacaAngka.baca(frame.baris, profil.value)
        val status = penstabil.tambah(frame.waktuMs, (hasil as? HasilBaca.Terbaca)?.gram)
        _ui.value = TimbangUi(
            hasilBaca = hasil,
            status = status,
            isi = isiTerakhir,
            labelMentah = labelTerakhir,
            potonganOcr = frame.potonganOcr,
        )
        if (status is StatusStabil.Stabil) {
            kunci(status.gram, SumberBerat.KAMERA_STABIL, status.jumlahCocok)
        }
    }

    /** Warga mengunci angka yang sedang terbaca walau belum stabil (perlu verifikasi kader). */
    fun kunciSekarang() {
        when (val status = _ui.value.status) {
            is StatusStabil.Membaca -> kunci(status.gram, SumberBerat.KAMERA_DIKUNCI_MANUAL, 0)
            is StatusStabil.Stabil -> kunci(status.gram, SumberBerat.KAMERA_STABIL, status.jumlahCocok)
            StatusStabil.Mencari -> Unit
        }
    }

    /** Angka tidak terbaca: warga mengetik sendiri (perlu verifikasi kader). */
    fun ketikManual(gram: Int) = kunci(gram, SumberBerat.KETIK_MANUAL, 0)

    private fun kunci(gram: Int, sumber: SumberBerat, jumlahCocok: Int) {
        if (!sudahTerkunci.compareAndSet(false, true)) return
        val frame = frameTerakhir
        val isi = isiTerakhir
        val teks = _ui.value.hasilBaca?.teks.orEmpty()
        val profilSaatIni = profil.value
        viewModelScope.launch(Dispatchers.IO) {
            val foto = frame?.let {
                runCatching { simpanFotoBukti(getApplication(), it.terlihat) }
                    .onFailure { e -> Log.w(TAG, "Foto bukti gagal disimpan", e) }
                    .getOrNull()
            }
            _terkunci.send(
                HasilTimbang(
                    gram = gram,
                    sumber = sumber,
                    jumlahFrameCocok = jumlahCocok,
                    teksTerbaca = teks,
                    isi = isi,
                    fotoPath = foto?.absolutePath,
                    profil = profilSaatIni,
                    waktuMs = System.currentTimeMillis(),
                ),
            )
        }
    }

    /** Siapkan pembacaan baru (saat layar kamera dibuka atau warga menekan "Ulangi"). */
    fun mulaiUlang() {
        while (_terkunci.tryReceive().isSuccess) Unit
        penstabil.reset()
        frameTerakhir = null
        _ui.value = TimbangUi(isi = isiTerakhir, labelMentah = labelTerakhir)
        sudahTerkunci.set(false)
    }

    override fun onCleared() {
        analyzer.close()
    }

    private companion object {
        const val TAG = "TimbangViewModel"
    }
}

/** Menyimpan hasil timbang terakhir agar bisa dibaca layar konfirmasi (dibagi di tingkat activity). */
class SesiTimbangViewModel : ViewModel() {
    private val _hasil = MutableStateFlow<HasilTimbang?>(null)
    val hasil: StateFlow<HasilTimbang?> = _hasil.asStateFlow()

    fun atur(hasil: HasilTimbang?) {
        _hasil.value = hasil
    }
}
