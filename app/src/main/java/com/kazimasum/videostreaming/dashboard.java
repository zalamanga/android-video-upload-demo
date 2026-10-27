package com.kazimasum.videostreaming;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class dashboard extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.requireUser(this, login.class)) return;
        setContentView(R.layout.activity_dashboard);
        findViewById(R.id.addvideo).setOnClickListener(view -> startActivity(new Intent(this, addvideo.class)));
        findViewById(R.id.logout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            FirebaseSetup.requireUser(this, login.class);
        });
    }

    @Override protected void onStart() {
        super.onStart();
        if (findViewById(R.id.addvideo) != null) FirebaseSetup.requireUser(this, login.class);
    }
}
