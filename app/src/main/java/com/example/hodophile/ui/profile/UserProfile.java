package com.example.hodophile.ui.profile;

import com.example.hodophile.ReviewItem;

import java.util.Map;

public class UserProfile {

    private String username, image;
    private Map<String, ReviewItem> reviews;

    public UserProfile() { }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, ReviewItem> getReviews() {
        return reviews;
    }

    public void setReviews(Map<String, ReviewItem> reviews) {
        this.reviews = reviews;
    }
}
