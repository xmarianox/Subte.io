package la.funka.subteio.adapters;

import android.support.v7.widget.RecyclerView;
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
public class StationItemAdapter extends RecyclerView.Adapter<StationItemAdapter.ViewHolder> {

    private static final String LOG_TAG = StationItemAdapter.class.getSimpleName();

    private RealmResults<SubwayStation> dataset;
    private int itemLayout;

    public StationItemAdapter(RealmResults<SubwayStation> data, int itemLayout) {
        dataset = data;
        this.itemLayout = itemLayout;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        public TextView stationText;

        public ViewHolder(View itemView){
            super(itemView);

            itemView.setOnClickListener(this);

            stationText = (TextView) itemView.findViewById(R.id.station_name);
        }

        @Override
        public void onClick(View v) {

        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SubwayStation stationItem = dataset.get(position);

        holder.stationText.setText(stationItem.getStation_name());

        //holder set tag
        holder.itemView.setTag(stationItem);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
