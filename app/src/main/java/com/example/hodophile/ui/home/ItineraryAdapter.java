package com.example.hodophile.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.R;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;

/**
 * ItineraryAdapter class is an adapter for
 * recycler views used in the itinerary fragment.
 */
public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.MyViewHolder> {

    Context context;
    ArrayList<com.example.hodophile.ui.home.ItineraryItem> list;

    public ItineraryAdapter(Context context, ArrayList<com.example.hodophile.ui.home.ItineraryItem> list) {
        this.context = context;
        this.list = list;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView location, time;
        ConstraintLayout itemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.itineraryLocation);
            time = itemView.findViewById(R.id.itineraryTime);
            itemLayout = itemView.findViewById(R.id.itineraryItemLayout);
        }
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.itinerary_item, parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        com.example.hodophile.ui.home.ItineraryItem item = list.get(position);
        holder.location.setText(item.getLocation());
        holder.time.setText(String.format("%s - %s", item.getStartTime().toString(), item.getEndTime().toString()));

        holder.itemLayout.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("Saved", true);
            bundle.putString("Date", item.getDate());
            bundle.putString("Location", item.getLocation());
            if (item.getType() == 1) {
                Navigation.findNavController(v).navigate(
                        R.id.action_itineraryFragment_to_accommodationDetailFragment, bundle);
            } else if (item.getType() == 2) {
                Navigation.findNavController(v).navigate(
                        R.id.action_itineraryFragment_to_attractionDetailFragment, bundle);
            } else if (item.getType() == 3) {
                Navigation.findNavController(v).navigate(
                        R.id.action_itineraryFragment_to_foodDetailFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
