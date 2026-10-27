package com.kazimasum.videostreaming;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;

public class addvideo extends AppCompatActivity {
    private VideoView player;
    private Button browse, upload;
    private TextView status;
    private Uri selected;
    private UploadTask task;
    private boolean busy, uploaded, active;
    private final ActivityResultLauncher<String[]> picker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                String type = getContentResolver().getType(uri);
                if (type != null && !type.startsWith("video/")) { message("Pilih file video."); return; }
                selected = uri; uploaded = false;
                try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (SecurityException ignored) { /* Access may only last for the current document-provider session. */ }
                player.setVideoURI(uri); controls();
                message("Video dipilih. Pratinjau lokal tersedia; tekan Upload Video untuk menyimpan ke akun Anda.");
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.requireUser(this, login.class)) return;
        setContentView(R.layout.activity_addvideo);
        player = findViewById(R.id.videoView);
        browse = findViewById(R.id.browse); upload = findViewById(R.id.upload);
        status = findViewById(R.id.status);
        MediaController controls = new MediaController(this); controls.setAnchorView(player); player.setMediaController(controls);
        player.setOnPreparedListener(media -> { if (active) player.start(); });
        player.setOnErrorListener((media, what, extra) -> { message("Pratinjau gagal. Format video mungkin tidak didukung Android."); return true; });
        browse.setOnClickListener(view -> { if (!busy) picker.launch(new String[]{"video/*"}); });
        upload.setOnClickListener(view -> {
            if (busy) { if (task != null && task.isInProgress()) task.cancel(); }
            else sendVideo();
        });
        if (state != null && state.getString("selected") != null) {
            selected = Uri.parse(state.getString("selected")); uploaded = state.getBoolean("uploaded");
            player.setVideoURI(selected);
        }
        controls();
    }

    private void sendVideo() {
        if (selected == null || uploaded || !FirebaseSetup.requireUser(this, login.class)) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String mime = getContentResolver().getType(selected);
        String extension = mime == null ? null : MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        String path = "user-videos/" + user.getUid() + "/" + UUID.randomUUID() + "." + (extension == null ? "mp4" : extension);
        StorageReference file = FirebaseStorage.getInstance().getReference(path);
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType(mime == null ? "video/mp4" : mime).build();
        busy = true; controls(); message("Mengunggah video...");
        task = file.putFile(selected, metadata);
        task.addOnProgressListener(progress -> {
            if (progress.getTotalByteCount() > 0) message("Mengunggah " + (100 * progress.getBytesTransferred() / progress.getTotalByteCount()) + "%");
        }).addOnSuccessListener(result -> {
            uploaded = true;
            file.getDownloadUrl().addOnSuccessListener(url -> {
                busy = false; controls(); message("Upload selesai. URL download:\n" + url + "\n\nURL ini dapat memberi akses ke video. Bagikan hanya kepada pihak yang Anda izinkan.");
            }).addOnFailureListener(error -> {
                busy = false; controls(); message("File sudah terunggah, tetapi URL gagal diperoleh. Periksa Storage pada path:\n" + path);
            });
        }).addOnFailureListener(error -> { busy = false; controls(); message("Upload gagal. Periksa koneksi, rules, dan batas ukuran 100 MiB."); })
                .addOnCanceledListener(() -> { busy = false; controls(); message("Upload dibatalkan."); });
    }

    private void controls() {
        if (isDestroyed() || browse == null || upload == null) return;
        browse.setEnabled(!busy);
        upload.setEnabled(busy || (selected != null && !uploaded));
        upload.setText(busy ? "Batalkan upload" : "Upload Video");
    }
    private void message(String text) { if (!isDestroyed() && status != null) status.setText(text); }
    @Override protected void onResume() { super.onResume(); active = true; }
    @Override protected void onPause() { active = false; if (player != null) player.pause(); super.onPause(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        if (selected != null) state.putString("selected", selected.toString());
        state.putBoolean("uploaded", uploaded); super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        if (task != null && task.isInProgress()) task.cancel();
        if (player != null) player.stopPlayback();
        super.onDestroy();
    }
}
