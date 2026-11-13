package io.github.serfusion.gasukom.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.serfusion.gasukom.timbang.KonfirmasiScreen
import io.github.serfusion.gasukom.timbang.PengaturanTimbanganScreen
import io.github.serfusion.gasukom.timbang.SesiTimbangViewModel
import io.github.serfusion.gasukom.timbang.TimbangScreen
import io.github.serfusion.gasukom.ui.beranda.BerandaScreen

/** Nama-nama rute (layar) di aplikasi. */
object Rute {
    const val BERANDA = "beranda"
    const val TIMBANG = "timbang"
    const val KONFIRMASI = "konfirmasi"
    const val PENGATURAN_TIMBANGAN = "pengaturan-timbangan"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    // Dibagi antara layar kamera dan layar konfirmasi (dimiliki activity).
    val sesi: SesiTimbangViewModel = viewModel()

    NavHost(navController = navController, startDestination = Rute.BERANDA) {
        composable(Rute.BERANDA) {
            BerandaScreen(
                onSetorSampah = { navController.navigate(Rute.TIMBANG) },
                onPengaturanTimbangan = { navController.navigate(Rute.PENGATURAN_TIMBANGAN) },
            )
        }
        composable(Rute.TIMBANG) {
            TimbangScreen(
                sesi = sesi,
                onTerkunci = {
                    navController.navigate(Rute.KONFIRMASI) { launchSingleTop = true }
                },
                onKembali = { navController.popBackStack() },
                onPengaturan = { navController.navigate(Rute.PENGATURAN_TIMBANGAN) },
            )
        }
        composable(Rute.KONFIRMASI) {
            KonfirmasiScreen(
                sesi = sesi,
                onUlangi = { navController.popBackStack() },
                onSelesai = {
                    sesi.atur(null)
                    navController.popBackStack(Rute.BERANDA, inclusive = false)
                },
            )
        }
        composable(Rute.PENGATURAN_TIMBANGAN) {
            PengaturanTimbanganScreen(onKembali = { navController.popBackStack() })
        }
    }
}
