package com.example.hodophile.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.R;
import com.example.hodophile.ReviewItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class ProfileReviewsAdapter extends RecyclerView.Adapter<ProfileReviewsAdapter.ProfileReviewViewHolder> {

    Context context;
    List<ReviewItem> list;

    public ProfileReviewsAdapter(Context context, List<ReviewItem> list) {
        this.context = context;
        this.list = list;
    }

    public static class ProfileReviewViewHolder extends RecyclerView.ViewHolder {

        TextView location, review, date;
        RatingBar rating;
        ShapeableImageView image;

        public ProfileReviewViewHolder(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.reviewUsername);
            review = itemView.findViewById(R.id.reviewText);
            date = itemView.findViewById(R.id.reviewDate);
            rating = itemView.findViewById(R.id.reviewRating);
            image = itemView.findViewById(R.id.userPicture);
        }
    }

    @NonNull
    @NotNull
    @Override
    public ProfileReviewsAdapter.ProfileReviewViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.review_item, parent,false);
        return new ProfileReviewsAdapter.ProfileReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProfileReviewsAdapter.ProfileReviewViewHolder holder, int position) {
        ReviewItem item = list.get(position);
        holder.image.setVisibility(View.GONE);
        holder.location.setText(item.getLocation());
        holder.date.setText(item.getDate());
        holder.review.setText(item.getReview());
        holder.rating.setRating(item.getRating());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
