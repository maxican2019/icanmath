# Katalog slot gambar

Gambar di aplikasi bisa diganti tanpa mengubah kode. Setiap slot adalah satu berkas PNG di:

```
app/src/main/res/drawable-nodpi/
```

## Cara mengganti

1. Siapkan PNG dengan **nama berkas persis sama** dengan kolom pertama di bawah.
2. Timpa berkas lama di folder di atas, lalu build ulang.
3. Untuk kembali ke tampilan bawaan (emoji atau bentuk polos), **hapus berkasnya**.

Slot berstatus **placeholder** berisi kotak berlabel sementara. Slot berstatus **kosong** belum punya berkas dan memakai tampilan bawaan; cukup tambahkan berkas bernama sama.

## Aturan berkas

- PNG dengan latar transparan untuk ikon. Jangan ada dua berkas dengan nama dasar yang sama.
- Nama huruf kecil, angka, dan garis bawah saja. Jangan diubah.
- Ukuran di tabel adalah saran. Rasio sama lebih penting daripada ukuran persis.
- Sisakan margin kecil di tepi gambar agar tidak terpotong.

## Status (31 gambar asli, 4 placeholder, 4 kosong)

| Berkas | Ukuran (px) | Dipakai di | Tanpa gambar tampil sebagai | Status |
|---|---|---|---|---|
| `img_home_bg.png` | 1080 × 1920 | Layar awal: latar penuh (kalau ada, simbol melayang disembunyikan) | Latar luar angkasa buatan kode | kosong (opsional) |
| `img_level_sd.png` | 128 × 128 | Pilih Kelas: tab SD | Emoji 🎒 | kosong (opsional) |
| `img_level_smp.png` | 128 × 128 | Pilih Kelas: tab SMP | Emoji 📚 | kosong (opsional) |
| `img_level_sma.png` | 128 × 128 | Pilih Kelas: tab SMA | Emoji 🎓 | kosong (opsional) |
| `img_menu_soal.png` | 256 × 256 | Dashboard: menu Latihan Soal; header Pilih Kelas dan Pilih Jenis Soal | Emoji 📝 | gambar asli |
| `img_menu_logika.png` | 256 × 256 | Dashboard: menu Latihan Logika; header menu logika | Emoji 🧩 / 🧠 | gambar asli |
| `img_menu_cetak.png` | 256 × 256 | Dashboard: menu Cetak PDF; header Cetak Soal | Emoji 🖨️ | gambar asli |
| `img_menu_medali.png` | 256 × 256 | Dashboard: menu Medali Saya; header Medali Saya | Emoji 🏅 | gambar asli |
| `img_menu_profil.png` | 256 × 256 | Dashboard: Tambah Profil; header Profil Baru | Emoji 👨‍👩‍👧 | gambar asli |
| `img_menu_setting.png` | 256 × 256 | Dashboard: Pengaturan; header Pengaturan | Emoji ⚙️ | gambar asli |
| `img_daily_hero.png` | 256 × 256 | Dashboard: pojok kartu latihan harian | Emoji 🎯 | gambar asli |
| `img_streak.png` | 128 × 128 | Dashboard: lencana streak; badge combo saat latihan | Emoji 🔥 | placeholder |
| `img_rank_1.png` | 128 × 128 | Dashboard: ranking peringkat 1 | Emoji 🥇 | placeholder |
| `img_rank_2.png` | 128 × 128 | Dashboard: ranking peringkat 2 | Emoji 🥈 | placeholder |
| `img_rank_3.png` | 128 × 128 | Dashboard: ranking peringkat 3 | Emoji 🥉 | placeholder |
| `img_mode_random.png` | 192 × 192 | Pilih jenis soal: Soal acak | Emoji 🎲 | gambar asli |
| `img_mode_mcq.png` | 192 × 192 | Pilih jenis soal: Pilihan ganda | Emoji 🎯 | gambar asli |
| `img_mode_story.png` | 192 × 192 | Pilih jenis soal: Soal cerita | Emoji 📖 | gambar asli |
| `img_logic_puzzle.png` | 192 × 192 | Menu logika: Puzzle penjumlahan | Emoji 🧩 | gambar asli |
| `img_logic_compare.png` | 192 × 192 | Menu logika: Lebih besar atau kecil | Emoji ⚖️ | gambar asli |
| `img_logic_group.png` | 192 × 192 | Menu logika: Kelompokkan; header game kelompokkan | Emoji 🍎 | gambar asli |
| `img_logic_other.png` | 192 × 192 | Menu logika: Permainan lain (segera hadir) | Emoji 🎲 | gambar asli |
| `img_fruit_apple.png` | 192 × 192 | Game Kelompokkan: apel | Lingkaran merah huruf A | gambar asli |
| `img_fruit_banana.png` | 192 × 192 | Game Kelompokkan: pisang | Bentuk lonjong kuning huruf P | gambar asli |
| `img_fruit_grape.png` | 192 × 192 | Game Kelompokkan: anggur | Lingkaran ungu huruf G | gambar asli |
| `img_medal_bronze.png` | 256 × 256 | Medali Saya: tingkat Bronze | Lingkaran coklat huruf B | gambar asli |
| `img_medal_silver.png` | 256 × 256 | Medali Saya: tingkat Silver | Lingkaran abu-abu huruf S | gambar asli |
| `img_medal_gold.png` | 256 × 256 | Medali Saya: tingkat Gold | Lingkaran emas huruf G | gambar asli |
| `img_medal_locked.png` | 256 × 256 | Medali Saya: belum didapat | Lingkaran abu-abu tanda - | gambar asli |
| `img_star_on.png` | 192 × 192 | Hasil latihan: bintang diperoleh | Emoji ⭐ | gambar asli |
| `img_star_off.png` | 192 × 192 | Hasil latihan: bintang belum diperoleh | Simbol ☆ | gambar asli |
| `img_feedback_correct.png` | 192 × 192 | Panel jawaban benar | Tidak ada gambar | gambar asli |
| `img_feedback_wrong.png` | 192 × 192 | Panel jawaban salah | Tidak ada gambar | gambar asli |
| `img_avatar_1.png` | 256 × 256 | Avatar profil 1 (Male 1): pilihan di Profil Baru dan Medali Saya | Lingkaran berisi huruf pertama nama | gambar asli |
| `img_avatar_2.png` | 256 × 256 | Avatar profil 2 (Male 2): pilihan di Profil Baru dan Medali Saya | Lingkaran berisi huruf pertama nama | gambar asli |
| `img_avatar_3.png` | 256 × 256 | Avatar profil 3 (Female 1): pilihan di Profil Baru dan Medali Saya | Lingkaran berisi huruf pertama nama | gambar asli |
| `img_avatar_4.png` | 256 × 256 | Avatar profil 4 (Female 2): pilihan di Profil Baru dan Medali Saya | Lingkaran berisi huruf pertama nama | gambar asli |
| `img_avatar_5.png` | 256 × 256 | Avatar profil 5 (Female Hijab 1): pilihan di Profil Baru dan Medali Saya | Lingkaran berisi huruf pertama nama | gambar asli |
| `img_avatar_6.png` | 256 × 256 | Avatar profil 6 (Female Hijab 2): pilihan di Profil Baru dan Medali Saya | Lingkaran berisi huruf pertama nama | gambar asli |

