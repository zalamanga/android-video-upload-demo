package com.kazimasum.videostreaming;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout email, password;
    private ProgressBar progress;
    private boolean busy;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.configureOrShow(this)) return;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) { openDashboard(); return; }
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.email); password = findViewById(R.id.pwd);
        progress = findViewById(R.id.progressBar3);
    }

    public void gotosignin(View view) { if (!busy) startActivity(new Intent(this, login.class)); }

    public void singup(View view) {
        if (busy) return;
        String address = email.getEditText().getText().toString().trim();
        String secret = password.getEditText().getText().toString();
        email.setError(null); password.setError(null);
        if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) { email.setError("Masukkan email yang valid."); return; }
        if (secret.length() < 8) { password.setError("Password minimal 8 karakter."); return; }
        busy = true; progress.setVisibility(View.VISIBLE); view.setEnabled(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(address, secret).addOnCompleteListener(task -> {
            busy = false; progress.setVisibility(View.GONE); view.setEnabled(true);
            if (task.isSuccessful()) openDashboard();
            else Toast.makeText(this, "Pendaftaran gagal. Periksa akun, koneksi, dan konfigurasi Firebase.", Toast.LENGTH_LONG).show();
        });
    }

    private void openDashboard() {
        startActivity(new Intent(this, dashboard.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
