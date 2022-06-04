package com.example.hodophile.ui.attractions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hodophile.R;
import com.example.hodophile.SearchFragment;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttractionsFragment extends SearchFragment {

    private ArrayList<AttractionItineraryItem> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private AttractionsAdapter adapter;
    private SearchView searchView;
    private ProgressBar progressBar;
    private RequestQueue queue;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.searchRecycler);
        searchView = view.findViewById(R.id.searchView);
        progressBar = view.findViewById(R.id.searchProgressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                list = new ArrayList<>();
                queue = Volley.newRequestQueue(getContext());
                getLocationID(query, queue);
//                extractInfo("298278");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttractionsAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
    }

    public void extractInfo(String locationID) {
        String attractionsURL = "https://travel-advisor.p.rapidapi.com/attractions/list?sort=recommended&location_id=" + locationID;
        JsonObjectRequest searchAttractions = new JsonObjectRequest(Request.Method.GET,
                attractionsURL, null, response -> {
                    try {
                        if (response.isNull("data")) {
                            Toast.makeText(getContext(), "No attractions found.", Toast.LENGTH_SHORT).show();
                        } else {
                            JSONArray arr = response.getJSONArray("data");
                            for (int i = 0; i < arr.length(); i++) {
                                if (i != 6 && i != 15 && i != 24) { // API call returns different data at these positions
                                    JSONObject item = arr.getJSONObject(i);
                                    AttractionItineraryItem attraction = new AttractionItineraryItem();
                                    attraction.setId(getFromJson("location_id", item));
                                    attraction.setLocation(getFromJson("name", item));
                                    attraction.setRating(getFromJson("rating", item));
                                    attraction.setAddress(getFromJson("address", item));
                                    attraction.setLink(getURLFromJson("website", item));
                                    attraction.setDescription(getFromJson("description", item));
                                    attraction.setBookingURL(getBookingURLFromJson(item));
                                    attraction.setImageURL(getImageURLFromJson(item));
                                    attraction.setCategory(getFromJsonArray("subcategory", "name", item));

                                    list.add(attraction);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error. Please try again.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    adapter = new AttractionsAdapter(getContext(), list);
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(ProgressBar.GONE);
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
        searchAttractions.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(searchAttractions);
    }

    private String getBookingURLFromJson(JSONObject json) throws JSONException {
        if (json.isNull("booking")) {
            return null;
        } else {
            JSONObject booking = json.getJSONObject("booking");
            return getURLFromJson("url", booking);
        }
    }
}