package com.example.hodophile.ui.attractions;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.hodophile.ui.home.ItineraryItem;

public class AttractionItineraryItem extends ItineraryItem implements Parcelable {

    private String description, link, bookingURL, imageURL, address, category, rating, id;

    public AttractionItineraryItem() {
        super(2);
    }

    protected AttractionItineraryItem(Parcel in) {
        description = in.readString();
        link = in.readString();
        bookingURL = in.readString();
        imageURL = in.readString();
        address = in.readString();
        category = in.readString();
        rating = in.readString();
        id = in.readString();
    }

    public static final Creator<AttractionItineraryItem> CREATOR = new Creator<AttractionItineraryItem>() {
        @Override
        public AttractionItineraryItem createFromParcel(Parcel in) {
            return new AttractionItineraryItem(in);
        }

        @Override
        public AttractionItineraryItem[] newArray(int size) {
            return new AttractionItineraryItem[size];
        }
    };

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getLink() { return link; }

    public void setLink(String link) { this.link = link; }

    public String getBookingURL() { return bookingURL; }

    public void setBookingURL(String bookingURL) { this.bookingURL = bookingURL; }

    public String getImageURL() { return imageURL; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

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
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(bookingURL);
        dest.writeString(imageURL);
        dest.writeString(address);
        dest.writeString(category);
        dest.writeString(rating);
        dest.writeString(id);
    }
}
