package io.github.serfusion.gasukom

import android.app.Application

/**
 * Titik awal aplikasi. Objek bersama (repository, dsb.) dibuat di sini secara malas
 * supaya bisa dipakai oleh semua layar tanpa pustaka dependency injection.
 */
class GasukomApplication : Application()
