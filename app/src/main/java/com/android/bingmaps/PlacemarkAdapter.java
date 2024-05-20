package com.android.bingmaps;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlacemarkAdapter extends RecyclerView.Adapter<PlacemarkAdapter.PlacemarkViewHolder> {

    private ArrayList<Placemark> placemarkList;
    private Context context;
    private String category="";
    private GoToLocation goToLocation;
    public PlacemarkAdapter(Context context, ArrayList<Placemark> placemarkList,GoToLocation goToLocation) {
        this.context = context;
        this.placemarkList = placemarkList;
        this.goToLocation = goToLocation;
    }

    @NonNull
    @Override
    public PlacemarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recyclerview, parent, false);
        return new PlacemarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacemarkViewHolder holder, int position) {
        Placemark placemark = placemarkList.get(position);
            Log.i("MARDUK","Name: "+placemark.getName()+" Color: "+placemark.getColor());
            holder.llayoutBottom.setBackgroundColor(Color.parseColor(placemark.getColor()));
//            holder.tvCategory.setText(Html.fromHtml("<b>Category:</b> " +category));
            holder.tvLocation.setText(Html.fromHtml("<b>Location:</b> " + placemark.getName()));
            holder.tvDescriptions.setText(Html.fromHtml("<b>Description:</b> " + placemark.getDescription()));
            String[] parts = placemark.getCoordinates().split(",");
            String cordinates=parts[1].trim() + "," + parts[0].trim();
            holder.tvGeoPoint.setText(Html.fromHtml("<b>Coordinates:</b> " + cordinates));
            holder.imgCopy.setOnClickListener(v -> {
                // Copy coordinates to clipboard
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Coordinates",cordinates );
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            holder.imgOpenWithGoogleMaps.setOnClickListener(v -> {
                String uri = "http://maps.google.com/maps?q=loc:" + cordinates;
                context.startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(uri)));
            });
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLocation.goToLocation(cordinates);
                }
            });


    }

    @Override
    public int getItemCount() {
        return placemarkList.size();
    }

    public static class PlacemarkViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory, tvLocation, tvDescriptions, tvGeoPoint,tvCategoryTitle;
        ImageView imgCopy, imgOpenWithGoogleMaps;
        LinearLayout llayoutBottom,llayoutMain;
        CardView cardView;
        public PlacemarkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescriptions = itemView.findViewById(R.id.tvDescriptions);
            tvGeoPoint = itemView.findViewById(R.id.tvGeoPoint);
            imgCopy = itemView.findViewById(R.id.imgCopy);
            imgOpenWithGoogleMaps = itemView.findViewById(R.id.imgOpenWithGoogleMaps);
            llayoutBottom = itemView.findViewById(R.id.llayoutBottom);
            cardView = itemView.findViewById(R.id.cardView);
            llayoutMain = itemView.findViewById(R.id.llayoutMain);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
        }
    }
}
