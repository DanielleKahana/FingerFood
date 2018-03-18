package com.example.ran.fingerfood.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.example.ran.fingerfood.Database.MyDataManager;
import com.example.ran.fingerfood.Database.UserData;
import com.example.ran.fingerfood.Logic.Card;
import com.example.ran.fingerfood.Logic.CardArrayAdapter;
import com.example.ran.fingerfood.Logic.Restaurant;
import com.example.ran.fingerfood.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final double DEFAULT_DISTANCE_KM = 10;

    /*** Firebase ***/
    private FirebaseAuth mAuth;
    private UserData mUserData;
    private MyDataManager myDataManager;

    /*** Arrays ***/
    private ArrayList <Restaurant> mAllRestaurantsList; // Array of ALL the restaurants in DB
    private ArrayList<Card> mCardsToShow; // Array of all cards that is going to be shown in the activity
    private double[] mUserCoordinates;
    private List<Card> rowItems;
    private ArrayList<Card> mAllLikedCards;

    /*** User's preferences ***/
    private boolean mUserEatKosher , mUserWantDelivery;
    private int mUserMaxDistanceFromRest;
    private int mUserPricePreference;

    /*** Animations ***/
    private Animation mAnimScale;
    private Animation mAnimAlpha;

    /*** Buttons ***/
    private ImageButton likebtn;
    private ImageButton unlikebtn;
    private ImageButton logOutButton;
    private ImageButton favoritesButton;
    private ImageButton preferenceButton;

    private GoogleApiClient  mGoogleApiClient;
    private ArrayAdapter arrayAdapter;
    private MediaPlayer mPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        initFirebase();

        //init arrays
        mAllLikedCards = mUserData.getAllLikes();
        mAllRestaurantsList = myDataManager.getAllRestaurants();
        mCardsToShow = new ArrayList<>();
        mUserCoordinates = new double[]{LoginActivity.DEFAULT_LATITUDE , LoginActivity.DEFAULT_LONGTITUDE};
        rowItems = new ArrayList<>();
        arrayAdapter = new CardArrayAdapter(this, R.layout.item, rowItems);

        initAnimations();
        initUIs();
        initDefaultUserPreferences();
        getBundleFromActivity();
        cardViewHandler();

        filterByUserPrefs();
        showCardsFromList();
    }



    public void cardViewHandler() {
        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                playSound();
                unlikebtn.startAnimation(mAnimScale);
                Card card = (Card) dataObject;
                mCardsToShow.remove(card);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onRightCardExit(Object dataObject) {
                playSound();
                likebtn.startAnimation(mAnimScale);
                Card card = (Card) dataObject;
                mUserData.addCardToLikes(card);
                mCardsToShow.remove(card);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

//                if (itemsInAdapter <= 1) {
//                    //show all the restaurants that the user disliked again
//                    showCardsFromList();
//                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

    }



    public void filterByUserPrefs() {

        for (Restaurant rest: mAllRestaurantsList){
            if (isEligible(rest))
                mCardsToShow.addAll(rest.getCardList());
        }

        mCardsToShow.removeAll(mUserData.getAllLikes());
    }


    private boolean isEligible(Restaurant rest) {
        double distanceFromRest = distance(rest.getLatitude(), rest.getLongitude(), mUserCoordinates[0], mUserCoordinates[1]);

        if (distanceFromRest > mUserMaxDistanceFromRest)
            return false;

        if (mUserEatKosher && !rest.isKosher())
            return false;

        if (mUserWantDelivery && !rest.isHasDelivery())
            return false;

        if (mUserPricePreference != 0){
            if (mUserPricePreference != rest.getPrice())
                return false;
        }
        return true;
    }


    public void initDefaultUserPreferences() {
        mUserMaxDistanceFromRest= (int)DEFAULT_DISTANCE_KM;
        mUserEatKosher = false;
        mUserEatKosher = false;
        mUserWantDelivery = false;
        mUserPricePreference = 0;
    }


    public void initFirebase() {
        myDataManager = MyDataManager.getInstance();
        mUserData = UserData.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }


    public void initUIs() {
        likebtn = (ImageButton)findViewById(R.id.v);
        unlikebtn = (ImageButton)findViewById(R.id.x);
        logOutButton = (ImageButton)findViewById(R.id.log_out_button);
        favoritesButton = (ImageButton)findViewById(R.id.favorites_button);
        preferenceButton = (ImageButton)findViewById(R.id.Preference_button);
        setListeners();
    }


    public void initAnimations() {
        mAnimScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        mAnimAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
    }


    public void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }


    @Override
    protected void onStart() {

        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        myDataManager = MyDataManager.getInstance();
        mAllRestaurantsList = myDataManager.getAllRestaurants();
        filterByUserPrefs();
        showCardsFromList();
    }


    public void showCardsFromList() {
        rowItems.clear();
            if (!mCardsToShow.isEmpty()) {

                for (Card card : mCardsToShow) {
                    if (!rowItems.contains(card))
                        rowItems.add(card);
                }
                Collections.shuffle(rowItems);
            }
        arrayAdapter.notifyDataSetChanged();
    }


    public void setListeners(){

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutButton.startAnimation(mAnimAlpha);
                logOutUser();
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoritesButton.startAnimation(mAnimAlpha);
                goToProfileActivity(view);
            }
        });

        preferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferenceButton.startAnimation(mAnimAlpha);
                goToPreferencesActivity();
            }
        });
    }


    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }


    public  double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    public  double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }





    public void logOutUser() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        mAuth.signOut();

                        // Google sign out
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                        //facebook sign out
                        LoginManager.getInstance().logOut();

                        mUserData.clearData();
                        goToLoginActivity();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Log Out").setMessage("Are you sure you want to log out? ")
                .setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

    }


    public void goToPreferencesActivity(){
        Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        logOutUser();
    }


    public void goToProfileActivity(View view){
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }


    public void getBundleFromActivity() {
        int dist , price;
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            if (extras.getDoubleArray(LoginActivity.LOCATION_KEY) != null){
                mUserCoordinates = extras.getDoubleArray(LoginActivity.LOCATION_KEY);
            }

            if (extras.getInt(PreferencesActivity.PREF_DISTANCE_KEY) != 0){
                dist = extras.getInt(PreferencesActivity.PREF_DISTANCE_KEY);
                mUserMaxDistanceFromRest = dist + 5;
            }

            if (extras.getInt(PreferencesActivity.PREF_PRICE_KEY) != 0){
                price = extras.getInt(PreferencesActivity.PREF_PRICE_KEY);
                mUserPricePreference = price;
            }

            if (extras.getBoolean(PreferencesActivity.PREF_KOSHER_KEY) != false ){
                mUserEatKosher = true;
            }

            if (extras.getBoolean(PreferencesActivity.PREF_DELIVERY_KEY) != false ){
                mUserWantDelivery = true;
            }
        }
    }

    public void playSound() {

        try {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }

            mPlayer = MediaPlayer.create(MainActivity.this, R.raw.pop);
            mPlayer.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
