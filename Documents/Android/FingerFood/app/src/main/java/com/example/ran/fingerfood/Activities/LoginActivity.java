package com.example.ran.fingerfood.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ran.fingerfood.Database.MyDataManager;
import com.example.ran.fingerfood.Database.UserData;
import com.example.ran.fingerfood.Logic.NetworkStatus;
import com.example.ran.fingerfood.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;


public class LoginActivity extends AppCompatActivity {

    public static final String ALL_USERS = "users";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";
    public final static String LOCATION_KEY = "location";
    public static final double DEFAULT_LATITUDE = 32.113619;
    public static final double DEFAULT_LONGTITUDE = 34.818165;
    public static final int RC_GOOGLE_LOGIN = 2;

    //android ui
    private Button mSignIn, mRegister, mForgot , mLater;
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;
    private ImageButton mGoogleSignInBtn , mFacebookSignInBtn;

    //firebase
    private FirebaseAuth mAuth;
    private MyDataManager myDataManager;
    private UserData mUserData;
    private FirebaseDatabase mFirebase;
    private String mUserId;
    private String mUserFirstName;
    private String mUserLastName;

    private boolean isValid;

    //network
    private NetworkStatus mNetworkStatus;

    //location
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private double[] mCoordinates;

//google
    private GoogleApiClient mGoogleApiClient;

//facebook
    private CallbackManager mFacebookCallbackManager;




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                //google sign in failed
                Toast.makeText(LoginActivity.this, "Google Sign in went wrong ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        initFirebase();
        initLocation();
        initNetworkStatus();
        initUIs();

        /**** FACEBOOK ****/
        initFacebookSignIn();

        /**** GOOGLE ****/
        initGoogleSignIn();
    }



    public void initFirebase() {
        myDataManager = MyDataManager.getInstance();
        mFirebase = myDataManager.getFirebaseDatabase();
        mAuth = FirebaseAuth.getInstance();
    }

    public void initUIs() {

        mSignIn = (Button) findViewById(R.id.sign_in_button);
        mRegister = (Button) findViewById(R.id.register_button);
        mForgot = (Button) findViewById(R.id.forgot_password_button);
        mGoogleSignInBtn = (ImageButton) findViewById(R.id.google_btn_id);
        mFacebookSignInBtn = (ImageButton) findViewById(R.id.facebook_btn_id);
        mLater = (Button) findViewById(R.id.sign_in_later_btn);


        //text
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        //progressBar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_id);
        mProgressBar.setVisibility(View.INVISIBLE);

        setButtonListeners();

    }

