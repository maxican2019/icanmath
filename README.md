# icanmath

Aplikasi Android latihan matematika untuk kelas 1 SD sampai 12 SMA. Tanpa login: data tersimpan lokal di perangkat, bisa dicadangkan ke file dan dipulihkan di perangkat lain.

> Status: MVP awal (v0.1.0). Kode ditulis tanpa Android SDK di lingkungan penulis, jadi **belum pernah dikompilasi atau dijalankan**. Kemungkinan ada galat kompilasi kecil di build pertama. Lihat bagian "Build pertama".

## Fitur

- Halaman awal bertema luar angkasa (pelangi, bintang berkelip, simbol matematika melayang) dengan tombol Masuk, Setting, dan Pulihkan.
- Dashboard dengan menu beridentitas ikon, kartu latihan harian, ranking, dan ganti profil lewat klik nama di bagian atas.
- Profil tanpa akun (nama, umur, kelas, avatar); mendukung lebih dari satu profil.
- Dashboard: streak, latihan harian (5 soal campuran, seed tanggal), ranking mingguan lokal antar profil.
- Pilih jenjang dan kelas (SD 1-6, SMP 7-9, SMA 10-12), tiga jenis soal:
  - Random: dibuat baru tiap mulai, jawaban dihitung kode.
  - Pilihan ganda tetap: bank soal di `assets/questions_mcq.json`.
  - Soal cerita tetap (hanya SD 1-6; di SMP dan SMA menunya disembunyikan): `assets/questions_story.json`.
- Submenu ramah anak: header berwarna, kartu besar dengan gambar, kotak kelas bergaya level game, animasi muncul dan membal saat ditekan.
- Latihan bergaya game: kartu jawaban beranimasi, combo, poin, dan panel di bawah layar yang menjelaskan cara menghitung serta istilah (misalnya arti KPK) setelah setiap jawaban.
- Hasil latihan dengan bintang dan bagian "Pelajari lagi" untuk soal yang salah.
- Setelah latihan: bila ada lebih dari satu profil, pilih lanjut sebagai profil mana (default profil yang sama).
- Latihan logika: puzzle penjumlahan, lebih besar atau kecil, kelompokkan buah.
- Medali Bronze/Silver/Gold: waktu, nilai, daily sukses, daily non-stop, dan medali logika yang terpisah.
- Cetak soal ke PDF A4 (dengan kunci jawaban opsional), dibagikan lewat share sheet Android.
- Cadangan dan pulihkan data lewat file JSON (simpan, bagikan, impor).

## Game dan inventory

Latihan Logika, Permainan lain, membuka **Rumah Game** yang berisi Math Battle (dan ruang untuk game lain nanti). Hasil farming disimpan di modul **Inventory bersama** yang dipakai semua game; lihat `docs/INVENTORY.md` untuk API, contoh pemakaian, tabel drop, dan cara menjalankan tes.

## Mengganti gambar

Semua gambar di aplikasi adalah slot bernama yang bisa ditimpa dengan PNG tanpa mengubah kode. Lihat `docs/IMAGE_SLOTS.md` untuk daftar, ukuran, dan caranya. Sumber desain ada di `design/source/`.

## Build pertama

1. Buka folder ini di Android Studio (versi terbaru yang mendukung AGP 8.13) dengan JDK 17 atau lebih baru.
2. Tunggu Gradle sync. Folder ini belum berisi Gradle Wrapper; Android Studio akan memakai Gradle yang sesuai. Untuk membuat wrapper: `gradle wrapper --gradle-version 8.13`.
3. Jalankan di emulator atau perangkat (minSdk 26).
4. Unit test: `./gradlew testDebugUnitTest`.

Versi library ada di `gradle/libs.versions.toml`. Versi sengaja konservatif (AGP 8.13.2, Kotlin 2.3.0, Compose 1.9.4, Material3 1.4.0). Versi lebih baru sudah tersedia; naikkan setelah build pertama berhasil.

## Struktur

```
app/src/main/java/com/icanmath/app/
  Models.kt            data class (profil, sesi, soal)
  Repository.kt        penyimpanan JSON + ekspor/impor cadangan
  QuestionGenerator.kt generator soal per kelas
  QuestionBank.kt      pembaca bank soal tetap
  Medals.kt            statistik, streak, ranking, medali
  AppViewModel.kt      state dan aksi aplikasi
  Pdf.kt               ekspor PDF
  ui/                  layar Compose dan navigasi
app/src/main/assets/   bank soal tetap (JSON)
```

## Keputusan teknis

- Penyimpanan: satu file JSON, bukan Room, supaya cadangan sama dengan data. Migrasi ke Room bisa dilakukan nanti.
- Ranking: lokal antar profil di satu perangkat. Hanya latihan di kelas profil yang dihitung ke ranking dan medali.
- Medali waktu mensyaratkan akurasi minimal 80%.
- File cadangan tidak dienkripsi dan bisa diedit; jangan dipakai untuk ranking online tanpa validasi server.
- Android Auto Backup aktif (`allowBackup`), jadi data ikut terpindah saat ganti HP dengan akun Google yang sama.

## Belum ada (roadmap)

- Rapor mingguan, bulanan, tahunan (PDF) dan kartu bagikan medali.
- Permainan logika lain (pola, timbangan).
- Bank soal tetap yang besar (sekarang hanya contoh kecil; kelas tanpa bank memakai soal acak).
- Pembahasan langkah, ikon aplikasi, mode gelap yang dipoles, suara dan getar, pembacaan soal (TTS) untuk kelas 1-3.
- Tes UI dan tes lebih banyak untuk medali dan streak.

## Catatan konten

Tulis soal orisinal. Jangan menyalin soal dari buku pelajaran atau ujian karena berhak cipta. Pilih lisensi (misalnya MIT) dan tambahkan file `LICENSE` sebelum repositori dipublikasikan.
