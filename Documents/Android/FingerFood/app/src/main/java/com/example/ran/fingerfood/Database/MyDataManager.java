package com.example.ran.fingerfood.Database;

import com.example.ran.fingerfood.Logic.Card;
import com.example.ran.fingerfood.Logic.Restaurant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ran on 16/03/2018.
 */

public class MyDataManager {
    private static MyDataManager mDatabase = null;

    public static final String ALL_USERS = "users";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";
    public static final String USER_LIKES = "likes";
    public static final String ALL_RESTS = "restaurants";
    public static final String CARDS = "Cards";
    public static final String REST_NAME = "restName";
    public static final String KOSHER = "isKosher";
    public static final String DELIVERY = "hasDelivery";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longtitude";
    public static final String WEBSITE = "website";
    public static final String PHONE_NUMBER = "PhoneNumber";
    public static final String ADDRESS = "Address";
    public static final String PRICE = "Price";

    private ArrayList<Restaurant> allRestaurants;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRestsRef;
    private ValueEventListener mRestsListener;



    public interface FirebaseCallback{
        void onCallback(ArrayList<Restaurant> restaurantsList);
    }

    //constructor
    private MyDataManager(){
        setFirebaseDatabase();
    }



    //getInstance
    public static MyDataManager getInstance(){
        if (mDatabase ==  null){
            mDatabase = new MyDataManager();
        }
        return mDatabase;
    }



    public DatabaseReference getRestsRef() {
        return mRestsRef;
    }




    public void setFirebaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public ArrayList<Restaurant> getAllRestaurants() {
        return allRestaurants;
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public void addNewUser(String userId , String mFirstName, String mLastName) {
        //get reference to the new user
        DatabaseReference mRef = firebaseDatabase.getReference().child(ALL_USERS).child(userId);

        mRef.child(USER_FIRST_NAME).setValue(mFirstName);
        mRef.child(USER_LAST_NAME).setValue(mLastName);
        mRef.child(USER_LIKES).setValue(0);
    }


    public void readData(final FirebaseCallback firebaseCallback){
        allRestaurants = new ArrayList<>();
        mRestsRef =firebaseDatabase.getReference().child(ALL_RESTS);



        mRestsListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Restaurant rest = new Restaurant();
                    rest.setRestId(postSnapshot.getKey().toString());
                    rest.setRestName(postSnapshot.child(REST_NAME).getValue().toString());
                    rest.setKosher((boolean)postSnapshot.child(KOSHER).getValue());
                    rest.setHasDelivery((boolean)postSnapshot.child(DELIVERY).getValue());
                    rest.setLatitude((double)postSnapshot.child(LATITUDE).getValue());
                    rest.setLongitude((double)postSnapshot.child(LONGITUDE).getValue());
                    rest.setWebSiteUrl((String)postSnapshot.child(WEBSITE).getValue());
                    rest.setPhoneNumber((String)postSnapshot.child(PHONE_NUMBER).getValue());
                    rest.setAddress((String)postSnapshot.child(ADDRESS).getValue());
                    String price = (postSnapshot.child(PRICE).getValue().toString());
                    rest.setPrice(Integer.parseInt(price));

                    for (DataSnapshot urlSnapshot  : postSnapshot.child(CARDS).getChildren()){
                        String cardId = urlSnapshot.getKey().toString();
                        String imageUrl = urlSnapshot.getValue().toString();
                        Card card = new Card(cardId,rest.getRestId(), rest.getRestName(), imageUrl);
                        rest.addCardToList(card);
                    }
                    allRestaurants.add(rest);
                }
                firebaseCallback.onCallback(allRestaurants);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mRestsRef.addListenerForSingleValueEvent(mRestsListener);
    }


}
