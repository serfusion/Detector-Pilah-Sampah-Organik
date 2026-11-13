package io.github.serfusion.gasukom.timbang

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.serfusion.gasukom.ui.IkonSenter
import io.github.serfusion.gasukom.ui.theme.WarnaStatus
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Size as UkuranGambar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimbangScreen(
    sesi: SesiTimbangViewModel,
    onTerkunci: () -> Unit,
    onKembali: () -> Unit,
    onPengaturan: () -> Unit,
    vm: TimbangViewModel = viewModel(),
) {
    val context = LocalContext.current
    var izinKamera by remember { mutableStateOf(adaIzinKamera(context)) }
    var pernahDitolak by remember { mutableStateOf(false) }
    val mintaIzin = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { diberi ->
        izinKamera = diberi
        if (!diberi) pernahDitolak = true
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { izinKamera = adaIzinKamera(context) }
    LaunchedEffect(Unit) {
        // Urutan penting: bersihkan hasil lama dulu, baru dengarkan penguncian berikutnya.
        vm.mulaiUlang()
        if (!izinKamera) mintaIzin.launch(Manifest.permission.CAMERA)
        vm.terkunci.collect { hasil ->
            sesi.atur(hasil)
            onTerkunci()
        }
    }

    val ui by vm.ui.collectAsStateWithLifecycle()
    val profil by vm.profil.collectAsStateWithLifecycle()
    var senter by rememberSaveable { mutableStateOf(false) }
    var zoom by rememberSaveable { mutableFloatStateOf(0f) }
    var tampilDetail by rememberSaveable { mutableStateOf(false) }
    var dialogManual by remember { mutableStateOf(false) }
    var punyaSenter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timbang Sampah") },
                navigationIcon = {
                    IconButton(onClick = onKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (punyaSenter) {
                        IconToggleButton(checked = senter, onCheckedChange = { senter = it }) {
                            Icon(IkonSenter, contentDescription = if (senter) "Matikan senter" else "Nyalakan senter")
                        }
                    }
                    IconToggleButton(checked = tampilDetail, onCheckedChange = { tampilDetail = it }) {
                        Icon(Icons.Filled.Info, contentDescription = "Detail pembacaan")
                    }
                    IconButton(onClick = onPengaturan) {
                        Icon(Icons.Filled.Settings, contentDescription = "Pengaturan timbangan")
                    }
                },
            )
        },
    ) { padding ->
        if (!izinKamera) {
            PermintaanIzinKamera(
                pernahDitolak = pernahDitolak,
                onMinta = { mintaIzin.launch(Manifest.permission.CAMERA) },
                onBukaPengaturan = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                },
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            )
        } else {
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    PratinjauKamera(
                        analyzer = vm.analyzer,
                        senter = senter,
                        zoom = zoom,
                        onPunyaSenter = { punyaSenter = it },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        OverlayPanduan(status = ui.status, modifier = Modifier.fillMaxSize())
                    }
                    if (tampilDetail) {
                        PanelDetail(
                            ui = ui,
                            profil = profil,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                        )
                    }
                }
                PanelStatus(
                    ui = ui,
                    zoom = zoom,
                    onZoom = { zoom = it },
                    onKunci = vm::kunciSekarang,
                    onKetikManual = { dialogManual = true },
                )
            }
        }
    }

    if (dialogManual) {
        DialogKetikManual(
            onBatal = { dialogManual = false },
            onSimpan = { gram ->
                dialogManual = false
                vm.ketikManual(gram)
            },
        )
    }
}

