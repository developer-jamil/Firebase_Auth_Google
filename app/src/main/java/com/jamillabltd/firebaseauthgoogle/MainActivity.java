package com.jamillabltd.firebaseauthgoogle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient client;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //progressbar
        progressBar = findViewById(R.id.progressBarId);

        // Initialize variables
        SignInButton signInButton = findViewById(R.id.signInWithGoogle);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, options);
        signInButton.setOnClickListener(v -> {
            Intent i = client.getSignInIntent();
            startActivityForResult(i, 1234);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            progressBar.setVisibility(View.VISIBLE);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String selectedEmail = account.getEmail(); // get the selected Google account's email
                String modifiedEmail = Objects.requireNonNull(selectedEmail).replace(".", "-");

                // Check if the user already exists in the Realtime Database
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                referenceProfile.child("All Users").child(modifiedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User already exists, sign in with credential
                            Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            progressBar.setVisibility(View.GONE);
                                            finish();
                                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // User does not exist, create new entry in Realtime Database
                            Toast.makeText(MainActivity.this, "New user", Toast.LENGTH_SHORT).show();
                            UserInfo userInfo = new UserInfo(
                                    account.getDisplayName(),
                                    selectedEmail,
                                    "",
                                    "",
                                    "",
                                    "Regular",
                                    Objects.requireNonNull(account.getPhotoUrl()).toString()
                            );

                            referenceProfile.child("All Users").child(modifiedEmail).setValue(userInfo).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Sign in with the credential
                                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                                    FirebaseAuth.getInstance().signInWithCredential(credential)
                                            .addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    progressBar.setVisibility(View.GONE);
                                                    finish();
                                                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(MainActivity.this, Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(MainActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // handle database error
                    }
                });

            } catch (ApiException e) {
                // handle the exception
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            finish();
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }


}