package com.example.ran.fingerfood.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ran.fingerfood.Database.MyDataManager;
import com.example.ran.fingerfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {



    /*** UIs Elements ***/
    private Button mRegister;
    private EditText mEmailText, mPasswordText, mFirstNameText , mLastNameText;
    private String mEmail,mPassword,mFirstName,mLastName;
    private ProgressBar mProgressBar;

    /*** Firebase ***/
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseDatabase mFirebase;
    private MyDataManager myDataManager;

    private boolean isValid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        getWindow().getAttributes().windowAnimations = R.style.fade_in;

        myDataManager = MyDataManager.getInstance();
        mFirebase = myDataManager.getFirebaseDatabase();
        mAuth = FirebaseAuth.getInstance();
        initUIs();

    }

    public void initUIs() {
        mProgressBar = (ProgressBar) findViewById(R.id.register_progress_bar_id);
        mFirstNameText = (EditText) findViewById(R.id.first_name);
        mLastNameText = (EditText) findViewById(R.id.last_name);
        mRegister = (Button) findViewById(R.id.register_button);
        mEmailText = (EditText) findViewById(R.id.email);
        mPasswordText = (EditText) findViewById(R.id.password);

        //register button clicked
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mEmail = mEmailText.getText().toString();
                mPassword = mPasswordText.getText().toString();
                mFirstName =  mFirstNameText.getText().toString();
                mLastName = mLastNameText.getText().toString();

                isValid = validateUserInput();
                if (isValid){
                    mProgressBar.setVisibility(View.VISIBLE);
                    registerUser();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }



    public void registerUser() {
        mAuth.createUserWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(this,new OnCompleteListener<AuthResult>(){

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mProgressBar.setVisibility(View.GONE);
                if(!task.isSuccessful())
                {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException)
                        Toast.makeText(getApplicationContext(), "This email is already registered", Toast.LENGTH_SHORT).show();
                     else
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {

                    addUserToDatabase();
                    Toast.makeText(getApplicationContext(), "Registered Succesfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);

                }
            }
        });

    }


    public void addUserToDatabase(){
        String userId = mAuth.getCurrentUser().getUid();
        myDataManager.addNewUser(userId , mFirstName, mLastName);
    }

    public boolean validateUserInput() {

        //validate first Name
        if (mFirstName.isEmpty()) {
            mFirstNameText.setError("First name is required");
            mFirstNameText.requestFocus();
            return false;
        }

        //validate last Name
        if (mLastName.isEmpty()) {
            mLastNameText.setError("Last name is required");
            mLastNameText.requestFocus();
            return false;
        }

        //validate email
        if (mEmail.isEmpty()) {
            mEmailText.setError("Email is required");
            mEmailText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            mEmailText.setError("Please enter a valid email");
            mEmailText.requestFocus();
            return false;
        }

        //validate password
        if (mPassword.isEmpty()) {
            mPasswordText.setError("Password is required");
            mPasswordText.requestFocus();
            return false;
        }

        if (mPassword.length() < 6) {
            mPasswordText.setError("Minimum length of password should be 6");
            mPasswordText.requestFocus();
            return false;
        }

        return true;
    }


}
