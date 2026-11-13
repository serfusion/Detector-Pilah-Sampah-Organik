# Rencana Kerja dan Langkah Berikutnya

## Posisi saat ini (13 November 2026)

- ✅ Diskusi konsep sudah dirangkum di [Rangkuman](01-rangkuman.md).
- ✅ Rancangan aplikasi sudah ditulis di [Rancangan Aplikasi](02-rancangan-aplikasi.md).
- ✅ **M0 selesai.** Proyek Android sudah jadi, dan GitHub Actions membangun APK otomatis setiap ada perubahan.
- 🟡 **Kode M1 selesai, tetapi belum diuji dengan timbangan asli.** Kamera sudah membaca angka di kotak panduan, menguncinya saat stabil, mengenali isi sampah, lalu menampilkan layar konfirmasi berisi foto bukti.

**APK uji coba terbaru:** <https://github.com/serfusion/Detector-Pilah-Sampah-Organik/releases/download/build-terbaru/gasukom-digital.apk>
(halaman rilis: <https://github.com/serfusion/Detector-Pilah-Sampah-Organik/releases/tag/build-terbaru>)

## Urutan pembangunan

| Status | Tahap | Isi | Dianggap selesai bila |
|:---:|---|---|---|
| ✅ | **M0: Kerangka** | Proyek Android (Kotlin + Compose), struktur paket, dan GitHub Actions yang membangun APK | APK awal bisa diunduh dari GitHub dan terpasang di HP |
| 🟡 | **M1: Baca timbangan** | Kamera, kotak panduan, OCR, stabilisasi, deteksi isi sampah, layar konfirmasi + foto bukti, pengaturan timbangan | Angka terbaca benar di sebagian besar uji dengan timbangan asli. Kalau belum, diputuskan apakah perlu pembaca 7-segmen khusus atau timbangan Bluetooth |
| ⬜ | **M2: Setoran lokal (mode hub satu HP)** | Simpan setoran (Room), daftar warga dan kartu QR, poin, riwayat, leaderboard lokal, batas setoran harian | Satu HP bisa dipakai bergantian oleh beberapa warga tanpa internet |
| ⬜ | **M3: Online** | Supabase: login, tabel dan RLS, sinkron, unggah foto, fungsi leaderboard | Dua HP yang berbeda menampilkan leaderboard yang sama |
| ⬜ | **M4: Kader** | Antrean verifikasi, setujui/tolak, poin kegiatan, rekap RT/RW, ekspor CSV | Kader bisa mengelola satu RW dari HP |
| ⬜ | **M5: Uji lapangan** | Uji dengan 5–10 rumah tangga, perbaikan, lalu pilot 50 rumah tangga | Masukan warga sudah ditindaklanjuti |
| ⬜ | Lanjutan | Timbangan Bluetooth, dashboard web, model AI khusus, tantangan dan badge | — |

## Cara menguji APK (untuk menuntaskan M1)

1. Buka link APK di atas dari HP Android, unduh, lalu pasang. Langkah lengkapnya ada di halaman rilis.
2. Buka aplikasi, lalu masuk ke **Pengaturan Timbangan**. Pilih satuan yang sesuai dengan layar timbangan (gram, atau kg dengan 1/2/3 angka di belakang koma), lalu pilih kapasitasnya.
3. Kembali ke beranda, ketuk **Setor Sampah**, lalu izinkan kamera.
4. Tekan TARE/ZERO pada timbangan, taruh sampah, lalu arahkan **layar timbangan ke dalam kotak**. Tunggu bilah hijau penuh sampai angka terkunci.
5. Di layar konfirmasi, cek apakah angkanya benar, lalu ketuk **Bagikan hasil uji** dan kirim (misalnya ke WhatsApp sendiri). Isinya foto bukti dan angka yang terbaca.
6. Ulangi 10–20 kali dengan berat dan cahaya yang berbeda. Catat berapa kali angkanya **benar**, **salah**, atau **tidak terkunci**.
7. Kalau angka tidak terbaca, ketuk ikon **ⓘ** di layar kamera. Panel "Yang dibaca kamera" akan menampilkan potongan gambar dan teks hasil OCR. Ambil screenshot layar itu.

Bawa hasil uji tersebut (foto, screenshot, dan catatan benar/salah) ke sesi Claude berikutnya untuk menyetel pembacaan.

> Selama masa uji, setiap build APK bisa memakai tanda tangan yang berbeda. Kalau pemasangan gagal, hapus dulu versi lama lalu pasang lagi.

## Yang perlu disiapkan pemilik proyek

1. **Hasil uji APK dengan timbangan asli** (lihat bagian di atas). Ini paling penting untuk menuntaskan M1. Sertakan juga merek dan tipe timbangannya. Kalau belum punya timbangan, pilih sesuai rekomendasi di bagian 7.4 dokumen rancangan.
2. **Data wilayah pilot**: nama kampung, nomor RW dan RT yang ikut, serta lokasi titik timbang bersama bila ada.
3. **Akun Supabase gratis** (dibutuhkan mulai M3):
   1. Daftar di supabase.com lalu buat *New project*. Pilih region Singapura supaya dekat.
   2. Catat **Project URL** dan **publishable key** (di versi lama disebut *anon key*). Keduanya ada di menu *Project Settings*, bagian API.
   3. **Jangan pernah** membagikan *secret key* (*service_role key*) atau password database di chat maupun di repo.
   4. Skrip pembuatan tabel akan disediakan di repo (folder `supabase/`). Tinggal jalankan di *SQL Editor*.
4. **HP Android untuk uji coba.** Sebutkan tipe dan versi Android-nya.
5. File hasil ChatGPT seperti RAB, infografik, dan PPT (opsional). File yang memuat anggaran sebaiknya disimpan di tempat privat.

## Keputusan yang belum diambil

| Topik | Pilihan | Usulan |
|---|---|---|
| Nama aplikasi | GASUKOM Digital atau nama lain | GASUKOM Digital |
| Cara login warga | (a) didaftarkan kader + kartu QR, (b) email dan password, (c) akun Google | (a) untuk hub, (b) atau (c) untuk mode individu |
| Dasar peringkat RT/RW | Total poin atau rata-rata per rumah tangga | Rata-rata, dengan total tetap ditampilkan |
| Nilai poin dan bonus | Lihat bagian 9 dokumen rancangan | Pakai usulan dulu, dievaluasi setelah 1 bulan |
| Batas verifikasi otomatis | Berat maksimal per setoran, jumlah setoran per hari | 3 kg dan 3 kali per hari |
| Penyimpanan foto | Disimpan semua, atau dihapus setelah beberapa bulan | Simpan *thumbnail*, foto asli dihapus setelah 3 bulan |
| Kunci tanda tangan APK | Tetap kunci debug, atau kunci rilis di Secrets GitHub | Kunci rilis sebelum APK dibagikan ke warga (paling lambat M5) |
| Visibilitas repo | Publik atau privat | Kode boleh publik, anggaran dan proposal disimpan privat |

## Cara melanjutkan di sesi Claude berikutnya

Buka sesi baru pada repo ini lalu ketik, misalnya:

> Lanjutkan pembangunan APK sesuai docs/03-rencana-kerja.md.

Kalau sudah ada hasil uji timbangan, lampirkan foto/screenshot-nya di pesan yang sama. Claude akan membaca `CLAUDE.md` dan dokumen-dokumen ini, lalu meneruskan dari tahap yang belum selesai. Setiap tahap yang selesai dicentang di tabel di atas.
