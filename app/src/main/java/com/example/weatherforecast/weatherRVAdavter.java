package com.example.weatherforecast;

import static android.util.Log.println;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class weatherRVAdavter extends RecyclerView.Adapter<weatherRVAdavter.ViewHolder> {
   private Context context;
    private ArrayList<weatherRVModal> weatherRVModalArrayList;

    public weatherRVAdavter(Context context, ArrayList<weatherRVModal> weatherRVModalArrayList) {
        this.context = context;
        this.weatherRVModalArrayList = weatherRVModalArrayList;
    }


    @NonNull
    @Override
    public weatherRVAdavter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);
       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull weatherRVAdavter.ViewHolder holder, int position) {
        weatherRVModal modal =weatherRVModalArrayList.get(position);
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd hh:mm");
        SimpleDateFormat output =new SimpleDateFormat("hh:mm aa");


        try {
          //  Log.d("Umang", holder.toString()+ holder.temp.toString());
            Date t =input.parse(modal.getTime());
            holder.time.setText(output.format(t));

            holder.temp.setText(modal.getTemp()+"°C");
            holder.windSpeed.setText(modal.getWindSpeed()+" Km/hr");
        }
        catch (ParseException e)
        {
           e.printStackTrace();
        }

        String iconURL="https:"+modal.getIcon();
        Log.d("Umang", iconURL);
        Picasso.get().load(iconURL).into(holder.weatherConditions);
    }

    @Override
    public int getItemCount() {
        return weatherRVModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private TextView temp;
        private TextView windSpeed;
        private ImageView weatherConditions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            time= itemView.findViewById(R.id.idTvTime);
            temp= itemView.findViewById(R.id.idTvTemp);
            windSpeed= itemView.findViewById(R.id.idTvWind);
            weatherConditions= itemView.findViewById(R.id.idTvConditions);
        }
    }
}
