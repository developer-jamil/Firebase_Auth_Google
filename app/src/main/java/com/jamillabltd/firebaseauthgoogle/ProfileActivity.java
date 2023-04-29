package com.jamillabltd.firebaseauthgoogle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    GoogleSignInClient googleSignInClient;
    private ProgressBar progressBar;

    private TextView userName, profileName, userMobile, profileEmail, userEmail, userDob, userGender;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.userNameId);
        profileName = findViewById(R.id.fullNameId);
        profileEmail = findViewById(R.id.emailProfileId);
        userEmail = findViewById(R.id.emailId);
        userMobile = findViewById(R.id.mobileId);
        userDob = findViewById(R.id.dobId);
        userGender = findViewById(R.id.genderId);
        progressBar = findViewById(R.id.progressBarId);

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Initialize firebase auth and user
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        String getEmailFromGoogle = Objects.requireNonNull(firebaseUser).getEmail();
        //get user details from firebase database
        String modifiedEmail = Objects.requireNonNull(getEmailFromGoogle).replace(".", "-");

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child("All Users").child(modifiedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //access data
                UserInfo userInfo = snapshot.getValue(UserInfo.class);
                if (userInfo != null) {
                    String getName = userInfo.userName;
                    String getEmail = userInfo.userEmail;

                    String mobile = userInfo.userMobile;
                    String dob = userInfo.userDOB;
                    String gender = userInfo.userGender;
                    String profileImageLink = userInfo.profile_pic_link;
                    String userLevel = userInfo.userProfileBudgetStatus;

                    userName.setText(getName);
                    profileName.setText(getName);
                    profileEmail.setText(getEmail);
                    userEmail.setText(getEmail);
                    userMobile.setText(mobile);
                    userDob.setText(dob);
                    userGender.setText(gender);

                    //retrieve profile picture from firebase
                    CircleImageView circleImageView = findViewById(R.id.profileImageViewId);
                    if (profileImageLink == null || profileImageLink.isEmpty()) {
                        // If CircleImageView is empty or has no image, hide the ProgressBar
                        progressBar.setVisibility(View.GONE);
                    } else {
                        // If CircleImageView has an image, show the ProgressBar while loading
                        progressBar.setVisibility(View.VISIBLE);
                        Picasso.with(ProfileActivity.this)
                                .load(profileImageLink)
                                .placeholder(R.drawable.image_placeholder)
                                .into(circleImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // Hide the ProgressBar once the image is loaded
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError() {
                                        // If there is an error loading the image, hide the ProgressBar
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }

                    //user level
                    ImageView levelImage = findViewById(R.id.levelImageId);
                    if (userLevel.equals("Verified")) {
                        levelImage.setImageResource(R.drawable.verified_level);
                    } else if (userLevel.equals("VIP")) {
                        levelImage.setImageResource(R.drawable.vip_level);
                        //tint - levelImage.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.vip_tint), PorterDuff.Mode.SRC_IN);
                    } else {
                        levelImage.setVisibility(View.GONE);
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //edit profile image
        Button editProfileImage = findViewById(R.id.editProfileImageId);
        editProfileImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EditProfileImage.class)));

        //edit profile image
        Button editProfileInfo = findViewById(R.id.editProfileInfoId);
        editProfileInfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EditProfileActivity.class)));


    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate option menu
        getMenuInflater().inflate(R.menu.option_menu_layout, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //when any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //update profile
        if (id == R.id.menuUpdateProfileId) {
            startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        }

        //change password
        if (id == R.id.menuChangePasswordId) {
            startActivity(new Intent(getApplicationContext(), ChangePassword.class));
        }

        //Delete Profile
        if (id == R.id.menuDeleteProfileId) {
            startActivity(new Intent(getApplicationContext(), DeleteProfile.class));
        }

        //log out
        if (id == R.id.menuLogOutId) {
            // Initialize sign in client
            googleSignInClient = GoogleSignIn.getClient(ProfileActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
            // Sign out from google
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                // Check condition
                if (task.isSuccessful()) {
                    // When task is successful
                    // Sign out from firebase
                    firebaseAuth.signOut();

                    // Display Toast
                    Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();

                    // Close all previous activities and start MainActivity
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }
            });

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
        Toast.makeText(this, "onRestart Called", Toast.LENGTH_SHORT).show();
    }

}