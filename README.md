# Android Video Upload Demo

Latihan Java untuk registrasi/login Firebase, memilih video dari perangkat, melihat pratinjau, lalu mengunggah file ke Firebase Storage milik akun yang sedang login.

Basis lokal videostreaming semula hanya mempunyai auth dan pratinjau; tombol Upload belum memiliki aksi. Versi ini menambahkan upload yang sebenarnya, progress, pembatalan selama transfer, serta hasil download URL. Repo ini belum menyediakan katalog atau pemutar streaming untuk daftar video.

## Fitur dan batasnya

- Registrasi email/password, login, penjagaan layar yang memerlukan akun, dan logout.
- Pemilih dokumen sistem untuk video tanpa meminta akses luas ke seluruh penyimpanan.
- VideoView dengan MediaController untuk pratinjau lokal.
- Upload ke user-videos/<uid>/<uuid>.<ekstensi>, progress, pesan gagal, dan tombol pembatalan.
- Upload ulang file terpilih dicegah setelah transfer sukses. Pilih file baru untuk upload berikutnya.
- Tanpa konfigurasi pribadi, aplikasi hanya menampilkan petunjuk Firebase dan tidak mengambil instance layanan dari activity.

Upload berjalan selama activity hidup. Transfer yang masih berjalan dibatalkan ketika activity dihancurkan, termasuk saat rotasi yang membuat activity baru. Belum ada WorkManager, resume lintas proses, katalog upload, edit metadata, delete di UI, atau pencarian. File yang sudah berhasil diunggah dapat diperiksa dari Firebase Console.

## Toolchain dan build

Gunakan JDK 17, Android SDK 35, Build Tools 35.0.0, dan Android Studio yang mendukung AGP 8.8.2. Gradle Wrapper 8.10.2 sudah disertakan. Minimum API 21, target API 34.

Buka repo sebagai project di Android Studio, atau atur ANDROID_HOME ke SDK komputer Anda lalu jalankan:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

APK debug di app/build/outputs/apk/debug/app-debug.apk. Android Studio membuat local.properties sendiri; file mesin tersebut tidak masuk repo. Build tanpa google-services.json menghasilkan aplikasi dengan layar petunjuk config. Dependency pertama kali tetap memerlukan unduhan Gradle.

## Konfigurasi Firebase milik Anda

1. Buat project pengujian dan daftarkan applicationId `com.kazimasum.videostreaming`.
2. Aktifkan Authentication Email/Password, lalu buat bucket default Storage. Storage saat ini memerlukan paket Blaze; periksa syaratnya pada [panduan resmi Storage](https://firebase.google.com/docs/storage/android/start).
3. Tinjau `storage.rules`, lalu terapkan pada bucket pengujian yang benar. Contoh membatasi akses ke UID pemilik, create maksimal 100 MiB, dan MIME video.
4. Sesudah bucket tersedia, unduh ulang config aplikasi dari Project settings dan ganti `app/google-services.json`. Pastikan nama tidak berakhiran `(2)` dan applicationId sesuai. File diabaikan Git; plugin aktif hanya ketika config tersedia.
5. Cocokkan `project_info.storage_bucket` pada config baru dengan nama bucket di Console. Plugin mengubahnya menjadi resource `google_storage_bucket`. [Source resmi Google Services](https://github.com/google/play-services-plugins/blob/main/google-services-plugin/src/main/kotlin/com/google/gms/googleservices/GoogleServicesTask.kt). Kode memakai `FirebaseStorage.getInstance()`, yaitu bucket default dari konfigurasi. [Konfigurasi bucket Android](https://firebase.google.com/docs/storage/android/start#set_up_cloud_storage_in_your_app).
6. Build ulang, daftar akun uji dengan password minimal delapan karakter, buka tombol tambah video, pilih video uji, lalu tekan Upload Video.

Jika memakai bucket non-default, pola SDK adalah `FirebaseStorage.getInstance("gs://BUCKET_NAME")`; sesuaikan inisialisasi pada `addvideo.sendVideo()` dengan bucket milik Anda. Ini arahan adaptasi source, bukan field pengaturan pada aplikasi. [Bucket tambahan](https://firebase.google.com/docs/storage/android/start#use_multiple_cloud_storage_buckets).

Realtime Database tidak dipakai, jadi repo ini tidak membutuhkan `firebase_url` atau konfigurasi region database. Konfigurasi dan data cloud lama tidak disertakan atau dimigrasikan.

Download URL ditampilkan setelah upload. Bagikan hanya kepada pihak yang Anda izinkan karena tautan dapat memberikan akses. MIME/ukuran pada rules belum memvalidasi isi file atau hak distribusi; format pratinjau bergantung decoder Android.

## Peta kode

| File | Tugas |
| --- | --- |
| MainActivity.java | Form registrasi, validasi email/password, masuk ke dashboard. |
| login.java | Login dan status permintaan auth. |
| dashboard.java | Tombol tambah video dan logout. |
| addvideo.java | Document picker, pratinjau, upload/progress/cancel, URL hasil. |
| FirebaseSetup.java | Layar setup saat config belum ada, lalu penjagaan akun. |
| storage.rules | Contoh aturan akses berdasarkan UID. |
| app/src/main/res/layout | Form yang dapat digulir, dashboard, dan layar video. |

Class Java ada di app/src/main/java/com/kazimasum/videostreaming. Nama class lowercase login/dashboard/addvideo dipertahankan dari latihan asal agar riwayat source mudah dicocokkan.

## Validasi dan asal kode

Source dan sintaks XML diperiksa saat perapihan. Test bawaan hanya addition_isCorrect dan pemeriksaan package; test itu tidak membuktikan auth/upload. Perintah, hasil, dan batas pemeriksaan build ada pada [BUILD-VERIFICATION.md](BUILD-VERIFICATION.md). Status mengikuti hasil aktual di berkas itu. Auth Firebase, Storage rules, upload/cancel, playback, dan screenshot UI belum dinyatakan teruji pada perangkat atau cloud nyata.

Canonical dipilih dari folder lokal videostreaming karena lebih lengkap daripada port MyApplication5. Empat layout dan tiga gambar port tersebut identik, sedangkan empat body class sama setelah perbedaan package/import/whitespace diabaikan. Port dan prototype MyApplication4 tidak dimasukkan ke repo ini.

Tidak ditemukan URL upstream atau berkas lisensi pada source lokal. Nama package asal dan aset dipertahankan sebagai petunjuk provenance. Tidak ditambahkan lisensi hak cipta atau klaim bahwa seluruh aset dibuat ulang. Konfigurasi Firebase lama, keystore, SDK path, cache, dan hasil build tidak disertakan.
