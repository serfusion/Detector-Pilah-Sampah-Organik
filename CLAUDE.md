# CLAUDE.md

Konteks untuk sesi Claude Code berikutnya di repo ini.

## Proyek

GASUKOM Digital adalah APK Android untuk mencatat setoran sampah organik warga Kampung ProKlim (Perumahan Sekneg, Tangerang). Kamera membaca angka di layar timbangan digital biasa (OCR) dan mengenali isi sampah. Poin masuk ke leaderboard rumah tangga, RT, RW, dan kampung. Ada dua mode: mode individu (HP sendiri) dan mode hub (satu HP di titik timbang bersama, warga dikenali lewat kartu QR).

## Bahasa

Pemilik proyek berkomunikasi dalam Bahasa Indonesia, sering lewat HP. Balas dan tulis dokumentasi dalam Bahasa Indonesia yang sederhana. Teks di dalam aplikasi, nama kelas/fungsi domain, dan pesan commit juga berbahasa Indonesia.

## Status

M0 selesai. Kode M1 (kamera + OCR + penstabil + deteksi isi + konfirmasi) selesai, tetapi masih menunggu uji dengan timbangan asli oleh pemilik proyek. Lanjutkan dari tahap pertama yang belum dicentang di `docs/03-rencana-kerja.md`. Kalau pemilik proyek membawa foto/screenshot hasil uji, gunakan untuk menyetel `PembacaAngka`, `OlahPiksel`, dan `PemetaLabel`, lalu tambahkan kasusnya ke unit test.

## Dokumen acuan

- `docs/01-rangkuman.md`: latar belakang dan hasil diskusi awal
- `docs/02-rancangan-aplikasi.md`: rancangan teknis, menjadi acuan utama untuk fitur dan aturan
- `docs/03-rencana-kerja.md`: tahap M0–M5, cara menguji APK, bahan dari pemilik proyek, keputusan yang belum diambil

## Struktur kode

- `app/src/main/java/io/github/serfusion/gasukom/`
  - `ui/`: navigasi (`AppNavHost`), tema, beranda
  - `timbang/`: `PembacaAngka` (teks OCR → gram), `Penstabil` (kunci angka stabil), `AnalisisTimbang` (CameraX + ML Kit), `TimbangScreen`, `KonfirmasiScreen`, `PengaturanTimbanganScreen`, `HasilTimbang` (aturan verifikasi)
  - `klasifikasi/`: `PemetaLabel` (label ML Kit → organik/non-organik)
- `app/src/test/`: unit test JVM untuk semua logika murni
- `.github/workflows/build-apk.yml`: unit test + build APK + perbarui pre-release `build-terbaru`

## Keputusan yang sudah dikunci

- Android native dengan Kotlin + Jetpack Compose, CameraX, ML Kit on-device (bundled), Room, WorkManager, dan Supabase (supabase-kt).
- Versi: AGP 8.13.2, Kotlin 2.2.21, Gradle 8.14.3, compileSdk/targetSdk 36, minSdk 24, Java 17. Pustaka AndroidX dipilih dari akhir 2025. Jangan naikkan ke AGP 9 tanpa alasan, karena ada perubahan besar.
- Berat diambil dari timbangan: lewat OCR layar di MVP, lewat BLE di tahap lanjut. AI hanya mengenali isi sampah dan tidak menebak berat.
- Berat yang diketik manual selalu menunggu verifikasi kader. Poin hanya diberikan untuk setoran yang sudah terverifikasi.
- Aplikasi harus tetap jalan tanpa internet (Room) dan sinkron saat ada koneksi.
- APK uji = build *release* (R8) yang ditandatangani kunci debug, sampai secret `GASUKOM_KEYSTORE_*` diatur di GitHub.

## Membangun dan menguji di sesi cloud

- Sesi baru belum punya Android SDK. Pasang dulu:
  1. Unduh `commandlinetools-linux-*_latest.zip` dari `dl.google.com/android/repository/`, lalu ekstrak ke `/opt/android-sdk/cmdline-tools/latest`.
  2. Jalankan `sdkmanager --licenses` dan `sdkmanager "platforms;android-36" "build-tools;36.0.0" "platform-tools"`.
  3. Buat `local.properties` berisi `sdk.dir=/opt/android-sdk`. File ini di-ignore git.
- Perintah build: `gradle testDebugUnitTest assembleRelease` (Gradle 8.14.3 sudah terpasang) atau `./gradlew ...`.
- Maven Central kadang membalas **429** lewat proxy. Cukup ulangi perintahnya; jangan ganti sumber repositori.
- Tidak ada emulator (tanpa KVM). Logika diuji dengan unit test JVM, sedangkan uji di HP dilakukan pemilik proyek lewat APK `build-terbaru`.
- Setelah push, cek hasil workflow dengan tool GitHub `actions_list`/`get_job_logs` dan rilis dengan `get_release_by_tag` (`build-terbaru`).

## Aturan kerja

- Repo ini **publik**. Jangan commit kunci Supabase, keystore, data warga asli, atau anggaran proposal. Konfigurasi rahasia disimpan di `local.properties` atau di secret GitHub.
- Pembaca timbangan dan pengenal sampah harus diuji dengan kumpulan foto contoh sebelum dipakai di lapangan.
- Setiap tahap yang selesai dicentang di `docs/03-rencana-kerja.md`.
