package com.example.ran.fingerfood.Logic;

import java.io.Serializable;

/**
 * Created by ran on 23/01/2018.
 */

public class Card implements Serializable{
    private String cardId;
    private String restId;
    private String restName;
    private String imageUrl;





    public Card(String cardId , String restId, String restName, String imageUrl){
        this.cardId = cardId;
        this.restId = restId;
        this.restName = restName;
        this.imageUrl = imageUrl;
    }


    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setRestId(String restId) {
        this.restId = restId;
    }

    public void setRestName(String restName) {
        this.restName = restName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRestId() {

        return restId;
    }

    public String getRestName() {
        return restName;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public boolean equals(Object obj) {
        if (this.cardId.equals(((Card)obj).getCardId()))
            return true;
        return false;
    }

}