    public void setButtonListeners() {
        //register button clicked
        mRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!mNetworkStatus.isConnected()){
                    showNetworkErrorMessage();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();

            }
        });


        //Sign in button clicked
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mNetworkStatus.isConnected()){
                    showNetworkErrorMessage();
                    return;
                }
                isValid = validateUserInput(mEmail.getText().toString(), mPassword.getText().toString());
                if (isValid) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    signInWithEmail(mEmail.getText().toString(), mPassword.getText().toString());
                }
            }
        });


        //forgot password button clicked
        mForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mNetworkStatus.isConnected()){
                    showNetworkErrorMessage();
                    return;
                }
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();

            }
        });


        //google sign in button clicked
        mGoogleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNetworkStatus.isConnected()){
                    showNetworkErrorMessage();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                signInWithGoogle();
            }
        });

        mFacebookSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNetworkStatus.isConnected()){
                    showNetworkErrorMessage();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
            }
        });

        mLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNetworkStatus.isConnected()){
                    showNetworkErrorMessage();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference ref = mFirebase.getReference().child(ALL_USERS).child(userId);
                            ref.child(USER_FIRST_NAME).setValue("Anonymous");
                            ref.child(USER_LAST_NAME).setValue("User");
                            mUserData = UserData.getInstance();
                            mProgressBar.setVisibility(View.VISIBLE);
                            readData();
                        }
                        else{
                            Toast.makeText(LoginActivity.this , "Authentication failed." , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    public void initNetworkStatus() {
        mNetworkStatus = new NetworkStatus(this);

        if (!mNetworkStatus.isConnected()){
            showNetworkErrorMessage();
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        }
    }


    public void showNetworkErrorMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sorry, this application required an Internet connection.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this , "Connection failed." ,Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }


    public void initFacebookSignIn() {

        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(loginResult.getAccessToken() != null) {
                    HandleFacebookAccessToken(loginResult.getAccessToken());
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this , "Authentication failed." , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(LoginActivity.this , "Authentication failed." , Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void HandleFacebookAccessToken(AccessToken accessToken) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this , "authentication failed" , Toast.LENGTH_SHORT).show();
                }
                else{
                    mUserId = mAuth.getCurrentUser().getUid();
                    DatabaseReference ref = mFirebase.getReference().child(ALL_USERS).child(mUserId);
                    Profile profile = Profile.getCurrentProfile();
                    mUserFirstName = "facebook";
                    mUserLastName = "user";
                    if (profile != null){
                        mUserFirstName = profile.getFirstName();
                        mUserLastName = profile.getLastName();
                        ref.child(USER_FIRST_NAME).setValue(mUserFirstName);
                        ref.child(USER_LAST_NAME).setValue(mUserLastName);
                    }
                    mUserData = UserData.getInstance();
                    mProgressBar.setVisibility(View.VISIBLE);
                    readData();
                }
            }
        });
    }


    public void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
    }


    public void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            mUserId = mAuth.getCurrentUser().getUid();
                            DatabaseReference ref = mFirebase.getReference().child(ALL_USERS).child(mUserId);
                            ref.child(USER_FIRST_NAME).setValue(account.getGivenName());
                            ref.child(USER_LAST_NAME).setValue(account.getFamilyName());
                            mUserData = UserData.getInstance();
                            mProgressBar.setVisibility(View.VISIBLE);
                            readData();
                        }
                    }
                });
    }


    public void signInWithEmail(String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        if (!task.isSuccessful())
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        else {
                            mUserData = UserData.getInstance();
                            mProgressBar.setVisibility(View.VISIBLE);
                            readData();
                        }
                    }
                });
    }


    public void readData() {
        new CountDownTimer(5000, 5000) {

            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
               goToMainActivity();
            }
        }.start();
    }

    public void showFailedMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sorry, weak internet connection... \nPlease try again soon.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void goToMainActivity() {
        mProgressBar.setVisibility(View.INVISIBLE);

        if (myDataManager.getAllRestaurants().isEmpty()){
            showFailedMessage();
            return;
        }
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDoubleArray(LOCATION_KEY, mCoordinates);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();


    }








    public boolean validateUserInput(String email, final String password) {

        //validate email
        if (email.isEmpty()) {
            mEmail.setError("Email is required");
            mEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Please enter a valid email");
            mEmail.requestFocus();
            return false;
        }

        //validate password
        if (password.isEmpty()) {
            mPassword.setError("Password is required");
            mPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            mPassword.setError("Minimum length of password should be 6");
            mPassword.requestFocus();
            return false;
        }

        return true;
    }


    public void initLocation() {

        //set default location - Afeka College , Tel Aviv
        mCoordinates = new double[]{DEFAULT_LATITUDE , DEFAULT_LONGTITUDE};

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (location != null) {

                    mCoordinates[0] = location.getLatitude();
                    mCoordinates[1] = location.getLongitude();

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        requestPermission();
    }


    public void requestPermission() {
        boolean hasPermission = false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            hasPermission = true;
        }

        else {
            if(ContextCompat.checkSelfPermission(this ,Manifest.permission.ACCESS_FINE_LOCATION )== PackageManager.PERMISSION_GRANTED){
                hasPermission = true;
            }
        }

        if (hasPermission){

            //we have permissions
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }
        else {

            //request permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION } , 1);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}


