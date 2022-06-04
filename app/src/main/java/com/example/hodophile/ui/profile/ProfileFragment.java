package com.example.hodophile.ui.profile;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hodophile.R;
import com.example.hodophile.ReviewItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FirebaseUser user;
    private TextView username, email;
    private ShapeableImageView image, editPicture;
    private Button editBtn;
    private RecyclerView reviewsRecycler;
    private DatabaseReference database;
    private List<ReviewItem> list;
    private com.example.hodophile.ui.profile.ProfileReviewsAdapter adapter;
    private ActivityResultLauncher<String> mGetContent;
    private Map<String, Object> updates;
    com.example.hodophile.ui.profile.UserProfile profile;
    private boolean myProfile;
    private String userID;
    private Uri newUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            myProfile = user.getUid().equals(getArguments().getString("userID"));
        }

        mGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    editPicture.setImageURI(uri);
                    newUri = uri;
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        username = view.findViewById(R.id.profilePageUsername);
        email = view.findViewById(R.id.profilePageEmail);
        image = view.findViewById(R.id.profilePagePicture);
        editBtn = view.findViewById(R.id.editProfileButton);
        reviewsRecycler = view.findViewById(R.id.profileReviewsRecycler);

        if (myProfile) {
            email.setText(user.getEmail());
            userID = user.getUid();
        } else {
            userID = getArguments().getString("userID");
            email.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
        }

        database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Profiles").child(userID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                profile = snapshot.getValue(com.example.hodophile.ui.profile.UserProfile.class);
                username.setText(profile.getUsername());
                Picasso.get().load(profile.getImage()).placeholder(R.mipmap.default_profile_picture)
                        .fit().into(image);
                Map<String, ReviewItem> reviews = profile.getReviews();
                if (reviews != null) {
                    list = new ArrayList<>(reviews.values());
                    reviewsRecycler.setHasFixedSize(true);
                    reviewsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    adapter = new com.example.hodophile.ui.profile.ProfileReviewsAdapter(getContext(), list);
                    reviewsRecycler.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        editBtn.setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            View popupView = getLayoutInflater().inflate(R.layout.profile_popup, null);
            EditText editUsername = popupView.findViewById(R.id.editUsername);
            Button editProfileBtn = popupView.findViewById(R.id.editButton);
            Button changePic = popupView.findViewById(R.id.changePictureBtn);
            editPicture = popupView.findViewById(R.id.editProfilePicture);
            ImageButton closeBtn = popupView.findViewById(R.id.editProfileClose);
            updates = new HashMap<>();
            editUsername.setText(profile.getUsername());
            Picasso.get().load(profile.getImage()).placeholder(R.mipmap.default_profile_picture)
                    .fit().into(editPicture);

            dialogBuilder.setView(popupView);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            changePic.setOnClickListener(v1 -> mGetContent.launch("image/*"));

            editProfileBtn.setOnClickListener(v12 -> {
                if (newUri != null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference()
                            .child("images").child(userID + ".jpg");
                    UploadTask uploadTask = storageRef.putFile(newUri);
                    uploadTask.addOnFailureListener(exception -> Toast.makeText(getContext(),
                            "Upload failed", Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                                    .addOnCompleteListener(task -> {
                                        String imageURL = task.getResult().toString();
                                        updates.put("image", imageURL);
                                        String newUsername = editUsername.getText().toString();
                                        if (newUsername.isEmpty()) {
                                            editUsername.setError("Please enter a username");
                                            editUsername.requestFocus();
                                        } else {
                                            updates.put("username", newUsername);
                                            database.updateChildren(updates);
                                            dialog.dismiss();
                                        }
                                    }));
                } else {
                    String newUsername = editUsername.getText().toString();
                    if (newUsername.isEmpty()) {
                        editUsername.setError("Please enter a username");
                        editUsername.requestFocus();
                    } else {
                        updates.put("username", newUsername);
                        database.updateChildren(updates);
                        dialog.dismiss();
                    }
                }
            });

            closeBtn.setOnClickListener(x -> dialog.dismiss());

        });


    }
}