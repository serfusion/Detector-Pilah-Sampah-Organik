# Rangkuman Diskusi Awal

Sumbernya adalah percakapan ChatGPT berjudul *Pengolahan Sampah Organik* (mode suara, 11–12 November 2026 WIB). Link share-nya disimpan oleh pemilik proyek dan sengaja tidak dicantumkan di sini karena repo ini publik.

## 1. Titik awal: inisiatif warga

- Lokasinya Kampung ProKlim (Program Kampung Iklim) di salah satu RW Perumahan Sekneg, Tangerang.
- Warga sudah mengompos sampah organik rumah tangga dengan komposter dari **galon bekas air mineral 15 liter**. Galon dipotong dua, bagian atasnya dibalik dengan leher menghadap ke bawah, lalu diberi kasa berlubang. Air lindi menetes ke bagian bawah.
- Gagasannya adalah menjadikan kegiatan ini **gerakan** pengolahan sampah dari rumah tangga.

## 2. GASUKOM dan KGM-19

- **GASUKOM** (*Gerakan Satu Rumah Satu Komposter*) adalah nama gerakan/program.
- **KGM-19** (*Komposter Galon Modular*) adalah nama model komposter percontohan. Angka "19" hanya nama model, **bukan** kapasitas 19 liter. Kapasitas sebenarnya harus diukur dari prototipe.
- Desain yang disepakati dipilih karena paling mudah dibuat warga:
  - Satu galon 15 L dibelah dua. Bagian atas dibalik menjadi ruang kompos, bagian bawah menjadi penampung lindi.
  - Kasa/saringan dipasang di leher galon dan bisa dilepas untuk dibersihkan. Keran lindi ada di bagian bawah, ditambah tutup atas dan ventilasi bersaringan.
  - Target awal yang masih perlu diuji: ruang kompos ±10–12 L, penampung lindi ±2–3 L, 12–16 lubang aerasi berdiameter 8–10 mm, dan biaya Rp30–50 ribu per unit di luar tenaga kerja.
- Catatan keselamatan:
  - Tepi potongan dihaluskan.
  - PET tidak boleh dipanaskan.
  - Lindi jangan langsung dipakai sebagai pupuk sebelum diuji.
  - Buat 10 unit percontohan dulu sebelum produksi lebih banyak.

## 3. Arah program

GASUKOM dikembangkan menjadi rencana proposal penelitian dan pengabdian tim UMT bersama calon mitra industri. Program ini punya tiga luaran yang saling terkait:

1. **KGM-19**: komposter sebagai teknologi tepat guna.
2. **GASUKOM Digital**: aplikasi pencatatan, AI, dan gamifikasi. **Bagian inilah yang dikerjakan di repo ini.**
3. **GASUKOM Community**: kompetisi positif, apresiasi, dan kegiatan warga.

Rencana uji coba (pilot) melibatkan **50 rumah tangga selama 6 bulan**, dengan target minimal 40 rumah tangga aktif. Metodenya R&D dan *Participatory Action Research*: warga ikut merancang dan menguji.

> Rincian anggaran (RAB), pembagian peran mitra, dan strategi proposal **sengaja tidak dimasukkan** karena repo ini publik. Rinciannya ada di percakapan asli dan file Excel/PPT milik pemilik proyek (lihat bagian 8).

## 4. Konsep GASUKOM Digital dari diskusi

Ide awal pemilik proyek: mirip aplikasi kesehatan yang memotret makanan lalu langsung tahu kalorinya, tetapi untuk sampah. Aplikasi memotret sampah lalu tahu jenis dan beratnya. Warga mendaftar dan setorannya tercatat. Warga lalu berlomba, dan yang paling rajin mendapat apresiasi.

Fitur yang dibahas:

| Fitur | Keterangan |
|---|---|
| Registrasi | Nama, RT/RW, identitas rumah tangga |
| Scan QR | Identitas unit komposter, rumah tangga, atau titik timbang |
| AI Waste Scanner | Mengenali jenis sampah organik dari foto |
| Berat | Diambil dari timbangan (lihat poin penting di bawah) |
| Waste Tracker | Riwayat harian, mingguan, bulanan |
| Eco Points | Poin dari setoran yang terverifikasi |
| Leaderboard | Rumah tangga, RT, RW, kampung |
| Eco Challenge | Tantangan berkala |
| Dashboard | Untuk tim peneliti dan kader: partisipasi, total kg, komposisi sampah |

**Poin penting yang disepakati:** berat **tidak boleh** ditebak AI dari foto saja. Berat diambil dari timbangan, sedangkan AI hanya mengenali jenis sampah. Karena itu pemilik proyek mengusulkan agar sampah difoto **di atas timbangan elektronik**, sehingga angka timbangan ikut terbaca dan bisa divalidasi aplikasi.

Ada dua opsi timbangan yang dibahas:

