package com.example.hodophile.ui.attractions;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.R;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AttractionsAdapter extends RecyclerView.Adapter<AttractionsAdapter.AttractionViewHolder> {

    Context context;

    ArrayList<AttractionItineraryItem> list;

    public AttractionsAdapter(Context context, ArrayList<AttractionItineraryItem> list) {
        this.context = context;
        this.list = list;
    }

    public static class AttractionViewHolder extends RecyclerView.ViewHolder {

        TextView name, address, rating, price;
        ImageView image;
        ConstraintLayout layout;

        public AttractionViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.location);
            address = itemView.findViewById(R.id.itemAddress);
            rating = itemView.findViewById(R.id.itemRating);
            price = itemView.findViewById(R.id.itemPrice);
            image = itemView.findViewById(R.id.itemImage);
            layout = itemView.findViewById(R.id.itemLayout);
        }
    }

    @NonNull
    @NotNull
    @Override
    public AttractionsAdapter.AttractionViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.search_result, parent,false);
        return new AttractionsAdapter.AttractionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AttractionsAdapter.AttractionViewHolder holder, int position) {
        AttractionItineraryItem item = list.get(position);
        holder.name.setText(item.getLocation());
        holder.rating.setText(item.getRating());
        holder.price.setText(item.getCategory());
        holder.address.setVisibility(View.GONE);
        Picasso.get().load(item.getImageURL())
                .placeholder(R.drawable.error_placeholder_small).fit().into(holder.image);

        holder.layout.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("Attraction", item);
            bundle.putBoolean("Saved", false);
            Navigation.findNavController(v).navigate(
                    R.id.action_nav_attractions_to_attractionDetailFragment, bundle);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
