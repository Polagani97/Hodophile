package com.example.hodophile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

        Context context;
        ArrayList<com.example.hodophile.ReviewItem> list;

public ReviewAdapter(Context context, ArrayList<com.example.hodophile.ReviewItem> list) {
        this.context = context;
        this.list = list;
        }

public static class ReviewViewHolder extends RecyclerView.ViewHolder {

    TextView username, review, date;
    RatingBar rating;
    ShapeableImageView image;

    public ReviewViewHolder(View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.reviewUsername);
        review = itemView.findViewById(R.id.reviewText);
        date = itemView.findViewById(R.id.reviewDate);
        rating = itemView.findViewById(R.id.reviewRating);
        image = itemView.findViewById(R.id.userPicture);
    }
}

    @NonNull
    @NotNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.review_item, parent,false);
        return new ReviewAdapter.ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewViewHolder holder, int position) {
        com.example.hodophile.ReviewItem item = list.get(position);

        DatabaseReference database = FirebaseDatabase
                .getInstance("https://orbital-le-voyage-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Profiles").child(item.getUserID());
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                holder.username.setText(username);
                String imageURL = snapshot.child("image").getValue(String.class);
                Picasso.get().load(imageURL).placeholder(R.mipmap.default_profile_picture)
                        .fit().into(holder.image);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context,
                        "Unable to retrieve profile information", Toast.LENGTH_SHORT).show();
            }
        });

        holder.date.setText(item.getDate());
        holder.review.setText(item.getReview());
        holder.rating.setRating(item.getRating());

        holder.username.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userID", item.getUserID());
            Navigation.findNavController(v).navigate(R.id.profileFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}