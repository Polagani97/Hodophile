package com.example.hodophile.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.example.hodophile.RecyclerItemTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItineraryFragment extends Fragment {

    private String date, userID;
    private RecyclerView recyclerView;
    private DatabaseReference database;
    private ItineraryAdapter adapter;
    private ArrayList<com.example.hodophile.ui.home.ItineraryItem> list;
    private TextView start, end;
    private EditText location;
    private FloatingActionButton fab;
    private Button btn;
    private ImageButton closeBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data passed from home fragment
        if (getArguments() != null) {
            date = getArguments().getString("DATE");
            userID = getArguments().getString("UserID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler);
        database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Users").child(userID).child("Itinerary");
        fab = view.findViewById(R.id.fab);

        // Set header
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(date);

        // Fill up recycler view with data from database
        database.child(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                list = new ArrayList<>();
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new ItineraryAdapter(getContext(), list);
                recyclerView.setAdapter(adapter);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    com.example.hodophile.ui.home.ItineraryItem itineraryItem = dataSnapshot.getValue(com.example.hodophile.ui.home.ItineraryItem.class);
                    list.add(itineraryItem);
                }
                list.sort(com.example.hodophile.ui.home.ItineraryItem::compareTo);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        // Pop up for adding new event to itinerary
        fab.setOnClickListener(v -> {
            AlertDialog.Builder addDialogBuilder = new AlertDialog.Builder(getContext());
            View popupView = getLayoutInflater().inflate(R.layout.itinerary_popup, null);
            location = popupView.findViewById(R.id.popupEvent);
            start = popupView.findViewById(R.id.popupStart);
            end = popupView.findViewById(R.id.popupEnd);
            btn = popupView.findViewById(R.id.popupButton);
            closeBtn = popupView.findViewById(R.id.popupClose);
            com.example.hodophile.ui.home.TimeParcel st = new com.example.hodophile.ui.home.TimeParcel();
            com.example.hodophile.ui.home.TimeParcel et = new com.example.hodophile.ui.home.TimeParcel();

            start.setOnClickListener(t -> {
                TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                    st.setHr(hourOfDay);
                    st.setMin(minute);
                    start.setText(st.toString());
                    start.setError(null);
                }, 0, 0, false);
                timePicker.show();
            });

            end.setOnClickListener(t -> {
                TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view12, hourOfDay, minute) -> {
                    et.setHr(hourOfDay);
                    et.setMin(minute);
                    end.setText(et.toString());
                    end.setError(null);
                }, 0, 0, false);
                timePicker.show();
            });

            addDialogBuilder.setView(popupView);
            AlertDialog addDialog = addDialogBuilder.create();
            addDialog.show();
            btn.setOnClickListener(v1 -> {
                String loc = location.getText().toString();
                if (loc.isEmpty()) {
                    location.setError("Please key in an event");
                    location.requestFocus();
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
                    com.example.hodophile.ui.home.ItineraryItem overlap = checkOverlap(list, st, et);
                    if (overlap != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Overlapping Events");
                        builder.setMessage(String.format(
                                "This new event overlaps with %s. Are you sure you want to add this event to your itinerary?",
                                overlap.getLocation()));
                        builder.setPositiveButton("Confirm", (dg, which) -> {
                            com.example.hodophile.ui.home.ItineraryItem item = new com.example.hodophile.ui.home.ItineraryItem(loc, date, st, et);
                            database.child(date).child(loc).setValue(item);
                            addDialog.dismiss();
                        });
                        builder.setNegativeButton("Cancel", null);
                        AlertDialog confirmationDialog = builder.create();
                        confirmationDialog.show();
                    } else {
                        com.example.hodophile.ui.home.ItineraryItem item = new com.example.hodophile.ui.home.ItineraryItem(loc, date, st, et);
                        database.child(date).child(loc).setValue(item);
                        addDialog.dismiss();
                    }
                }
            });

            closeBtn.setOnClickListener(x -> addDialog.dismiss());
        });

        ItemTouchHelper.SimpleCallback callback = new RecyclerItemTouchHelper(getContext()) {
            @Override
            public void deleteItem(int position) {
                AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(getContext());
                deleteDialogBuilder.setTitle("Delete Event");
                com.example.hodophile.ui.home.ItineraryItem deleteItem = list.get(position);
                deleteDialogBuilder.setMessage(String.format("Are you sure you want to delete %s from your itinerary?",
                        deleteItem.getLocation()));
                deleteDialogBuilder.setPositiveButton("Confirm", (dialog, which) -> {
                    com.example.hodophile.ui.home.ItineraryItem deleted = list.remove(position);
                    adapter.notifyItemRemoved(position);
                    database.child(date).child(deleted.getLocation()).removeValue()
                            .addOnCompleteListener(t -> Toast.makeText(getContext(),
                                    "Event deleted from itinerary", Toast.LENGTH_SHORT).show());
                });
                deleteDialogBuilder.setNegativeButton("Cancel",
                        (dialog, which) -> adapter.notifyItemChanged(position));
                AlertDialog deleteDialog = deleteDialogBuilder.create();
                deleteDialog.show();
            }

            @Override
            public void editItem(int position) {
                AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(getContext());
                View popupView = LayoutInflater.from(getContext()).inflate(R.layout.edit_itinerary_popup, null);
                TextView event = popupView.findViewById(R.id.editPopupEvent);
                TextView date = popupView.findViewById(R.id.editPopupDate);
                TextView start = popupView.findViewById(R.id.editPopupStart);
                TextView end = popupView.findViewById(R.id.editPopupEnd);
                Button updateBtn = popupView.findViewById(R.id.editPopupUpdate);
                ImageButton closeBtn = popupView.findViewById(R.id.editPopupClose);

                com.example.hodophile.ui.home.ItineraryItem item = list.remove(position);
                com.example.hodophile.ui.home.TimeParcel st = new com.example.hodophile.ui.home.TimeParcel(item.getStartTime().getHr(), item.getStartTime().getMin());
                com.example.hodophile.ui.home.TimeParcel et = new com.example.hodophile.ui.home.TimeParcel(item.getEndTime().getHr(), item.getEndTime().getMin());
                event.setText(item.getLocation());
                date.setText(item.getDate());
                start.setText(st.toString());
                end.setText(et.toString());

                Map<String, Object> updates = new HashMap<>();

                editDialogBuilder.setView(popupView);
                AlertDialog editDialog = editDialogBuilder.create();
                editDialog.show();

                start.setOnClickListener(t -> {
                    TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view13, hourOfDay, minute) -> {
                        if (hourOfDay != st.getHr() || minute != st.getMin()) {
                            st.setHr(hourOfDay);
                            st.setMin(minute);
                            updates.put("startTime", st);
                            start.setText(st.toString());
                        }
                    }, 0, 0, false);
                    timePicker.show();
                });

                end.setOnClickListener(t -> {
                    TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view14, hourOfDay, minute) -> {
                        if (hourOfDay != et.getHr() || minute != et.getMin()) {
                            et.setHr(hourOfDay);
                            et.setMin(minute);
                            updates.put("endTime", et);
                            end.setText(et.toString());
                        }
                    }, 0, 0, false);
                    timePicker.show();
                });

                date.setOnClickListener(t -> {
                    DatePickerDialog datePicker = new DatePickerDialog(getContext());
                    datePicker.setOnDateSetListener((view15, year, month, dayOfMonth) -> {
                        LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                        String dateString = localDate.format(formatter);
                        if (!item.getDate().equals(dateString)) {
                            date.setText(dateString);
                            updates.put("date", dateString);
                        }
                    });
                    datePicker.show();
                });

                updateBtn.setOnClickListener(v -> {
                    if (updates.isEmpty()) {
                        editDialog.dismiss();
                        list.add(position, item);
                        adapter.notifyItemChanged(position);
                    } else if (et.compareTo(st) < 0) {
                        end.setError("End time is earlier than start time");
                        end.requestFocus();
                    } else if (updates.containsKey("date")) {
                        database.child((String)updates.get("date")).get().addOnCompleteListener(task -> {
                            List<com.example.hodophile.ui.home.ItineraryItem> itineraryList = new ArrayList<>();
                            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                                com.example.hodophile.ui.home.ItineraryItem itineraryItem = dataSnapshot.getValue(com.example.hodophile.ui.home.ItineraryItem.class);
                                itineraryList.add(itineraryItem);
                            }
                            itineraryList.sort(com.example.hodophile.ui.home.ItineraryItem::compareTo);
                            com.example.hodophile.ui.home.ItineraryItem overlap = checkOverlap(itineraryList, st, et);
                            if (overlap != null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Overlapping Events");
                                builder.setMessage(String.format(
                                        "This edited event overlaps with %s. Are you sure you want to edit this event?",
                                        overlap.getLocation()));
                                builder.setPositiveButton("Confirm", (dg, which) -> {
                                    updateDatabase(updates, item);
                                    editDialog.dismiss();
                                });
                                builder.setNegativeButton("Cancel", null);
                                AlertDialog confirmationDialog = builder.create();
                                confirmationDialog.show();
                            } else {
                                updateDatabase(updates, item);
                                editDialog.dismiss();
                            }
                        });
                    } else {
                        com.example.hodophile.ui.home.ItineraryItem overlap = checkOverlap(list, st, et);
                        if (overlap != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Overlapping Events");
                            builder.setMessage(String.format(
                                    "This edited event overlaps with %s. Are you sure you want to edit this event?",
                                    overlap.getLocation()));
                            builder.setPositiveButton("Confirm", (dg, which) -> {
                                updateDatabase(updates, item);
                                editDialog.dismiss();
                            });
                            builder.setNegativeButton("Cancel", null);
                            AlertDialog confirmationDialog = builder.create();
                            confirmationDialog.show();
                        } else {
                            updateDatabase(updates, item);
                            editDialog.dismiss();
                        }
                    }
                });

                closeBtn.setOnClickListener(t -> {
                    editDialog.dismiss();
                    list.add(position, item);
                    adapter.notifyItemChanged(position);
                });
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private com.example.hodophile.ui.home.ItineraryItem checkOverlap(List<com.example.hodophile.ui.home.ItineraryItem> itineraryList, com.example.hodophile.ui.home.TimeParcel start, com.example.hodophile.ui.home.TimeParcel end) {
        for (int i = 0; i < itineraryList.size(); i++) {
            com.example.hodophile.ui.home.ItineraryItem item = itineraryList.get(i);
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

    private void updateDatabase(Map<String, Object> updates, com.example.hodophile.ui.home.ItineraryItem item) {
        if (updates.containsKey("date")) {
            String newDate = (String) updates.get("date");
            DatabaseReference oldReference = database.child(item.getDate()).child(item.getLocation());
            DatabaseReference newReference = database.child(newDate).child(item.getLocation());
            oldReference.get().addOnCompleteListener(task ->
                    newReference.setValue(task.getResult().getValue(), (firebaseError, firebase) -> {
                if (firebaseError != null) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                } else {
                    newReference.updateChildren(updates)
                            .addOnCompleteListener(t -> {
                                Toast.makeText(getContext(),
                                        "Itinerary updated", Toast.LENGTH_SHORT).show();
                                oldReference.removeValue();
                            });
                }
            }));
        } else {
            database.child(item.getDate()).child(item.getLocation()).updateChildren(updates)
                    .addOnCompleteListener(t -> Toast.makeText(getContext(),
                            "Itinerary updated", Toast.LENGTH_SHORT).show());
        }
    }
}