# Agra Finance — Android

Aplikasi pencatat keuangan pribadi dengan tema gelap, kartu saldo gradasi, aksen warna, grafik, dan navigasi bawah; dibuat menggunakan Kotlin + Jetpack Compose.

## Fitur
- Data awal pemasukan: Rp750.000 dari ayah dan Rp400.000 dari ibu.
- Pengingat bahwa uang ibu diperuntukkan bagi makan dan kebutuhan sehari-hari.
- Tambah pemasukan/pengeluaran dengan nominal, kategori, catatan, dan tanggal.
- Saldo dan total pemasukan/pengeluaran otomatis.
- Grafik batang arus kas.
- Anggaran kategori dengan indikator penggunaan dan peringatan saat kategori pengeluaran mencapai 80%.
- Riwayat transaksi, filter tanggal, dan hapus transaksi.
- Data transaksi disimpan lokal di SharedPreferences, sehingga tetap ada setelah aplikasi ditutup.

## Cara menjalankan
1. Instal **Android Studio** di komputer.
2. Ekstrak ZIP ini.
3. Android Studio → **Open** → pilih folder `AgraFinance`.
4. Tunggu Gradle Sync selesai. Pastikan JDK 17 tersedia dan Android SDK Platform 35 terpasang.
5. Jalankan lewat emulator atau sambungkan HP Android dengan USB debugging aktif, lalu tekan **Run ▶**.

## Catatan
- Ini adalah source project Android Studio, bukan APK yang sudah dikompilasi.
- Aplikasi menambahkan dua transaksi pemasukan awal hanya saat pertama kali dijalankan. Jika menghapus data aplikasi, data transaksi lokal ikut terhapus.
- Anggaran awal berjumlah Rp1.150.000. Pengeluaran yang dicatat mengurangi saldo dan pemakaian anggaran kategori. Pemindahan uang ke tabungan/dana darurat/cadangan dicatat sebagai transaksi kategori terkait jika ingin memantau realisasinya.
- Untuk saat ini, aplikasi tidak menghubungkan transaksi ke rekening bank dan tidak melakukan sinkronisasi cloud.


## Build APK otomatis lewat GitHub Actions
Jika tidak punya Android Studio:
1. Buat repository baru di GitHub, lalu upload seluruh isi folder proyek ini.
2. Buka tab **Actions** pada repository.
3. Pilih workflow **Build Android APK**, lalu tekan **Run workflow** (atau push ke branch main/master).
4. Setelah job selesai, buka hasil run dan unduh artifact **AgraFinance-debug-apk**.
5. Ekstrak ZIP artifact untuk mendapatkan `app-debug.apk`, lalu pindahkan ke HP dan instal. Android mungkin meminta izin instal aplikasi dari sumber tersebut.

APK debug ditujukan untuk uji coba pribadi, bukan rilis Play Store.
