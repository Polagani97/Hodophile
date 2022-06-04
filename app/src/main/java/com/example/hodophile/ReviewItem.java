package com.example.hodophile;

public class ReviewItem {

    private String userID, review, date, location;
    private float rating;

    public ReviewItem(String userID, float rating, String review, String date, String location) {
        this.userID = userID;
        this.rating = rating;
        this.review = review;
        this.date = date;
        this.location = location;
    }

    public ReviewItem() {
    }

    public String getUserID() { return userID; }

    public void setUserID(String userID) { this.userID = userID; }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

    public String getReview() { return review; }

    public void setReview(String review) { this.review = review; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }
}
