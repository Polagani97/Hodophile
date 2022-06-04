package com.example.hodophile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class SearchFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void getLocationID(String location, RequestQueue requestQueue) {
        JsonObjectRequest searchLocation = new JsonObjectRequest(Request.Method.GET, searchURl(location), null, response -> {
            try {
                JSONArray arr = response.getJSONArray("data");
                JSONObject result = arr.getJSONObject(0);
                String locationID = result.getJSONObject("result_object").getString("location_id");
                extractInfo(locationID);
            } catch (JSONException e) {
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
        searchLocation.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(searchLocation);
    }

    public String searchURl(String location) {
        try {
            return "https://travel-advisor.p.rapidapi.com/locations/search?query=" + URLEncoder.encode(location, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract void extractInfo(String locationID);

    public String getFromJsonArray(String tag1, String tag2, JSONObject json) throws JSONException {
        if (json.isNull(tag1) || json.getJSONArray(tag1).length() == 0) {
            return "Not Available";
        } else {
            JSONArray items = json.getJSONArray(tag1);
            StringBuilder result = new StringBuilder(items.getJSONObject(0).getString(tag2));
            for (int j = 1; j < items.length(); j++) {
                JSONObject item = items.getJSONObject(j);
                result.append(", ").append(item.getString(tag2));
            }
            return result.toString();
        }
    }

    public String getFromJson(String tag, JSONObject json) throws JSONException {
        if (json.isNull(tag)) {
            return "Not Available";
        } else if (json.getString(tag).isEmpty()) {
            return "Not Available";
        } else {
            return json.getString(tag);
        }
    }

    public String getURLFromJson(String tag, JSONObject json) throws JSONException {
        if (json.isNull(tag)) {
            return null;
        } else if (json.getString(tag).isEmpty()) {
            return null;
        } else {
            return json.getString(tag);
        }
    }

    public String getImageURLFromJson(JSONObject json) throws JSONException {
        if (json.isNull("photo") || json.getJSONObject("photo").isNull("images")) {
            return null;
        } else if (json.getJSONObject("photo").getJSONObject("images").isNull("medium")) {
            return null;
        } else {
            return getURLFromJson("url", json.getJSONObject("photo")
                    .getJSONObject("images").getJSONObject("medium"));
        }
    }
}