## Gambar yang belum dibuat

Placeholder (sebaiknya diganti):

- `img_streak.png` (128 × 128): Dashboard: lencana streak; badge combo saat latihan
- `img_rank_1.png` (128 × 128): Dashboard: ranking peringkat 1
- `img_rank_2.png` (128 × 128): Dashboard: ranking peringkat 2
- `img_rank_3.png` (128 × 128): Dashboard: ranking peringkat 3

Kosong (opsional, aplikasi tetap tampil baik tanpanya):

- `img_home_bg.png` (1080 × 1920): Layar awal: latar penuh (kalau ada, simbol melayang disembunyikan)
- `img_level_sd.png` (128 × 128): Pilih Kelas: tab SD
- `img_level_smp.png` (128 × 128): Pilih Kelas: tab SMP
- `img_level_sma.png` (128 × 128): Pilih Kelas: tab SMA

## Ikon aplikasi

Ikon aplikasi sudah terpasang sebagai adaptive icon dari `design/source/icon_icandomath.png`
(berkas di `res/mipmap-*/ic_launcher_foreground.png` dan warna latar di `res/values/ic_launcher_background.xml`).
Versi 512 × 512 untuk Google Play ada di `design/play_store_icon_512.png`.

## Menambah slot baru

Tambahkan konstanta di objek `Slot` pada `AppImage.kt`, lalu bungkus tampilan lama dengan
`SlotImage(Slot.NAMA, Modifier.size(...)) { tampilan lama }`.
