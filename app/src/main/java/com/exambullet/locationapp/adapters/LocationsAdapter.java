package com.exambullet.locationapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exambullet.locationapp.LocationData;
import com.exambullet.locationapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> implements Filterable {
    List<LocationData> locationDataList;
    List<LocationData> locationDataFull;
    Calendar calendar;

    public LocationsAdapter(List<LocationData> locationDataList,Calendar calendar) {
        this.locationDataList = locationDataList;
        this.calendar=calendar;
        locationDataFull=new ArrayList<>();
        locationDataFull.addAll(locationDataList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.location_card,parent,false);
        return new LocationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.latitude.setText("Latitude : "+locationDataList.get(position).getLocation().getLatitude());
        holder.locationName.setText("Location Number : "+(position+1));
        holder.otherInfo.setText("Data : "+locationDataList.get(position).getOtherInfo());
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(locationDataList.get(position).getLocation().getTime());
        holder.date.setText(new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(calendar.getTime()));
        holder.time.setText(new SimpleDateFormat("HH:mm", Locale.US).format(calendar.getTime()));
        holder.tripNumber.setText("Trip Number : "+ ((locationDataList.get(position).getTripNumber()==-1) ? "NA" : Integer.toString(locationDataList.get(position).getTripNumber())));
        holder.odometerStart.setText("Odometer at start : "+ ((locationDataList.get(position).getOdometerStart()==-1) ? "NA" : Double.toString(locationDataList.get(position).getOdometerStart())));
        holder.odometerEnd.setText("Odometer at end : "+ ((locationDataList.get(position).getOdometerEnd()==-1) ? "NA" : Double.toString(locationDataList.get(position).getOdometerEnd())));
    }

    @Override
    public int getItemCount() {
        return locationDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView locationName,latitude,otherInfo,date,time,tripNumber,odometerStart,odometerEnd;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName=itemView.findViewById(R.id.locationName);
            latitude=itemView.findViewById(R.id.latitude);
            otherInfo=itemView.findViewById(R.id.otherinfo);
            date=itemView.findViewById(R.id.date);
            time=itemView.findViewById(R.id.time);
            tripNumber=itemView.findViewById(R.id.tripNumber);
            odometerStart=itemView.findViewById(R.id.tripNumber2);
            odometerEnd=itemView.findViewById(R.id.tripNumber3);
        }
    }
    @Override
    public Filter getFilter() {
        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<LocationData> filteredList = new ArrayList<>();
                for (LocationData item : locationDataFull) {
                    Calendar ccalendar=Calendar.getInstance();
                    ccalendar.setTimeInMillis(item.getLocation().getTime());
                    SimpleDateFormat format=new SimpleDateFormat("dd-MMM-yyyy",Locale.US);
                    if (format.format(ccalendar.getTime()).equals(format.format(calendar.getTime()))) {
                        filteredList.add(item);
                    }
                }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            locationDataList.clear();
            locationDataList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
