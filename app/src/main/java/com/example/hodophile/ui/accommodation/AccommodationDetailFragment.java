package com.example.hodophile.ui.accommodation;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hodophile.DetailFragment;
import com.example.hodophile.R;
import com.example.hodophile.ui.home.ItineraryItem;
import com.example.hodophile.ui.home.TimeParcel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccommodationDetailFragment extends DetailFragment<com.example.hodophile.ui.accommodation.AccommodationItineraryItem> {

    private TextView nameView, descriptionView, linkView, priceView, addressView, extraView, review;
    private ImageView image;
    private FloatingActionButton detailFab;
    private Button addReviewBtn;
    private RecyclerView reviewsRecycler;
    private ProgressBar progressBar;
    private com.example.hodophile.ui.accommodation.AccommodationItineraryItem accommodation;
    private boolean saved;

    public AccommodationDetailFragment() { }

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
        priceView = view.findViewById(R.id.detailPrice);
        linkView = view.findViewById(R.id.detailLink);
        extraView = view.findViewById(R.id.detailBooking);
        image = view.findViewById(R.id.detailImage);
        detailFab = view.findViewById(R.id.detailfab);
        addReviewBtn = view.findViewById(R.id.addReviewOpenBtn);
        reviewsRecycler = view.findViewById(R.id.detailReviews);
        review = view.findViewById(R.id.review);
        progressBar = view.findViewById(R.id.detailProgressBar);

        extraView.setVisibility(View.GONE);

        if (saved) {
            detailFab.hide();
            String dateString = getArguments().getString("Date");
            String location = getArguments().getString("Location");
            retrieveSavedInfo(dateString, location, com.example.hodophile.ui.accommodation.AccommodationItineraryItem.class);
        } else {
            accommodation = getArguments().getParcelable("Accommodation");
            review.setVisibility(View.INVISIBLE);
            addReviewBtn.setVisibility(View.INVISIBLE);
            linkView.setVisibility(View.INVISIBLE);
            callDetailsAPI();
        }
    }

    private void callDetailsAPI() {
        String detailURL = "https://travel-advisor.p.rapidapi.com/hotels/get-details?location_id=" + accommodation.getId();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest searchHotel = new JsonObjectRequest(Request.Method.GET, detailURL,
                null, response -> {
                    try {
                        JSONObject data = response.getJSONArray("data").getJSONObject(0);
                        accommodation.setAddress(getFromJson("address", data));
                        accommodation.setDescription(getFromJson("description", data));
                        accommodation.setLink(getURLFromJson("website", data));
                        review.setVisibility(View.VISIBLE);
                        addReviewBtn.setVisibility(View.VISIBLE);
                        linkView.setVisibility(View.VISIBLE);
                        setDetails(accommodation);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error. Please try again.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, e -> Toast.makeText(getContext(), "Error. Please try again.", Toast.LENGTH_SHORT).show())
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("x-rapidapi-key", "864bde7699msh8d3f983cd6c3ed2p11e31ajsn15e541ffb40a");
                h.put("x-rapidapi-host", "travel-advisor.p.rapidapi.com");
                return h;
            }
        };
        searchHotel.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(searchHotel);
    }

    public void setDetails(com.example.hodophile.ui.accommodation.AccommodationItineraryItem item) {
        nameView.setText(item.getLocation());
        addressView.setText(String.format("Address: %s", item.getAddress()));
        descriptionView.setText(item.getDescription());
        priceView.setText(String.format("Price Range (USD): %s", item.getPrice()));
        Picasso.get().load(item.getImageURL()).placeholder(R.drawable.error_placeholder_large).fit().into(image);
        String link = item.getLink();
        retrieveReviews(item.getId(), reviewsRecycler);
        progressBar.setVisibility(ProgressBar.GONE);

        linkView.setOnClickListener(v -> goToLink(link));
        detailFab.setOnClickListener(v -> addToItinerary(item));
        addReviewBtn.setOnClickListener(v -> addReview(item.getLocation(), item.getId()));
    }

    private String getFromJson(String tag, JSONObject json) throws JSONException {
        if (json.isNull(tag)) {
            return "Not Available";
        } else if (json.getString(tag).isEmpty()) {
            return "Not Available";
        } else {
            return json.getString(tag);
        }
    }

    private String getURLFromJson(String tag, JSONObject json) throws JSONException {
        if (json.isNull(tag)) {
            return null;
        } else if (json.getString(tag).isEmpty()) {
            return null;
        } else {
            return json.getString(tag);
        }
    }

    @Override
    public void addToItinerary(com.example.hodophile.ui.accommodation.AccommodationItineraryItem item) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View popupView = getLayoutInflater().inflate(R.layout.accommodation_popup, null);
        TextView popupLocation = popupView.findViewById(R.id.accommodationPopupLocation);
        TextView startDate = popupView.findViewById(R.id.accommodationPopupStartDate);
        TextView startTime = popupView.findViewById(R.id.accommodationPopupStart);
        TextView endDate = popupView.findViewById(R.id.accommodationPopupEndDate);
        TextView endTime = popupView.findViewById(R.id.accommodationPopupEnd);
        ImageButton close = popupView.findViewById(R.id.accommodationPopupClose);
        Button addBtn = popupView.findViewById(R.id.accommodationPopupAdd);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        TimeParcel st = new TimeParcel();
        TimeParcel et = new TimeParcel();
        LocalDate[] dates = new LocalDate[2];

        popupLocation.setText(item.getLocation());
        startTime.setOnClickListener(t -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                st.setHr(hourOfDay);
                st.setMin(minute);
                startTime.setText(st.toString());
                startTime.setError(null);
            }, 0, 0, false);
            timePicker.show();
        });

        endTime.setOnClickListener(t -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                et.setHr(hourOfDay);
                et.setMin(minute);
                endTime.setText(et.toString());
                endTime.setError(null);
            }, 0, 0, false);
            timePicker.show();
        });

        startDate.setOnClickListener(t -> {
            DatePickerDialog datePicker = new DatePickerDialog(getContext());
            datePicker.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                dates[0] = LocalDate.of(year, month + 1, dayOfMonth);
                String dateString = dates[0].format(formatter);
                startDate.setText(dateString);
                startDate.setError(null);
            });
            datePicker.show();
        });

        endDate.setOnClickListener(t -> {
            DatePickerDialog datePicker = new DatePickerDialog(getContext());
            datePicker.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                dates[1] = LocalDate.of(year, month + 1, dayOfMonth);
                String dateString = dates[1].format(formatter);
                endDate.setText(dateString);
                endDate.setError(null);
            });
            datePicker.show();
        });

        dialogBuilder.setView(popupView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        addBtn.setOnClickListener(v -> {
            if (startDate.getText().toString().isEmpty()) {
                startDate.setError("Please select a start date");
                startDate.requestFocus();
            } else if (startTime.getText().toString().isEmpty()) {
                startTime.setError("Please select a start time");
                startTime.requestFocus();
            } else if (endDate.getText().toString().isEmpty()) {
                endDate.setError("Please select an end date");
                endDate.requestFocus();
            } else if (endTime.getText().toString().isEmpty()) {
                endTime.setError("Please select an end time");
                endTime.requestFocus();
            } else if (dates[0].isAfter(dates[1])) {
                endDate.setError("End date is earlier than start date");
                endDate.requestFocus();
            } else if (dates[0].isEqual(dates[1]) && et.compareTo(st) < 0) {
                endTime.setError("End time is earlier than start time");
                endTime.requestFocus();
            } else {
                splitAndAdd(dates, st, et, item, dialog);
            }
        });

        close.setOnClickListener(v -> dialog.dismiss());
    }

    private void splitAndAdd(LocalDate[] dates, TimeParcel start, TimeParcel end,
                             com.example.hodophile.ui.accommodation.AccommodationItineraryItem item, AlertDialog dialog) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Users").child(userID).child("Itinerary");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        if (dates[0].isEqual(dates[1])) {
            String date = dates[0].format(formatter);
            database.child(date).get().addOnCompleteListener(task -> {
                List<ItineraryItem> itineraryList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    ItineraryItem itineraryItem = dataSnapshot.getValue(ItineraryItem.class);
                    itineraryList.add(itineraryItem);
                }
                ItineraryItem overlap = checkOverlap(itineraryList, start, end);
                item.setDate(date);
                item.setStartTime(start);
                item.setEndTime(end);
                if (overlap == null) {
                    database.child(date).child(item.getLocation()).setValue(item)
                            .addOnCompleteListener(t -> Toast.makeText(getContext(),
                                    "Added to itinerary", Toast.LENGTH_SHORT).show());
                    dialog.dismiss();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Overlapping Events");
                    builder.setMessage(String.format(
                            "This new event overlaps with %s on %s. Are you sure you want to add this event to your itinerary?",
                            overlap.getLocation(), date));
                    builder.setPositiveButton("Confirm", (dg, which) ->
                            database.child(date).child(item.getLocation()).setValue(item)
                                    .addOnCompleteListener(t -> Toast.makeText(getContext(),
                                            "Added to itinerary", Toast.LENGTH_SHORT).show()));
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog confirmationDialog = builder.create();
                    confirmationDialog.show();
                }
            });
        } else {
            String date = dates[0].format(formatter);
            database.child(date).get().addOnCompleteListener(task -> {
                List<ItineraryItem> itineraryList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    ItineraryItem itineraryItem = dataSnapshot.getValue(ItineraryItem.class);
                    itineraryList.add(itineraryItem);
                }
                TimeParcel endOfDay = new TimeParcel(23, 59);
                ItineraryItem overlap = checkOverlap(itineraryList, start, endOfDay);
                item.setDate(date);
                item.setStartTime(start);
                item.setEndTime(endOfDay);
                if (overlap == null) {
                    database.child(date).child(item.getLocation()).setValue(item);
                    dialog.dismiss();
                    dates[0] = dates[0].plusDays(1);
                    splitAndAdd(dates, new TimeParcel(0, 0), end, item, dialog);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Overlapping Events");
                    builder.setMessage(String.format(
                            "This new event overlaps with %s on %s. Are you sure you want to add this event to your itinerary?",
                            overlap.getLocation(), date));
                    builder.setPositiveButton("Confirm", (dg, which) -> {
                        database.child(date).child(item.getLocation()).setValue(item);
                        dates[0] = dates[0].plusDays(1);
                        splitAndAdd(dates, new TimeParcel(0, 0), end, item, dialog);

                        });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog confirmationDialog = builder.create();
                    confirmationDialog.show();
                }
            });
        }
    }
}