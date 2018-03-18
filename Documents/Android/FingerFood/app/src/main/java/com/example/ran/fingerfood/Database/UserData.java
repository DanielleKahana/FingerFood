package com.example.ran.fingerfood.Database;

import com.example.ran.fingerfood.Logic.Card;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ran on 16/03/2018.
 */

public class UserData {
    private static UserData mData = null;
    public static final String ALL_USERS = "users";
    public  final static String LIKES = "likes";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";
    public static final String REST_NAME = "restName";
    public static final String IMAGE_URL = "imageURL";
    public static final String REST_ID = "restID";

    private ArrayList<Card> mAllLikes;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;

    private DatabaseReference mDishesRef;
    private DatabaseReference mUsersRef;

    private ValueEventListener mDishesListener;
    private String userId;
    private  String mUserName;



    //constructor
    private UserData(){
        firebaseDatabase = MyDataManager.getInstance().getFirebaseDatabase();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        setAllLikes();
        setUserName();
    }



    public String getUserName() {
        return mUserName;
    }


    public ArrayList<Card> getAllLikes() {
        return mAllLikes;
    }

    private void setAllLikes() {
        mAllLikes = new ArrayList<>();
        mUsersRef = firebaseDatabase.getReference().child(ALL_USERS);
        mDishesRef = mUsersRef.child(userId).child(LIKES);


        mDishesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String cardId = ds.getKey().toString();
                        String restID = ds.child(REST_ID).getValue().toString();
                        String imageUrl = ds.child(IMAGE_URL).getValue().toString();
                        String restName = ds.child(REST_NAME).getValue().toString();

                        Card card = new Card(cardId, restID, restName, imageUrl);
                        mAllLikes.add(card);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDishesRef.addListenerForSingleValueEvent(mDishesListener);


    }

    //getInstance
    public static UserData getInstance(){
        if (mData ==  null){
            mData = new UserData();
        }
        return mData;
    }


    public void addCardToLikes(Card card) {
        if (mAllLikes.contains(card)){
            return;
        }
        mAllLikes.add(card);
        mUsersRef.child(userId).child(LIKES).child(card.getCardId()).child(REST_ID).setValue(card.getRestId());
        mUsersRef.child(userId).child(LIKES).child(card.getCardId()).child(IMAGE_URL).setValue(card.getImageUrl());
        mUsersRef.child(userId).child(LIKES).child(card.getCardId()).child(REST_NAME).setValue(card.getRestName());
    }

    public void removeCardFromLikes(Card card) {
        mAllLikes.remove(card);
        mDishesRef.child(card.getCardId()).removeValue();
    }

    public void setUserName() {

        //logged in with email and password
        mUsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mUserName = dataSnapshot.child(USER_FIRST_NAME).getValue().toString() + " " + dataSnapshot.child(USER_LAST_NAME).getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void clearData() {
        mData = null;
    }
}
