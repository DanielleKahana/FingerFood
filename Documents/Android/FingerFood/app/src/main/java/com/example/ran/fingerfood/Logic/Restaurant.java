package com.example.ran.fingerfood.Logic;

import java.io.Serializable;
import java.util.ArrayList;

public class Restaurant implements Serializable {
    private String restName;
    private String restId;
    private double latitude, longitude;
    private boolean isKosher;
    private boolean hasDelivery;
    private String webSiteUrl;
    private String phoneNumber;
    private String address;
    private int price;

    private ArrayList<Card> cardList;


    public Restaurant(String restName, String restId, double latitude, boolean isKosher, boolean hasDelivery, double longitude , String webSiteUrl , String phoneNumber , String address , int price){
        setRestName(restName);
        setRestId(restId);
        setLatitude(latitude);
        setLongitude(longitude);
        setKosher(isKosher);
        setHasDelivery(hasDelivery);
        setWebSiteUrl(webSiteUrl);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setPrice(price);

        cardList = new ArrayList<Card>();

    }

    public Restaurant(){
        cardList = new ArrayList<Card>();
    }



    public ArrayList<Card> getCardList() {
        return cardList;
    }



    public void addCardToList(Card card) {
        this.cardList.add(card);
    }

    public void setRestName(String restName) {
        this.restName = restName;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public void setWebSiteUrl(String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void setRestId(String restId) {
        this.restId = restId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isKosher() {
        return isKosher;
    }

    public void setKosher(boolean kosher) {
        isKosher = kosher;
    }


    public String getRestName() {

        return restName;
    }

    public String getRestId() {
        return restId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    public boolean isHasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }


    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }






}
