package com.jamillabltd.firebaseauthgoogle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText fullNameEditText, emailEditText, dobEditText, mobileEditText;
    private ProgressBar progressBar;
    private Calendar selectedDate;
    private RadioGroup radioGroup;
    private String profileStatus;
    private String modifiedEmail;

    //retrieve data
    private String profile_pic_link;

    //firebase
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //set title
        this.setTitle(R.string.edit_profile);

        //back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initialize
        fullNameEditText = findViewById(R.id.fullNameEditTextId);
        emailEditText = findViewById(R.id.emailEditTextId);
        emailEditText.setEnabled(false);
        emailEditText.setFocusable(false);
        emailEditText.setFocusableInTouchMode(false);

        mobileEditText = findViewById(R.id.mobileEditTextId);
        progressBar = findViewById(R.id.progressBarId);
        radioGroup = findViewById(R.id.radioGroupId);

        dobEditText = findViewById(R.id.birthDateEditTextId);
        dobEditText.setOnClickListener(this);
        //selected date
        selectedDate = Calendar.getInstance();
        updateBirthDateEditText();

        Button updateInfo = findViewById(R.id.updateButtonId);
        updateInfo.setOnClickListener(this);

        // Initialize firebase auth and user
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        String getEmailFromGoogle = Objects.requireNonNull(firebaseUser).getEmail();
        //get user details from firebase database
        modifiedEmail = Objects.requireNonNull(getEmailFromGoogle).replace(".", "-");

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
                    profile_pic_link = userInfo.profile_pic_link;
                    profileStatus = userInfo.userProfileBudgetStatus;

                    fullNameEditText.setText(getName);
                    emailEditText.setText(getEmail);
                    mobileEditText.setText(mobile);
                    dobEditText.setText(dob);

                    // Set radio button based on gender value
                    switch (gender) {
                        case "":
                            radioGroup.clearCheck();
                            break;
                        case "Male":
                            radioGroup.check(R.id.maleId);
                            break;
                        case "Female":
                            radioGroup.check(R.id.femaleId);
                            break;
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    //onclick action
    @Override
    public void onClick(View v) {

        //dob
        if (v.getId() == R.id.birthDateEditTextId) {
            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfileActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // update selected date
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, monthOfYear);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        // update birth date EditText
                        updateBirthDateEditText();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            // show date picker dialog
            datePickerDialog.show();
        }

        //update button
        if (v.getId() == R.id.updateButtonId) {

            // get the selected gender value from the radio group
            int selectedGenderId = radioGroup.getCheckedRadioButtonId();
            if (selectedGenderId == -1) {
                // no RadioButton is selected, display an error message
                Toast.makeText(getApplicationContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
            String selectedGender = selectedGenderRadioButton.getText().toString();

            //get all entered data
            String getFullName = fullNameEditText.getText().toString().trim();
            String getEmail = emailEditText.getText().toString().trim();
            String getDob = dobEditText.getText().toString().trim();
            String getMobile = mobileEditText.getText().toString().trim();

            //validate mobile number using matcher and pattern
            String mobileRegex = "01[0-9]{9}";
            //First two digits must be 01 and rest 9 digits can be any digit.
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            Matcher mobileMatcher = mobilePattern.matcher(getMobile);

            // perform registration logic here
            if (TextUtils.isEmpty(getFullName)) {
                fullNameEditText.setError("Enter your full name!");
                fullNameEditText.requestFocus();
            } else if (TextUtils.isEmpty(getEmail)) {
                emailEditText.setError("Enter your email");
                emailEditText.setEnabled(true);
                emailEditText.setFocusable(true);
                emailEditText.setFocusableInTouchMode(true);
                emailEditText.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail).matches()) {
                emailEditText.setError("Enter a valid email");
                emailEditText.requestFocus();
            } else if (TextUtils.isEmpty(getDob)) {
                dobEditText.setError("Date of Birth is required");
                dobEditText.requestFocus();
            } else if (TextUtils.isEmpty(getMobile)) {
                mobileEditText.setError("Enter your mobile");
                mobileEditText.requestFocus();
            } else if (getMobile.length() != 11) {
                mobileEditText.setError("Enter a valid mobile");
                mobileEditText.requestFocus();
            } else if (!mobileMatcher.find()) {
                mobileEditText.setError("Enter a valid mobile");
                mobileEditText.requestFocus();
            } else {
                update(getFullName, getEmail, getDob, selectedGender, getMobile);
            }

        }
    }

    //update method
    private void update(String getFullName, String getEmail, String getDob, String selectedGender, String getMobile) {
        progressBar.setVisibility(View.VISIBLE);

        UserInfo userInfo = new UserInfo(getFullName, getEmail, getMobile, selectedGender, getDob , profileStatus, profile_pic_link );

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child("All Users").child(modifiedEmail).setValue(userInfo).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                //setting new display name
                Toast.makeText(EditProfileActivity.this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });

    }


    //chose dob from user - save previous chosen dob
    private void updateBirthDateEditText() {
        // format selected date as "dd/MM/yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = sdf.format(selectedDate.getTime());
        // update birth date EditText
        dobEditText.setText(formattedDate);
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