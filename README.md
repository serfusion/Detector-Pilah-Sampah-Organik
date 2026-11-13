# Detektor Pilah Sampah Organik — GASUKOM Digital

Aplikasi Android (APK) untuk mencatat setoran sampah organik warga. Sampah ditaruh di atas timbangan, lalu kamera HP membaca angka di layar timbangan sekaligus mengenali isi sampahnya. Setiap setoran menghasilkan poin yang masuk ke leaderboard rumah tangga, RT, RW, dan kampung, sehingga warga bisa berlomba merebut peringkat 1.

> **Status per 13 November 2026:** versi uji coba (tahap M1). Kamera sudah membaca angka timbangan, mengunci saat stabil, mengenali isi sampah, dan menampilkan konfirmasi dengan foto bukti. Fitur simpan setoran, poin, dan leaderboard menyusul. Lihat [Rencana Kerja](docs/03-rencana-kerja.md).

## Unduh APK uji coba

**[gasukom-digital.apk](https://github.com/serfusion/Detector-Pilah-Sampah-Organik/releases/download/build-terbaru/gasukom-digital.apk)** (±22 MB, Android 7.0 ke atas)

APK ini dibangun otomatis dari kode terbaru. Cara memasang dan catatan uji ada di [halaman rilis](https://github.com/serfusion/Detector-Pilah-Sampah-Organik/releases/tag/build-terbaru), sedangkan cara menguji ada di [Rencana Kerja](docs/03-rencana-kerja.md#cara-menguji-apk-untuk-menuntaskan-m1).

## Latar singkat

Warga Kampung ProKlim di Perumahan Sekneg, Tangerang, sudah mengolah sampah organik rumah tangga dengan komposter buatan sendiri. Komposternya dibuat dari satu galon bekas 15 liter yang dipotong dua, lalu bagian atasnya dibalik. Inisiatif ini dikembangkan menjadi gerakan **GASUKOM (Gerakan Satu Rumah Satu Komposter)** dengan komposter percontohan **KGM-19**. **GASUKOM Digital** adalah aplikasinya: mencatat setoran, memberi poin, dan membuat warga makin semangat.

## Alur singkat

1. Sampah organik ditaruh di atas timbangan. Berat wadah sudah dinolkan dengan tombol tara.
2. Warga membuka aplikasi, kamera langsung menyala.
3. Aplikasi membaca angka timbangan dan menguncinya saat angka sudah stabil. Pada saat yang sama, isi sampah dikenali dan aplikasi memberi peringatan bila terlihat sampah non-organik.
4. Foto disimpan sebagai bukti, lalu warga mengonfirmasi.
5. Poin bertambah (usulan: 10 poin per 100 g) dan leaderboard diperbarui. *(menyusul di tahap M2–M3)*

Aplikasi bisa dipakai di **HP warga masing-masing** atau di **satu HP di titik timbang bersama** (saung/balai RW) yang dipakai bergantian oleh banyak warga.

## Dokumen

| Dokumen | Isi |
|---|---|
| [Rangkuman](docs/01-rangkuman.md) | Ringkasan diskusi 11–12 Nov 2026: latar, KGM-19, konsep GASUKOM Digital, model individu & komunal |
| [Rancangan Aplikasi](docs/02-rancangan-aplikasi.md) | Rancangan teknis APK: layar, baca timbangan, deteksi sampah, poin, leaderboard, data, anti-curang |
| [Rencana Kerja](docs/03-rencana-kerja.md) | Status tahap, cara menguji APK, bahan yang perlu disiapkan, keputusan yang belum diambil |

## Teknologi

Android native (Kotlin + Jetpack Compose), CameraX, ML Kit on-device untuk membaca angka dan mengenali gambar, dan DataStore untuk pengaturan. Berikutnya Room untuk data lokal/offline dan Supabase paket gratis untuk akun, data online, dan leaderboard bersama.

Untuk membangun sendiri: pasang JDK 17+ dan Android SDK (platform 36), lalu jalankan `./gradlew testDebugUnitTest assembleRelease`. APK ada di `app/build/outputs/apk/release/`.
