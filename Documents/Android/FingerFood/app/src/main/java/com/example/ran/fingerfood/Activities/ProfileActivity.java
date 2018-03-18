package com.example.ran.fingerfood.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ran.fingerfood.Database.MyDataManager;
import com.example.ran.fingerfood.Database.UserData;
import com.example.ran.fingerfood.Logic.Card;
import com.example.ran.fingerfood.Logic.GridImageAdapter;
import com.example.ran.fingerfood.Logic.Restaurant;
import com.example.ran.fingerfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    public final static String COORDINATE_KEY = "coordinates";

    public  final static int CALL_PERMISSION_CODE = 123;

    /*** Arrays ***/
    private ArrayList<Restaurant> allRest;
    private ArrayList<Card> mLikes;
    private Dialog dishDialog;
    private double[] mRestCoordinates;

    /*** UIs Elements ***/
    private TextView mTextView;
    private TextView mNumberOfRest;
    private TextView mNumberOfDishes;
    private TextView txtclose;
    private ImageButton btnCall;
    private ImageButton btnWeb ;
    private ImageButton btnNav;
    private ImageButton btnUnlike;
    private ImageView img ;
    private TextView restName;
    private TextView restAddress ;

    /*** Firebase ***/
    private String mUserId;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebase;
    private MyDataManager myDataManager;
    private UserData mUserData;
    private String mUserName;

    private GridImageAdapter mAdapter;
    private MediaPlayer mPlayer;




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have permission
            }
            else {
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        getWindow().getAttributes().windowAnimations = R.style.fade_in;

        initFirebase();



        dishDialog = new Dialog(this);

        //init arrays
        mRestCoordinates = new double[2];
        mUserName = mUserData.getUserName();
        mLikes = mUserData.getAllLikes();
        allRest = myDataManager.getAllRestaurants();

        setUserName();
        setUpImageGrid(mLikes);

    }


    public void initFirebase() {
        myDataManager = MyDataManager.getInstance();
        mFirebase = myDataManager.getFirebaseDatabase();
        mUserData = UserData.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserId = mAuth.getCurrentUser().getUid();
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void setUpImageGrid(ArrayList<Card> likes){

        RecyclerView recyclerView = findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        mNumberOfRest = (TextView)findViewById(R.id.numberOfRest);
        mNumberOfDishes = (TextView)findViewById(R.id.numberOfDishes);
        mNumberOfRest.setText(String.valueOf(getNumberOfRests()));
        mNumberOfDishes.setText(String.valueOf(getNumberOfDishes()));

        mAdapter = new GridImageAdapter(getApplicationContext(), likes, new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Card card) {

                setViewsInDialog();

                Glide.clear(img);
                Glide.with(getApplicationContext()).load(card.getImageUrl())
                        .placeholder(R.mipmap.ic_firelogo)
                        .error(R.mipmap.ic_firelogo)
                        .fitCenter()
                        .crossFade().into(img);

                restName.setText(card.getRestName());

                for (Restaurant rest : allRest){
                    if (rest.getRestId().equals(card.getRestId())){
                        restAddress.setText(rest.getAddress());
                        break;
                    }
                }

                setDialogViewsListeners(card);

                dishDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dishDialog.getWindow().getAttributes().windowAnimations = R.style.fade_in;
                dishDialog.show();

            }
        });

        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }


    public void setDialogViewsListeners(final Card card) {

        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dishDialog.dismiss();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String phone = getRestPhone(card.getRestId());
                dialPhone(phone);
                dishDialog.dismiss();
            }
        });


        btnWeb.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String url = getWebURL(card.getRestId());
                if (url != null){
                    dishDialog.dismiss();
                    Uri uriUrl = Uri.parse(url);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
                else {
                    showFailedMessage();
                    dishDialog.dismiss();
                }

            }
        });


        btnNav.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                dishDialog.dismiss();
                setRestLocation(card.getRestId());

                Bundle bundle = new Bundle();
                bundle.putDoubleArray(COORDINATE_KEY, mRestCoordinates);
                Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });


        btnUnlike.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                playSound();
                dishDialog.dismiss();
                mUserData.removeCardFromLikes(card);
                mLikes.remove(card);
                Toast.makeText(ProfileActivity.this , "Deleted Successfully!" , Toast.LENGTH_SHORT).show();
                refresh();
            }
        });
    }


    public void setViewsInDialog() {
        dishDialog.setContentView(R.layout.photodialog_layout);
        txtclose = (TextView)dishDialog.findViewById(R.id.btnclose);
        btnCall = (ImageButton)dishDialog.findViewById(R.id.btnCall);
        btnWeb = (ImageButton)dishDialog.findViewById(R.id.btnWeb);
        btnNav = (ImageButton)dishDialog.findViewById(R.id.btnNav);
        btnUnlike = (ImageButton)dishDialog.findViewById(R.id.btnUnlike);
        img = (ImageView)dishDialog.findViewById(R.id.oversize_img);
        restName = (TextView)dishDialog.findViewById(R.id.rest_name_text);
        restAddress = (TextView)dishDialog.findViewById(R.id.rest_address_text);
    }


    public void setRestLocation(String restId) {

        for (Restaurant r : allRest){
            if (restId.equals(r.getRestId())){
                mRestCoordinates[0] = r.getLatitude();
                mRestCoordinates[1] = r.getLongitude();
            }
        }
    }


    public String getWebURL(String restId) {
        for (Restaurant r : allRest){
            if (restId.equals(r.getRestId())){
                return r.getWebSiteUrl();
            }
        }
        return null;
    }


    public String getRestPhone(String restId) {
        for (Restaurant r : allRest) {
            if (restId.equals(r.getRestId())) {
                return r.getPhoneNumber();
            }
        }
        return null;
    }


    public void dialPhone(String phone) {
        String number = "tel:" + phone;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE } , 1);
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(number)));
    }



    public void showFailedMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sorry, can't perform this action")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }


    public void setUserName(){
        mTextView = findViewById(R.id.UserName);

        if (mUser != null){

            mTextView.setText(mUserName);

        }
        else
            mTextView.setText("user");
    }




    public void refresh(){
        Intent intent = getIntent();
        startActivity(intent);
        finish();
    }


    public int getNumberOfRests(){
        Set<String> allLikedRest = new HashSet<>();

        for (Card card : mLikes){
            allLikedRest.add(card.getRestId());
        }

        return allLikedRest.size();

    }


    public int getNumberOfDishes(){
        return mLikes.size();
    }


    public void playSound() {

        try {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }

            mPlayer = MediaPlayer.create(ProfileActivity.this, R.raw.pop);
            mPlayer.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}