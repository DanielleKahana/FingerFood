package com.example.ran.fingerfood.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ran.fingerfood.R;

public class PreferencesActivity extends AppCompatActivity {

    public final static String PREF_DISTANCE_KEY = "distance";
    public final static String PREF_KOSHER_KEY = "kosher";
    public final static String PREF_DELIVERY_KEY = "delivery";
    public final static String PREF_PRICE_KEY = "price";
    public final static String[] PRICE_PICKER = {"ALL" , "$", "$$", "$$$" , "$$$$"};

    /*** UIs Elements ***/
    private GridLayout mainGrid;
    private  SeekBar seekBar;
    private  TextView textView;
    private int mProgressValue;
    private NumberPicker mPicker;
    private Button apply_btn , cancel_btn;

    private boolean hasDeliveryChecked;
    private boolean isKosherChecked;
    private int mPrice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getWindow().getAttributes().windowAnimations = R.style.fade_in; //style id
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setButtons();
        mainGrid = (GridLayout)findViewById(R.id.mainGrid);
        setToggleEvent(mainGrid);
        setPricePicker();
        setSeekBar();

    }



    public void setButtons() {
        apply_btn = (Button)findViewById(R.id.apply_btn_id);
        cancel_btn = (Button)findViewById(R.id.cancel_changes_btn_id);

        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyAndToMainActivity();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainActivity();
            }
        });



    }

    public void setPricePicker() {
        mPicker = (NumberPicker) findViewById(R.id.price_picker_id);
        mPicker.setMinValue(0);
        mPicker.setMaxValue(PRICE_PICKER.length - 1);
        mPicker.setDisplayedValues(PRICE_PICKER);
    }


    public void setSeekBar(){
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        textView = (TextView)findViewById(R.id.seekBarText);
        textView.setText("Restaurants " + seekBar.getProgress() + " Kilometers from you!");
        setmProgressValue((int) MainActivity.DEFAULT_DISTANCE_KM);
        seekBar.setProgress(mProgressValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgressValue = progress + 5;
                textView.setText("Restaurants " + mProgressValue + " Kilometers from you!");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Restaurants " + mProgressValue + " Kilometers from you!");
            }
        });

    }



    public void setmProgressValue(int mProgressValue) {
        this.mProgressValue = mProgressValue;
    }


    public void applyAndToMainActivity(){

        Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);

        Bundle bundle = new Bundle();

        if (mProgressValue != MainActivity.DEFAULT_DISTANCE_KM){
            bundle.putInt(PREF_DISTANCE_KEY, mProgressValue );
        }

        if(isKosherChecked){
            bundle.putBoolean(PREF_KOSHER_KEY , true);
        }

        if(hasDeliveryChecked){
            bundle.putBoolean(PREF_DELIVERY_KEY , true);
        }

        mPrice = mPicker.getValue();

        if (mPrice != 0){
            bundle.putInt(PREF_PRICE_KEY , mPrice);
        }

        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }





    public void setToggleEvent(GridLayout toggleEvent) {
       for (int i=0; i< mainGrid.getChildCount(); i++) {
           final CardView cardView = (CardView) mainGrid.getChildAt(i);
           cardView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   //Card Clicked
                   if (cardView.getCardBackgroundColor().getDefaultColor() == -1) {
                       if (cardView.getId() == R.id.delivery_card) {
                           hasDeliveryChecked = true;
                       }
                       if (cardView.getId() == R.id.kosher_card)
                           isKosherChecked = true;
                       cardView.setCardBackgroundColor(getResources().getColor(R.color.clickedGrey,getTheme()));

                   } else {
                       //Card unclicked
                       if (cardView.getId() == R.id.delivery_card) {
                           hasDeliveryChecked = false;
                       }
                       if (cardView.getId() == R.id.kosher_card)
                           isKosherChecked = false;
                       cardView.setCardBackgroundColor(Color.WHITE);

                   }
               }

           });
       }
    }


    public void goToMainActivity(){
        Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
