package com.jamillabltd.firebaseauthgoogle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class DeleteProfile extends AppCompatActivity {
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;

    private String modifyEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        //set title
        this.setTitle(R.string.delete_profile);

        //back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize firebase auth  // Initialize firebase user
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        TextView emailTextView = findViewById(R.id.emailTextViewId);
        String email = firebaseUser.getEmail();
        emailTextView.setText(email);

        modifyEmail = Objects.requireNonNull(email).replace(".", "-");

        Button deleteAccount = findViewById(R.id.deleteAccountId);
        deleteAccount.setOnClickListener(v -> showAlertDialog());


    }

    //delete user dialog
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Want to Delete User and Related Data?");
        builder.setMessage("Do your really want to delete your profile and related data? Deleted data can't be undo.");

        //open email app if user Click continue button
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            //delete user from authentication
            deleteAccountMethod();
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> finish());

        //create the alertdialog
        AlertDialog alertDialog = builder.create();

        //yes button color
        alertDialog.setOnShowListener(dialogInterface -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red)));

        //show the alertDialog
        alertDialog.show();
    }

    //delete the logged in google from the firebase
    private void deleteAccountMethod() {
        if (firebaseUser != null) {
            firebaseUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign out from firebase
                            firebaseAuth.signOut();

                            deleteUserRealtimeDatabaseInfo();
                            deleteUserProfileImage();

                            Toast.makeText(DeleteProfile.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DeleteProfile.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    //delete user realtime database info
    private void deleteUserRealtimeDatabaseInfo() {
        firebaseDatabase.getReference("Registered Users")
                .child("All Users").child(modifyEmail).removeValue();

        // Close all previous activities and start MainActivity
        Intent intent = new Intent(DeleteProfile.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //delete user profile image from firebase storage
    private void deleteUserProfileImage() {
        // Check if the user has a profile image stored in Firebase Storage
        if (modifyEmail != null && !modifyEmail.isEmpty()) {
            // Get a reference to the user's profile image in Firebase Storage
            //Firebase Storage
            StorageReference reference = firebaseStorage.getReference("Registered Users Image").child("All Users").child(modifyEmail);
            reference.delete();
        }
    }



    //back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

}