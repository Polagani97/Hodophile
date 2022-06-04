package com.example.hodophile.ui.attractions;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.DetailFragment;
import com.example.hodophile.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

public class AttractionDetailFragment extends DetailFragment<com.example.hodophile.ui.attractions.AttractionItineraryItem> {

    private String location, bookingURL, link;
    private com.example.hodophile.ui.attractions.AttractionItineraryItem attraction;
    private TextView nameView, descriptionView, linkView, bookingView, addressView, categoryView;
    private ImageView image;
    private Button addReviewBtn;
    private FloatingActionButton detailFab;
    private RecyclerView reviewsRecycler;
    private ProgressBar progressBar;
    private boolean saved;

    public AttractionDetailFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            saved = getArguments().getBoolean("Saved");
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameView = view.findViewById(R.id.detailName);
        addressView = view.findViewById(R.id.detailAddress);
        descriptionView = view.findViewById(R.id.detailDescription);
        categoryView = view.findViewById(R.id.detailPrice);
        bookingView = view.findViewById(R.id.detailBooking);
        linkView = view.findViewById(R.id.detailLink);
        image = view.findViewById(R.id.detailImage);
        detailFab = view.findViewById(R.id.detailfab);
        addReviewBtn = view.findViewById(R.id.addReviewOpenBtn);
        reviewsRecycler = view.findViewById(R.id.detailReviews);
        progressBar = view.findViewById(R.id.detailProgressBar);

        if (saved) {
            detailFab.hide();
            String dateString = getArguments().getString("Date");
            String location = getArguments().getString("Location");
            retrieveSavedInfo(dateString, location, com.example.hodophile.ui.attractions.AttractionItineraryItem.class);
        } else {
            attraction = getArguments().getParcelable("Attraction");
            setDetails(attraction);
        }
    }

    public void setDetails(com.example.hodophile.ui.attractions.AttractionItineraryItem item) {
        bookingURL = item.getBookingURL();
        link = item.getLink();
        location = item.getLocation();
        nameView.setText(location);
        addressView.setText(String.format("Address: %s", item.getAddress()));
        descriptionView.setText(item.getDescription());
        categoryView.setText(String.format("Category: %s", item.getCategory()));
        Picasso.get().load(item.getImageURL())
                .placeholder(R.drawable.error_placeholder_large).fit().into(image);
        retrieveReviews(item.getId(), reviewsRecycler);
        progressBar.setVisibility(View.GONE);

        bookingView.setOnClickListener(v -> goToLink(bookingURL));
        linkView.setOnClickListener(v -> goToLink(link));
        detailFab.setOnClickListener(v -> addToItinerary(item));
        addReviewBtn.setOnClickListener(v -> addReview(location, item.getId()));
    }
}

