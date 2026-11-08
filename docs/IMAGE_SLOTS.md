# Katalog slot gambar

Gambar di aplikasi bisa diganti tanpa mengubah kode. Setiap slot adalah satu berkas PNG di:

```
app/src/main/res/drawable-nodpi/
```

## Cara mengganti

1. Siapkan PNG dengan **nama berkas persis sama** dengan kolom pertama di bawah.
2. Timpa berkas lama di folder di atas, lalu build ulang.
3. Untuk kembali ke tampilan bawaan (emoji atau bentuk polos), **hapus berkasnya**.

Sebagian slot sudah berisi **gambar asli** (lihat kolom terakhir). Sisanya masih **placeholder berlabel** supaya terlihat posisi tiap gambar. Ganti satu per satu sesuai kebutuhan.

Sumber gambar asli ada di `design/source/container_math.png` (sheet 12 ikon). Tiap ikon dipotong dari sheet itu, latarnya dihapus, lalu disimpan sebagai PNG transparan sesuai nama slot.

Slot bertanda **opsional** (latar layar awal dan avatar) belum punya placeholder. Tambahkan berkasnya dengan nama yang tertera dan slot langsung aktif. Avatar sengaja dikosongkan supaya huruf pertama nama tetap terlihat sampai gambar avatar Anda siap.

## Aturan berkas

- Format PNG (JPG atau WebP juga bisa, tetapi nama dasarnya harus sama dan jangan ada dua berkas dengan nama dasar yang sama).
- Nama huruf kecil, angka, dan garis bawah saja. Jangan diubah.
- Gunakan latar transparan untuk ikon. Ukuran di tabel adalah ukuran piksel yang disarankan; rasio sama lebih penting daripada ukuran persis.
- Sisakan margin kecil di tepi gambar agar tidak terpotong.
- Gambar tidak otomatis menjadi teks bantu: kalau gambar memuat tulisan, pastikan terbaca di layar kecil.

## Daftar slot (38)

