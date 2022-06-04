package com.example.hodophile.ui.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hodophile.R;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    Context context;
    List<com.example.hodophile.ui.weather.WeatherItem> list;

    public WeatherAdapter(Context context, List<com.example.hodophile.ui.weather.WeatherItem> list) {
        this.context = context;
        this.list = list;
        }

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {

        ImageView iconView;
        TextView tempView, dateView;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.weather_icon_lv);
            tempView = itemView.findViewById(R.id.temperature_lv);
            dateView = itemView.findViewById(R.id.date_lv);
        }
    }

    @NonNull
    @NotNull
    @Override
    public WeatherAdapter.WeatherViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.weather_listview_item, parent,false);
        return new WeatherAdapter.WeatherViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WeatherAdapter.WeatherViewHolder holder, int position) {
        com.example.hodophile.ui.weather.WeatherItem item = list.get(position);
        holder.tempView.setText(String.format("%s°C", item.getTemperature()));
        holder.dateView.setText(item.getDate());
        int resourceID = context.getResources().getIdentifier(item.getIcon(), "drawable", context.getPackageName());
        holder.iconView.setImageResource(resourceID);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}