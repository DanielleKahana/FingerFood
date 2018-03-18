package com.example.ran.fingerfood.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ran.fingerfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText mEmailText;
    private Button mResetPasswordButton;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        getWindow().getAttributes().windowAnimations = R.style.fade_in; //style id

        mProgressBar = (ProgressBar)findViewById(R.id.forgot_progress_bar_id);
        mResetPasswordButton = (Button) findViewById(R.id.reset_button);

        //text
        mEmailText = (EditText) findViewById(R.id.email);

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();


        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email  = mEmailText.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(getApplication(),"Enter your registered email", Toast.LENGTH_LONG).show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                resetPassword(email);
            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }


    private void resetPassword(final String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(), "We have sent to your email instructions to reset your password! " , Toast.LENGTH_SHORT).show();
                        }
                        else{
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Failed to send password" , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
