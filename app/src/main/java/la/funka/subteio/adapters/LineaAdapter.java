package la.funka.subteio.adapters;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.RealmResults;
import la.funka.subteio.DetalleLineaActivity;
import la.funka.subteio.R;
import la.funka.subteio.model.SubwayLine;
import la.funka.subteio.utils.Util;

/**
 * Created by Mariano Molina on 01/07/2015.
 * Twitter: @xsincrueldadx
 */
public class LineaAdapter extends RecyclerView.Adapter<LineaAdapter.ViewHolder> {

    private static final String LOG_TAG = LineaAdapter.class.getSimpleName();
    private Util utils;

    private RealmResults<SubwayLine> lineas;
    private int itemLayout;

    public LineaAdapter(RealmResults<SubwayLine> data, int itemLayout, Util util) {
        lineas = data;
        this.itemLayout = itemLayout;
        this.utils = util;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        public TextView status;
        public ImageView image_line;
        public TextView frequency;
        public CardView cardView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            
            itemView.setOnClickListener(this);

            frequency  = (TextView) itemView.findViewById(R.id.linea_frequency);
            status     = (TextView) itemView.findViewById(R.id.linea_status);
            image_line = (ImageView) itemView.findViewById(R.id.image_line);
            cardView   = (CardView) itemView.findViewById(R.id.card_view);
        }

        @Override
        public void onClick(View v) {
            Intent intentDetalle = new Intent(v.getContext(), DetalleLineaActivity.class);
            intentDetalle.putExtra("NOMBRE_LINEA", image_line.getContentDescription());
            v.getContext().startActivity(intentDetalle);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SubwayLine linea = lineas.get(position);
        // Set atributo linea.
        holder.image_line.setContentDescription(linea.getLineName());
        // Set status linea.
        holder.status.setText(linea.getLineStatus());
        // Set frecuencia.
        double frequency = utils.calculateFrequency(linea.getLineFrequency());
        if (frequency != 0.0){
            holder.frequency.setText(String.valueOf(Math.round(frequency))+" min");
        }else{
            holder.frequency.setText("Sin Estimación");
        }
        // Set
        switch (linea.getLineName()){
            case "A":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_a);
                break;

            case "B":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_b);
                break;

            case "C":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_c);
                break;

            case "D":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_d);
                break;

            case "E":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_e);
                break;
            
            case "H":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_h);
                break;
            
            case "P":
                holder.image_line.setImageResource(R.drawable.ic_item_linea_p);
                break;

            //case "U":
            //    holder.image_line.setImageResource(R.drawable.ic_item_linea_a);
            //    break;
        }
        holder.itemView.setTag(linea);
    }

    @Override
    public int getItemCount() {
        return lineas.size();
    }
    
  
}
