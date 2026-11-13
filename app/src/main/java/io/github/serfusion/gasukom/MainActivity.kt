package io.github.serfusion.gasukom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.serfusion.gasukom.ui.AppNavHost
import io.github.serfusion.gasukom.ui.theme.GasukomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GasukomTheme {
                AppNavHost()
            }
        }
    }
}
