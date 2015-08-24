package la.funka.subteio.adapters;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import io.realm.RealmResults;
import la.funka.subteio.R;
import la.funka.subteio.model.SubwayStation;

/**
 * Created by Mariano Molina on 15/8/15.
 * Twitter: @xsincrueldadx
 */
public class StationItemAdapter extends RecyclerView.Adapter<StationItemAdapter.StationViewHolder>{

    private static final String TAG = "StationItemAdapter";

    private RealmResults<SubwayStation> items;
    private int itemLayout;

    public StationItemAdapter(RealmResults<SubwayStation> dataset, int layout) {
        items = dataset;
        this.itemLayout = layout;
    }

    public static class StationViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        //
        public TextView stationName;
        public CardView cardView;

        public StationViewHolder(View itemView) {
            super(itemView);

            // set onClick
            itemView.setOnClickListener(this);

            // set views
            cardView = (CardView) itemView.findViewById(R.id.station_item);
            stationName = (TextView) itemView.findViewById(R.id.station_name);
        }

        //
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Click en el item: " + stationName.getText());
        }
    }

    @Override
    public StationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StationViewHolder holder, int position) {
        final SubwayStation subwayStationItem = items.get(position);

        Log.d(TAG, "Item recycler: " + subwayStationItem.getStation_name());

        // set text
        holder.stationName.setText(subwayStationItem.getStation_name());

        // set Tag
        holder.itemView.setTag(subwayStationItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