- **A. Timbangan Bluetooth** (load cell + HX711 + ESP32 BLE): angka dikirim langsung ke aplikasi. ChatGPT menyarankan opsi ini sebagai sumber utama.
- **B. Timbangan digital biasa + kamera (OCR)**: aplikasi membaca angka di layar timbangan. Opsi ini lebih murah, tetapi bisa terganggu pantulan cahaya dan sudut kamera.

Untuk APK tahap pertama dipilih **opsi B**, sesuai permintaan awal agar kamera membaca angka di timbangan, dengan tambahan pengaman. Opsi A menjadi peningkatan di tahap berikutnya. Alasannya ada di [Rancangan Aplikasi, bagian 2](02-rancangan-aplikasi.md#2-keputusan-desain).

Usulan sistem poin:

| Aktivitas | Poin |
|---|---:|
| Setiap 100 g sampah organik terverifikasi | 10 |
| Setor 7 hari berturut-turut | 50 |
| Ikut edukasi pengomposan | 30 |
| Menyelesaikan tantangan bulanan | 100 |
| Mengajak 1 rumah tangga baru yang aktif | 50 |

Bentuk apresiasi yang dibahas: sertifikat rumah tangga peduli lingkungan, hadiah perlengkapan komposter, penghargaan RT/RW teraktif, dan penghargaan Kampung Sirkular. Penilaian sebaiknya juga melihat **konsistensi**, bukan hanya jumlah kilogram.

## 5. Dua model lapangan dan hybrid

Pemilik proyek menilai satu timbangan per rumah agak ribet, jadi disiapkan dua alternatif:

| Aspek | Alt. 1: Individu | Alt. 2: Komunal (*Community Hub*) |
|---|---|---|
| Tempat timbang | Di rumah | Satu titik di RW (saung/balai/taman) |
| Komposter | KGM-19 per rumah | Komposter komunal, KGM-19 opsional |
| Timbangan | Per rumah atau per beberapa rumah | Satu timbangan bersama |
| Aplikasi | Di HP tiap warga | Akun tiap warga, ditimbang di titik bersama |
| Kelebihan | Mandiri, data per rumah | Hemat alat, mudah diawasi, jadi ruang silaturahmi |
| Tantangan | Butuh lebih banyak alat | Perlu pengelola, jadwal, dan antrean |

Diskusi merekomendasikan model **hybrid**: Community Hub menjadi pusat kegiatan, sementara warga tetap boleh memakai fasilitas sendiri di rumah. Titik timbang bersama sekaligus menjadi ruang silaturahmi, misalnya untuk hari setor bersama, edukasi, dan gotong royong.

Karena itu APK harus mendukung **dua mode**: *mode individu* untuk HP sendiri dan *mode hub* untuk satu HP yang dipakai banyak warga.

## 6. Target indikator pilot

| Indikator | Target awal |
|---|---|
| Rumah tangga terdaftar / aktif | 50 / ≥40 |
| Kesesuaian berat tercatat dengan timbangan acuan | ≥95% (toleransi dalam gram ditetapkan sebelum uji) |
| Ketepatan AI mengenali jenis sampah | ≥85% pada data uji terpisah (dilengkapi precision/recall) |
| Data perilaku | Konsistensi setor dari data aplikasi dan observasi |

Semua angka di atas adalah target uji, bukan capaian.

## 7. Prinsip data dan privasi yang disepakati

- Data dikumpulkan dengan persetujuan pengguna, dan hanya yang benar-benar perlu.
- Leaderboard publik memakai **nama samaran atau nama kelompok**, tanpa alamat rumah.
- Hak akses dibedakan antara warga, kader/pengelola, dan peneliti.
- Pemakaian foto untuk melatih AI harus dijelaskan dan mendapat izin.
- Pengguna di bawah umur memerlukan persetujuan wali.

## 8. File hasil ChatGPT yang tidak ikut di link share

File berikut dibuat di ChatGPT dan hanya bisa diunduh dari percakapan aslinya:

- RAB awal dan RAB lengkap pilot 50 rumah tangga (Excel, beberapa versi)
- Estimasi biaya pengembangan aplikasi (Excel)
- Infografik Alternatif 1 (Individu) dan Alternatif 2 (Komunal) (PNG)
- Presentasi kemitraan 12 slide (PPTX)
- Gambar konsep KGM-19 dan infografik ekosistem

Kalau ingin disimpan bersama proyek, gambar dan infografik bisa diunggah ke `docs/lampiran/`. File yang memuat anggaran sebaiknya disimpan di tempat privat.

> Catatan: estimasi biaya aplikasi di ChatGPT mengasumsikan aplikasi dibuat oleh vendor IT. Kalau APK dibangun sendiri lewat repo ini, pos biaya aplikasi di proposal bisa dihitung ulang. Yang tersisa terutama server, perangkat uji, dan pemeliharaan.