private fun adaIzinKamera(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

/**
 * Pratinjau CameraX. Preview dan analisis diikat dengan ViewPort yang sama, sehingga
 * frame yang dianalisis persis sama dengan yang terlihat di layar (kotak panduan pas).
 */
@Composable
private fun PratinjauKamera(
    analyzer: AnalisisTimbang,
    senter: Boolean,
    zoom: Float,
    onPunyaSenter: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var kamera by remember { mutableStateOf<Camera?>(null) }
    var galat by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner, analyzer) {
        val pelaksana = Executors.newSingleThreadExecutor()
        val calonPenyedia = ProcessCameraProvider.getInstance(context)
        var aktif = true
        var analisis: ImageAnalysis? = null

        // Analyzer perlu tahu rasio area pratinjau agar potongan kotak panduan tepat.
        val pemantauUkuran = android.view.View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (v.width > 0 && v.height > 0) analyzer.rasioTampilan = v.width.toFloat() / v.height
        }
        previewView.addOnLayoutChangeListener(pemantauUkuran)
        if (previewView.width > 0 && previewView.height > 0) {
            analyzer.rasioTampilan = previewView.width.toFloat() / previewView.height
        }

        calonPenyedia.addListener({
            if (!aktif) return@addListener
            val penyedia = runCatching { calonPenyedia.get() }.getOrElse {
                galat = "Kamera tidak tersedia di HP ini."
                return@addListener
            }
            previewView.doOnLayout {
                if (!aktif) return@doOnLayout
                val pemilihResolusi = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(1280, 720),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                        ),
                    )
                    .build()
                val preview = Preview.Builder().setResolutionSelector(pemilihResolusi).build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val analisisBaru = ImageAnalysis.Builder()
                    .setResolutionSelector(pemilihResolusi)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setOutputImageRotationEnabled(true)
                    .build()
                analisisBaru.setAnalyzer(pelaksana, analyzer)
                analisis = analisisBaru
                val pembuatGrup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(analisisBaru)
                // ViewPort menyamakan area analisis dengan yang terlihat. Bila belum tersedia,
                // kamera tetap dibuka; analyzer memotong tengah sesuai rasio layar sebagai cadangan.
                previewView.viewPort?.let { pembuatGrup.setViewPort(it) }
                val grup = pembuatGrup.build()
                try {
                    penyedia.unbindAll()
                    val k = penyedia.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, grup)
                    kamera = k
                    onPunyaSenter(k.cameraInfo.hasFlashUnit())
                    galat = null
                } catch (e: Exception) {
                    Log.e("PratinjauKamera", "Kamera gagal dibuka", e)
                    galat = "Kamera belakang tidak bisa dibuka."
                }
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            aktif = false
            previewView.removeOnLayoutChangeListener(pemantauUkuran)
            analisis?.clearAnalyzer()
            if (calonPenyedia.isDone) runCatching { calonPenyedia.get().unbindAll() }
            kamera = null
            pelaksana.shutdown()
        }
    }

    LaunchedEffect(kamera, senter) {
        kamera?.let { runCatching { it.cameraControl.enableTorch(senter) } }
    }
    LaunchedEffect(kamera, zoom) {
        kamera?.let { runCatching { it.cameraControl.setLinearZoom(zoom) } }
    }
    LaunchedEffect(kamera) {
        val k = kamera ?: return@LaunchedEffect
        // Fokus & pencahayaan diarahkan ke kotak panduan (layar timbangan).
        delay(700)
        fokusPada(
            previewView,
            k,
            previewView.width * KotakPanduan.PECAHAN.centerX(),
            previewView.height * KotakPanduan.PECAHAN.centerY(),
        )
    }

    Box(
        modifier.pointerInput(kamera) {
            detectTapGestures { posisi -> kamera?.let { fokusPada(previewView, it, posisi.x, posisi.y) } }
        },
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        overlay()
        galat?.let {
            Text(
                it,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
            )
        }
    }
}

