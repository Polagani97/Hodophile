package com.example.hodophile.ui.accommodation;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.hodophile.ui.home.ItineraryItem;

public class AccommodationItineraryItem extends ItineraryItem implements Parcelable {

    private String address, imageURL, description, price, link, rating, id;

    public AccommodationItineraryItem() {
        super(1);
    }

    protected AccommodationItineraryItem(Parcel in) {
        address = in.readString();
        imageURL = in.readString();
        description = in.readString();
        price = in.readString();
        link = in.readString();
        rating = in.readString();
        id = in.readString();
    }

    public static final Creator<AccommodationItineraryItem> CREATOR = new Creator<AccommodationItineraryItem>() {
        @Override
        public AccommodationItineraryItem createFromParcel(Parcel in) {
            return new AccommodationItineraryItem(in);
        }

        @Override
        public AccommodationItineraryItem[] newArray(int size) {
            return new AccommodationItineraryItem[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRating() { return rating; }

    public void setRating(String rating) { this.rating = rating; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(imageURL);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(link);
        dest.writeString(rating);
        dest.writeString(id);
    }
}