| Berkas | Ukuran (px) | Dipakai di | Tanpa gambar tampil sebagai | Placeholder |
|---|---|---|---|---|
| `img_home_bg.png` | 1080 × 1920 | Layar awal: latar penuh di belakang semua elemen (opsional) | Gradasi indigo | opsional |
| `img_home_logo.png` | 800 × 200 | Layar awal: logo di bawah gambar utama | Tulisan "icanmath" | ada |
| `img_home_mascot.png` | 600 × 600 | Layar awal: gambar utama di bagian atas | Empat lingkaran bergerak + − × ÷ | ada |
| `img_menu_soal.png` | 256 × 256 | Dashboard: ikon menu Latihan Soal | Emoji 📝 | gambar asli: ikon 4 (dipakai juga untuk menu Latihan Soal) |
| `img_menu_logika.png` | 256 × 256 | Dashboard: ikon menu Latihan Logika | Emoji 🧩 | gambar asli: ikon 7 (latihan logika) |
| `img_menu_cetak.png` | 256 × 256 | Dashboard: ikon menu Cetak PDF | Emoji 🖨️ | ada |
| `img_menu_medali.png` | 256 × 256 | Dashboard: ikon menu Medali Saya | Emoji 🏅 | ada |
| `img_menu_profil.png` | 256 × 256 | Dashboard: ikon menu Tambah Profil | Emoji 👨‍👩‍👧 | ada |
| `img_menu_setting.png` | 256 × 256 | Dashboard: ikon menu Pengaturan | Emoji ⚙️ | ada |
| `img_daily_hero.png` | 256 × 256 | Dashboard: pojok kanan atas kartu latihan harian | Emoji 🎯 | gambar asli: ikon 12 (daily quiz) |
| `img_streak.png` | 128 × 128 | Dashboard: lencana streak; juga lencana combo saat latihan | Emoji 🔥 | ada |
| `img_rank_1.png` | 128 × 128 | Dashboard: ranking peringkat 1 | Emoji 🥇 | ada |
| `img_rank_2.png` | 128 × 128 | Dashboard: ranking peringkat 2 | Emoji 🥈 | ada |
| `img_rank_3.png` | 128 × 128 | Dashboard: ranking peringkat 3 | Emoji 🥉 | ada |
| `img_mode_random.png` | 192 × 192 | Pilih jenis soal: kartu Soal acak | Emoji 🎲 | gambar asli: ikon 4 (random soal) |
| `img_mode_mcq.png` | 192 × 192 | Pilih jenis soal: kartu Pilihan ganda | Emoji 🎯 | gambar asli: ikon 5 (soal fixed) |
| `img_mode_story.png` | 192 × 192 | Pilih jenis soal: kartu Soal cerita | Emoji 📖 | gambar asli: ikon 6 (soal cerita) |
| `img_logic_puzzle.png` | 192 × 192 | Menu Latihan Logika: kartu Puzzle penjumlahan | Emoji 🧩 | ada |
| `img_logic_compare.png` | 192 × 192 | Menu Latihan Logika: kartu Lebih besar atau kecil | Emoji ⚖️ | ada |
| `img_logic_group.png` | 192 × 192 | Menu Latihan Logika: kartu Kelompokkan | Emoji 🍎 | ada |
| `img_logic_other.png` | 192 × 192 | Menu Latihan Logika: kartu Permainan lain | Emoji 🎲 | ada |
| `img_fruit_apple.png` | 192 × 192 | Game Kelompokkan: buah apel | Bentuk merah dengan huruf A | gambar asli: ikon 1 (buah apel) |
| `img_fruit_banana.png` | 192 × 192 | Game Kelompokkan: buah pisang | Bentuk kuning dengan huruf P | gambar asli: ikon 2 (buah pisang) |
| `img_fruit_grape.png` | 192 × 192 | Game Kelompokkan: buah anggur | Bentuk ungu dengan huruf A | gambar asli: ikon 3 (buah anggur) |
| `img_medal_bronze.png` | 256 × 256 | Medali Saya dan daftar medali: tingkat Bronze | Lingkaran coklat huruf B | gambar asli: ikon 8 |
| `img_medal_silver.png` | 256 × 256 | Medali Saya dan daftar medali: tingkat Silver | Lingkaran abu-abu biru huruf S | gambar asli: ikon 9 |
| `img_medal_gold.png` | 256 × 256 | Medali Saya dan daftar medali: tingkat Gold | Lingkaran emas huruf G | gambar asli: ikon 10 |
| `img_medal_locked.png` | 256 × 256 | Medali Saya dan daftar medali: tingkat terkunci (belum didapat) | Lingkaran abu-abu tanda - | gambar asli: ikon 11 (siluet) |
| `img_star_on.png` | 192 × 192 | Hasil latihan: bintang yang diperoleh | Emoji ⭐ | ada |
| `img_star_off.png` | 192 × 192 | Hasil latihan: bintang yang belum diperoleh | Simbol ☆ | ada |
| `img_feedback_correct.png` | 192 × 192 | Panel di bawah layar saat jawaban benar (maskot senang) | Tidak ada gambar | ada |
| `img_feedback_wrong.png` | 192 × 192 | Panel di bawah layar saat jawaban salah (maskot menyemangati) | Tidak ada gambar | ada |
| `img_avatar_1.png` | 256 × 256 | Avatar profil nomor 1 (dipilih otomatis dari nama profil) | Lingkaran berisi huruf pertama nama | opsional |
| `img_avatar_2.png` | 256 × 256 | Avatar profil nomor 2 (dipilih otomatis dari nama profil) | Lingkaran berisi huruf pertama nama | opsional |
| `img_avatar_3.png` | 256 × 256 | Avatar profil nomor 3 (dipilih otomatis dari nama profil) | Lingkaran berisi huruf pertama nama | opsional |
| `img_avatar_4.png` | 256 × 256 | Avatar profil nomor 4 (dipilih otomatis dari nama profil) | Lingkaran berisi huruf pertama nama | opsional |
| `img_avatar_5.png` | 256 × 256 | Avatar profil nomor 5 (dipilih otomatis dari nama profil) | Lingkaran berisi huruf pertama nama | opsional |
| `img_avatar_6.png` | 256 × 256 | Avatar profil nomor 6 (dipilih otomatis dari nama profil) | Lingkaran berisi huruf pertama nama | opsional |

## Ikon aplikasi (launcher)

Ikon di layar utama HP **bukan** slot di atas, karena Android memerlukan format khusus. Di Android Studio: klik kanan `app`, pilih New, Image Asset, lalu pilih Launcher Icons (Adaptive and Legacy) dan unggah gambar Anda. Setelah itu tambahkan `android:icon="@mipmap/ic_launcher"` di `AndroidManifest.xml` pada tag `<application>`.

## Menambah slot baru

Tambahkan konstanta di objek `Slot` pada `AppImage.kt`, lalu bungkus tampilan lama dengan `SlotImage(Slot.NAMA, Modifier.size(...)) { tampilan lama }`.
