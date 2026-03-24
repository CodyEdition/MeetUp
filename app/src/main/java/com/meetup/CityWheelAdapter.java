package com.meetup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityWheelAdapter extends RecyclerView.Adapter<CityWheelAdapter.CityViewHolder> {

    public interface OnCityClickListener {
        void onCityClick(String city);
    }

    private final List<String> cities;
    private final OnCityClickListener listener;

    public CityWheelAdapter(List<String> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_wheel, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        String city = cities.get(position);
        holder.cityNameText.setText(city);
        holder.itemView.setOnClickListener(v -> listener.onCityClick(city));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static final class CityViewHolder extends RecyclerView.ViewHolder {
        final TextView cityNameText;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            cityNameText = itemView.findViewById(R.id.cityNameText);
        }
    }
}
