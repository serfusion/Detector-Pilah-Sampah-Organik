package io.github.serfusion.gasukom.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.serfusion.gasukom.ui.beranda.BerandaScreen

/** Nama-nama rute (layar) di aplikasi. */
object Rute {
    const val BERANDA = "beranda"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rute.BERANDA) {
        composable(Rute.BERANDA) {
            BerandaScreen(
                onSetorSampah = {},
                onPengaturanTimbangan = {},
            )
        }
    }
}
