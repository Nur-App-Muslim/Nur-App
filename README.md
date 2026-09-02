# NurApp

Aplikasi Android untuk jadwal sholat, notifikasi adzan, membaca Al-Qur'an, dan penunjuk arah kiblat secara offline. Dibuat dengan Kotlin dan Jetpack Compose.

---

## Tangkapan Layar

<p align="center">
  <img src="docs/screenshots/device/05-home.png" width="22%" />
  <img src="docs/screenshots/device/06-quran-list.png" width="22%" />
  <img src="docs/screenshots/device/08-adhan.png" width="22%" />
  <img src="docs/screenshots/device/13-qibla.png" width="22%" />
</p>

---

## Fitur

- **Jadwal Sholat & Adzan**: Perhitungan waktu sholat offline berbasis koordinat GPS, lengkap dengan alarm adzan tepat waktu dan opsi dering saat mode senyap.
- **Al-Qur'an Offline**: Teks Arab, transliterasi Latin, terjemahan bahasa Indonesia dan Inggris, penanda bacaan terakhir (*last read*), serta bookmark ayat.
- **Audio Murattal**: Pilihan audio per surah dari qari pilihan yang bisa diunduh untuk didengarkan offline.
- **Arah Kiblat**: Kompas penunjuk arah Ka'bah menggunakan sensor perangkat.
- **Hadits & Doa**: Kumpulan hadits harian, doa-doa hisnul muslim, dan jadwal imsakiyah Ramadhan.
- **Tema & Tampilan**: Mendukung mode terang dan gelap (*dark mode*), serta pengaturan ukuran font Arab.

---

## Cara Build

### Kebutuhan:
- Android Studio (versi Ladybug / Iguana atau yang lebih baru)
- JDK 17
- Android SDK Platform 34+

### Langkah Kompilasi:

1. Clone repositori ini:
   ```bash
   git clone https://github.com/saferill/Nur-App.git
   cd Nur-App
   ```

2. Buka proyek di Android Studio, atau build langsung melalui terminal:
   ```bash
   # Linux / macOS
   ./gradlew assembleDebug

   # Windows PowerShell
   .\gradlew.bat assembleDebug
   ```

Berkas APK akan berada di `app/build/outputs/apk/debug/app-debug.apk`.

---

## Izin Aplikasi

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: Diperlukan untuk menghitung jadwal sholat dan arah kiblat sesuai lokasi pengguna.
- `POST_NOTIFICATIONS`: Menampilkan notifikasi waktu sholat (Android 13+).
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: Memastikan alarm adzan berbunyi tepat pada waktunya.
- `RECEIVE_BOOT_COMPLETED`: Menjadwalkan ulang alarm sholat secara otomatis saat ponsel dinyalakan kembali.

---

## Lisensi

Proyek ini menggunakan lisensi [MIT License](LICENSE).
