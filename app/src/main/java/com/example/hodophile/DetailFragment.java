package com.example.hodophile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.ui.home.ItineraryItem;
import com.example.hodophile.ui.home.TimeParcel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public abstract class DetailFragment<T extends ItineraryItem> extends Fragment {

    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private TextView date, start, end, location, reviewLocation, ratingBarBG;
    private Button itineraryBtn, reviewBtn;
    private ImageButton itineraryCloseBtn, reviewCloseBtn;
    private EditText reviewText;
    private RatingBar ratingBar;
    private ArrayList<com.example.hodophile.ReviewItem> list;
    private com.example.hodophile.ReviewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    public void goToLink(String link) {
        if (link == null) {
            Toast.makeText(getContext(), "Not Available", Toast.LENGTH_SHORT).show();
        } else {
            Uri webAddress = Uri.parse(link);
            Intent goToLink = new Intent(Intent.ACTION_VIEW, webAddress);
            startActivity(goToLink);
        }
    }

    public void retrieveSavedInfo(String dateString, String location, Class<T> tClass) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("child", dateString);
        DatabaseReference database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Users").child(userID).child("Itinerary").child(dateString).child(location);
        database.get().addOnCompleteListener(task -> {
            T item = task.getResult().getValue(tClass);
            setDetails(item);
        });
    }

    public abstract void setDetails(T item);

    public void addToItinerary(T item) {
        dialogBuilder = new AlertDialog.Builder(getContext());
        View popupView = getLayoutInflater().inflate(R.layout.detail_popup, null);
        location = popupView.findViewById(R.id.detailPopupLocation);
        date = popupView.findViewById(R.id.detailPopupDate);
        start = popupView.findViewById(R.id.detailPopupStart);
        end = popupView.findViewById(R.id.detailPopupEnd);
        itineraryBtn = popupView.findViewById(R.id.detailPopupAdd);
        itineraryCloseBtn = popupView.findViewById(R.id.detailPopupClose);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Users").child(userID).child("Itinerary");
        TimeParcel st = new TimeParcel();
        TimeParcel et = new TimeParcel();

        location.setText(item.getLocation());
        start.setOnClickListener(t -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                st.setHr(hourOfDay);
                st.setMin(minute);
                item.setStartTime(st);
                start.setText(st.toString());
                start.setError(null);
            }, 0, 0, false);
            timePicker.show();
        });

        end.setOnClickListener(t -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                et.setHr(hourOfDay);
                et.setMin(minute);
                item.setEndTime(et);
                end.setText(et.toString());
                end.setError(null);
            }, 0, 0, false);
            timePicker.show();
        });

        date.setOnClickListener(t -> {
            DatePickerDialog datePicker = new DatePickerDialog(getContext());
            datePicker.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                String dateString = localDate.format(formatter);
                date.setText(dateString);
                item.setDate(dateString);
                date.setError(null);
            });
            datePicker.show();
        });

        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
        itineraryBtn.setOnClickListener(v -> {
            if (date.getText().toString().isEmpty()) {
                date.setError("Please select a date");
                date.requestFocus();
            } else if (start.getText().toString().isEmpty()) {
                start.setError("Please select a start time");
                start.requestFocus();
            } else if (end.getText().toString().isEmpty()) {
                end.setError("Please select an end time");
                end.requestFocus();
            } else if (et.compareTo(st) < 0) {
                end.setError("End time is earlier than start time");
                end.requestFocus();
            } else {
                database.child(item.getDate()).get().addOnCompleteListener(task -> {
                    List<ItineraryItem> itineraryList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        ItineraryItem itineraryItem = dataSnapshot.getValue(ItineraryItem.class);
                        itineraryList.add(itineraryItem);
                    }
                    itineraryList.sort(ItineraryItem::compareTo);
                    ItineraryItem overlap = checkOverlap(itineraryList, st, et);
                    if (overlap == null) {
                        database.child(item.getDate()).child(item.getLocation()).setValue(item)
                                .addOnCompleteListener(t -> Toast.makeText(getContext(),
                                        "Added to itinerary", Toast.LENGTH_SHORT).show());
                        dialog.dismiss();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Overlapping Events");
                        builder.setMessage(String.format(
                                "This new event overlaps with %s. Are you sure you want to add this event to your itinerary?",
                                overlap.getLocation()));
                        builder.setPositiveButton("Confirm", (dg, which) -> {
                                database.child(item.getDate()).child(item.getLocation()).setValue(item);
                                dialog.dismiss();
                        });
                        builder.setNegativeButton("Cancel", null);
                        AlertDialog confirmationDialog = builder.create();
                        confirmationDialog.show();
                    }
                });
            }
        });

        itineraryCloseBtn.setOnClickListener(v -> dialog.dismiss());
    }

    public ItineraryItem checkOverlap(List<ItineraryItem> itineraryList, TimeParcel start, TimeParcel end) {
        for (int i = 0; i < itineraryList.size(); i++) {
            ItineraryItem item = itineraryList.get(i);
            if (item.getStartTime().compareTo(start) == 0) {
                return item;
            } else if (item.getStartTime().compareTo(start) < 0) {
                if (item.getEndTime().compareTo(start) > 0) {
                    return item;
                }
            } else if (item.getStartTime().compareTo(start) > 0) {
                if (item.getStartTime().compareTo(end) < 0) {
                    return item;
                }
            }
        }
        return null;
    }

    public void addReview(String location, String locationID) {
        dialogBuilder = new AlertDialog.Builder(getContext());
        View popupView = getLayoutInflater().inflate(R.layout.review_popup, null);
        reviewLocation = popupView.findViewById(R.id.addReviewLocation);
        reviewText = popupView.findViewById(R.id.addReviewText);
        ratingBar = popupView.findViewById(R.id.addRatingBar);
        reviewBtn = popupView.findViewById(R.id.addReviewBtn);
        reviewCloseBtn = popupView.findViewById(R.id.addReviewClose);
        ratingBarBG = popupView.findViewById(R.id.ratingBarBG);

        reviewLocation.setText(location);
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (rating > 0.0) {
                ratingBarBG.setError(null);
            } else {
                ratingBarBG.setError("Please select a rating");
                ratingBarBG.requestFocus();
            }
        });

        reviewBtn.setOnClickListener(v -> {
            String reviewString = reviewText.getText().toString();
            float rating = ratingBar.getRating();
            if (rating == 0.0) {
                ratingBarBG.setError("Please select a rating");
                ratingBarBG.requestFocus();
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userID = user.getUid();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                String date = LocalDate.now().format(formatter);
                com.example.hodophile.ReviewItem review = new com.example.hodophile.ReviewItem(userID, rating, reviewString, date, location);
                DatabaseReference databaseReviews = FirebaseDatabase
                        .getInstance(getString(R.string.database_link))
                        .getReference("Reviews").child(locationID).child(userID);
                databaseReviews.setValue(review);
                DatabaseReference databaseProfile = FirebaseDatabase
                        .getInstance(getString(R.string.database_link))
                        .getReference("Profiles").child(userID).child("reviews").child(locationID);
                databaseProfile.setValue(review);
                dialog.dismiss();
            }
        });
        reviewCloseBtn.setOnClickListener(v -> dialog.dismiss());
    }

    public void retrieveReviews(String locationID, RecyclerView recyclerView) {
        DatabaseReference database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Reviews").child(locationID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                list = new ArrayList<>();
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new com.example.hodophile.ReviewAdapter(getContext(), list);
                recyclerView.setAdapter(adapter);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ReviewItem reviewItem = dataSnapshot.getValue(ReviewItem.class);
                    list.add(reviewItem);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
