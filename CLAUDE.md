# CLAUDE.md

Konteks untuk sesi Claude Code berikutnya di repo ini.

## Proyek

GASUKOM Digital adalah APK Android untuk mencatat setoran sampah organik warga Kampung ProKlim (Perumahan Sekneg, Tangerang). Kamera membaca angka di layar timbangan digital biasa (OCR) dan mengenali isi sampah. Poin masuk ke leaderboard rumah tangga, RT, RW, dan kampung. Ada dua mode: mode individu (HP sendiri) dan mode hub (satu HP di titik timbang bersama, warga dikenali lewat kartu QR).

## Bahasa

Pemilik proyek berkomunikasi dalam Bahasa Indonesia, sering lewat HP. Balas dan tulis dokumentasi dalam Bahasa Indonesia yang sederhana. Teks di dalam aplikasi juga berbahasa Indonesia.

## Status

Rancangan sudah selesai, kode belum ada. Lanjutkan dari tahap pertama yang belum dicentang di `docs/03-rencana-kerja.md`.

## Dokumen acuan

- `docs/01-rangkuman.md`: latar belakang dan hasil diskusi awal
- `docs/02-rancangan-aplikasi.md`: rancangan teknis, menjadi acuan utama untuk fitur dan aturan
- `docs/03-rencana-kerja.md`: tahap M0–M5, bahan dari pemilik proyek, dan keputusan yang belum diambil

## Keputusan yang sudah dikunci

- Android native dengan Kotlin + Jetpack Compose, CameraX, ML Kit on-device (bundled), Room, WorkManager, dan Supabase (supabase-kt).
- Berat diambil dari timbangan: lewat OCR layar di MVP, lewat BLE di tahap lanjut. AI hanya mengenali isi sampah dan tidak menebak berat.
- Berat yang diketik manual selalu menunggu verifikasi kader. Poin hanya diberikan untuk setoran yang sudah terverifikasi.
- Aplikasi harus tetap jalan tanpa internet (Room) dan sinkron saat ada koneksi.

## Aturan kerja

- Repo ini **publik**. Jangan commit kunci Supabase, keystore, data warga asli, atau anggaran proposal. Konfigurasi rahasia disimpan di `local.properties` atau di secret GitHub.
- Pembaca timbangan dan pengenal sampah harus diuji dengan kumpulan foto contoh sebelum dipakai di lapangan.
- Setiap tahap yang selesai dicentang di `docs/03-rencana-kerja.md`.
