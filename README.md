# Detektor Pilah Sampah Organik — GASUKOM Digital

Rancangan aplikasi Android (APK) untuk mencatat setoran sampah organik warga. Sampah ditaruh di atas timbangan, lalu kamera HP membaca angka di layar timbangan sekaligus mengenali isi sampahnya. Setiap setoran menghasilkan poin yang masuk ke leaderboard rumah tangga, RT, RW, dan kampung, sehingga warga bisa berlomba merebut peringkat 1.

> **Status per 13 November 2026:** tahap rancangan, belum ada kode. Pembangunan APK dimulai di sesi berikutnya dari tahap **M0** di [Rencana Kerja](docs/03-rencana-kerja.md).

## Latar singkat

Warga Kampung ProKlim di Perumahan Sekneg, Tangerang, sudah mengolah sampah organik rumah tangga dengan komposter buatan sendiri. Komposternya dibuat dari satu galon bekas 15 liter yang dipotong dua, lalu bagian atasnya dibalik. Inisiatif ini dikembangkan menjadi gerakan **GASUKOM (Gerakan Satu Rumah Satu Komposter)** dengan komposter percontohan **KGM-19**. **GASUKOM Digital** adalah aplikasinya: mencatat setoran, memberi poin, dan membuat warga makin semangat.

## Alur singkat

1. Sampah organik ditaruh di atas timbangan. Berat wadah sudah dinolkan dengan tombol tara.
2. Warga membuka aplikasi, kamera langsung menyala.
3. Aplikasi membaca angka timbangan dan menguncinya saat angka sudah stabil. Pada saat yang sama, isi sampah dikenali dan aplikasi memberi peringatan bila terlihat plastik atau sampah non-organik.
4. Foto disimpan sebagai bukti, lalu warga mengonfirmasi.
5. Poin bertambah (usulan: 10 poin per 100 g) dan leaderboard diperbarui.

Aplikasi bisa dipakai di **HP warga masing-masing** atau di **satu HP di titik timbang bersama** (saung/balai RW) yang dipakai bergantian oleh banyak warga.

## Dokumen

| Dokumen | Isi |
|---|---|
| [Rangkuman](docs/01-rangkuman.md) | Ringkasan diskusi 11–12 Nov 2026: latar, KGM-19, konsep GASUKOM Digital, model individu & komunal |
| [Rancangan Aplikasi](docs/02-rancangan-aplikasi.md) | Rancangan teknis APK: layar, baca timbangan, deteksi sampah, poin, leaderboard, data, anti-curang |
| [Rencana Kerja](docs/03-rencana-kerja.md) | Urutan pembangunan, bahan yang perlu disiapkan, keputusan yang belum diambil |

## Rencana teknologi

Android native (Kotlin + Jetpack Compose), CameraX, ML Kit on-device untuk membaca angka dan mengenali gambar, Room untuk data lokal/offline, dan Supabase paket gratis untuk akun, data online, dan leaderboard bersama.
