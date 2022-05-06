package com.example.cyclosens.activities;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cyclosens.R;
import com.example.cyclosens.classes.Activity;

import java.util.ArrayList;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>{
    private static final String TAG = ActivitiesAdapter.class.getSimpleName(); //POUR LES LOG
    private final ArrayList<Activity> activities;
    private final int itemResource;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameActivity;
        private final TextView dateActivity;
        private Activity activity;

        public ViewHolder(View view) {
            super(view);
            this.nameActivity = (TextView) itemView.findViewById(R.id.nameActivity);
            this.dateActivity = (TextView) itemView.findViewById(R.id.dateActivity);

            view.setOnClickListener(v -> {
                if (this.activity != null) {
                    Intent intent = new Intent(itemView.getContext(), ActivityInformation.class);
                    intent.putExtra("activity", this.activity);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    public ActivitiesAdapter(int itemResource, ArrayList<Activity> activities) {
        this.activities = activities;
        this.itemResource = itemResource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemResource,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder  holder, int position) {
        holder.activity = activities.get(position);
        holder.nameActivity.setText(holder.activity.getNameActivity());
        holder.dateActivity.setText(holder.activity.getDateActivity());
    }

    @Override
    public int getItemCount() {
        return this.activities.size();
    }
}
