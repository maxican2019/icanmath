# Math Battle

Game kuis beranimasi: pemanah melawan monster laba-laba. Gameplay dipindahkan dari prototipe
https://claude.ai/artifact/RgLjtiB3JSat6R6mNMEcAd (satu berkas HTML) ke Kotlin + Compose. Tidak ada gameplay baru.

Masuk lewat: Latihan Logika, Permainan lain (Rumah Game), Math Battle.

## Aturan

- 10 level, 3 nyawa, skor. Setiap membuka game mulai dari level 1 (progres sesi tidak disimpan, seperti prototipe).
- Setiap level menampilkan satu **soal cerita** dengan 4 pilihan jawaban. Soal selalu diacak.
- **Jawaban benar**: pahlawan menembak atau menebas sampai monster kalah, skor naik `100 × (1 + level ÷ 3)`, lalu undian hadiah.
- **Jawaban salah**: serangan terhenti lebih awal, monster menjatuhkan pahlawan, nyawa berkurang 1. Pembahasan jawaban ditampilkan.
  Nyawa habis: tombol "Mulai ulang" kembali ke level 1.
- Menang di level 10: tombol "Main lagi" kembali ke level 1 (loop farming).

## Level dan jenis soal

| Level | Soal |
|---|---|
| 1 sampai 3 | Sisa serangan yang dibutuhkan (hitung mundur), rentang makin besar |
| 4 | HP monster tersisa setelah ditembak dengan panah berbagai bahan |
| 5 | HP monster tersisa setelah ditebas dengan pedang berbagai bahan |
| 6 | Damage yang diterima pahlawan (ATK dikurangi total DEF armor) |
| 7 | Turn minimal untuk mengalahkan monster |
| 8 | Turn-based: HP pahlawan saat monster kalah |
| 9 | Turn-based boss, monster menyerang 2 kali per turn |
| 10 | Boss Tarantula, monster menyerang 2 sampai 3 kali per turn |

Nilai bahan: panah kayu 1, iron 3, gold 5, rainbow 10. Pedang kayu 1, iron 6, gold 10, rainbow 20.
Armor DEF: kulit 5, kayu 7, iron 14, gold 16, diamond 20.

Monster makin garang seiring level (lebih banyak kaki, duri, bercak, dan tanduk; boss lebih besar). Pahlawan dan monster
tanpa mata, hidung, atau mulut.

## Hadiah

Diundi satu kali saat menang di level 3, 6, 9, dan 10, dan masuk ke Inventory bersama (lihat `INVENTORY.md`).
Tabel ada di `DropTable.default`.

## Peta kode

| Bagian | Berkas |
|---|---|
| Generator 10 level soal, pilihan jawaban | `battle/BattleQuestions.kt` |
| Skenario animasi dari jawaban, sesi (nyawa, skor) | `battle/BattleScript.kt` |
| Tabel drop dan undian | `battle/DropTable.kt`, `battle/BattleRewards.kt` |
| Animasi dan gambar panggung (Canvas) | `ui/BattleStage.kt` |
| Layar permainan | `ui/MathBattleScreen.kt` |
| Tes | `src/test/.../battle/` |