private fun fokusPada(previewView: PreviewView, kamera: Camera, x: Float, y: Float) {
    runCatching {
        val titik = previewView.meteringPointFactory.createPoint(x, y)
        val aksi = FocusMeteringAction.Builder(titik, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .disableAutoCancel()
            .build()
        kamera.cameraControl.startFocusAndMetering(aksi)
    }
}

@Composable
private fun OverlayPanduan(status: StatusStabil, modifier: Modifier = Modifier) {
    val warna = when (status) {
        is StatusStabil.Stabil -> WarnaStatus.Aman
        is StatusStabil.Membaca -> Color(0xFFFFC107)
        StatusStabil.Mencari -> Color.White
    }
    val p = KotakPanduan.PECAHAN
    BoxWithConstraints(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val kiriAtas = Offset(p.left * size.width, p.top * size.height)
            val ukuran = UkuranGambar(p.width() * size.width, p.height() * size.height)
            val sudut = CornerRadius(16.dp.toPx())
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = kiriAtas,
                size = ukuran,
                cornerRadius = sudut,
                style = Stroke(width = 9.dp.toPx()),
            )
            drawRoundRect(
                color = warna,
                topLeft = kiriAtas,
                size = ukuran,
                cornerRadius = sudut,
                style = Stroke(width = 4.dp.toPx()),
            )
        }
        Text(
            "Sampah di atas timbangan ikut terfoto sebagai bukti",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Text(
            "Arahkan layar timbangan ke dalam kotak",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = maxHeight * p.top - 40.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun PanelStatus(
    ui: TimbangUi,
    zoom: Float,
    onZoom: (Float) -> Unit,
    onKunci: () -> Unit,
    onKetikManual: () -> Unit,
) {
    val status = ui.status
    val angka = when (status) {
        is StatusStabil.Membaca -> FormatBerat.teks(status.gram)
        is StatusStabil.Stabil -> FormatBerat.teks(status.gram)
        StatusStabil.Mencari -> "– – –"
    }
    val keterangan = when (status) {
        is StatusStabil.Stabil -> "Stabil, mengunci…"
        is StatusStabil.Membaca -> "Membaca… tahan HP tetap diam"
        StatusStabil.Mencari -> petunjuk(ui.hasilBaca)
    }
    val progres = when (status) {
        is StatusStabil.Stabil -> 1f
        is StatusStabil.Membaca -> status.progres
        StatusStabil.Mencari -> 0f
    }
    Surface(tonalElevation = 2.dp, shadowElevation = 8.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(angka, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(
                keterangan,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(progress = { progres }, modifier = Modifier.fillMaxWidth())
            BarisIsiSampah(ui.isi)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Zoom", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(12.dp))
                Slider(value = zoom, onValueChange = onZoom, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onKetikManual, modifier = Modifier.weight(1f)) {
                    Text("Ketik manual")
                }
                Button(
                    onClick = onKunci,
                    enabled = status !is StatusStabil.Mencari,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Kunci angka")
                }
            }
        }
    }
}

private fun petunjuk(hasil: HasilBaca?): String = when (hasil) {
    is HasilBaca.TerlaluRingan ->
        "Timbangan menunjukkan ${FormatBerat.teks(hasil.gram)}. Taruh sampah di atas timbangan."
    is HasilBaca.MelebihiKapasitas ->
        "Terbaca ${FormatBerat.teks(hasil.gram)}, melebihi kapasitas. Cek satuan di pengaturan timbangan."
    else -> "Belum ada angka terbaca. Dekatkan HP, hindari silau, dan pastikan angka masuk kotak."
}

/** Panel diagnosis: menampilkan apa yang benar-benar dibaca kamera (berguna untuk menyetel). */
@Composable
private fun PanelDetail(ui: TimbangUi, profil: ProfilTimbangan, modifier: Modifier = Modifier) {
    Column(
        modifier
            .widthIn(max = 320.dp)
            .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Yang dibaca kamera:", color = Color.White, style = MaterialTheme.typography.labelMedium)
        ui.potonganOcr?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Potongan kotak panduan",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 90.dp),
            )
        }
        val teks = ui.hasilBaca?.teks?.ifBlank { "(kosong)" } ?: "-"
        Text("Teks OCR: $teks", color = Color.White, style = MaterialTheme.typography.bodySmall)
        val label = ui.labelMentah.sortedByDescending { it.keyakinan }.take(5)
            .joinToString { "${it.nama} ${(it.keyakinan * 100).roundToInt()}%" }
            .ifBlank { "-" }
        Text("Label gambar: $label", color = Color.White, style = MaterialTheme.typography.bodySmall)
        Text(
            "Profil: ${profil.satuan.judul}, maks ${FormatBerat.teks(profil.kapasitasGram)}" +
                if (profil.layarLed) ", layar LED" else "",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PermintaanIzinKamera(
    pernahDitolak: Boolean,
    onMinta: () -> Unit,
    onBukaPengaturan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Izin kamera diperlukan", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Kamera dipakai untuk membaca angka di layar timbangan dan memotret bukti setoran. " +
                "Foto disimpan di HP ini.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onMinta) { Text("Izinkan kamera") }
        if (pernahDitolak) {
            TextButton(onClick = onBukaPengaturan) { Text("Buka pengaturan aplikasi") }
        }
    }
}
