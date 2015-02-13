package la.funka.subteio;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LineaAdapter extends RecyclerView.Adapter<LineaAdapter.ViewHolder> {

    private static final String LOG_TAG = LineaAdapter.class.getSimpleName();

    private ArrayList<Linea> lineas;
    private int itemLayout;

    public LineaAdapter(ArrayList<Linea> data, int itemLayout) {
        lineas = data;
        this.itemLayout = itemLayout;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {
        public TextView name;
        public TextView status;
        public LinearLayout content_shape;
        public CardView cardView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            
            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(R.id.linea_id);
            status = (TextView) itemView.findViewById(R.id.linea_status);
            content_shape = (LinearLayout) itemView.findViewById(R.id.content_shape);
            
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(v.getContext(), "Click en la linea: " + name.getText().toString(), Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(v.getContext(), DetalleLineaActivity.class);
            //intent.putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
            Log.d(LOG_TAG, "click");

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Linea linea = lineas.get(position);
        holder.name.setText(linea.getName());
        holder.status.setText(linea.getStatus());
        holder.status.setTextColor(Color.parseColor("#009900"));
        //Log.d(LOG_TAG, "Status: " + holder.status.getText());
        if (holder.status.getText().length() != 6) {
            holder.status.setTextColor(Color.parseColor("#E91627"));
        }

        switch (linea.getName()){
            case "A":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_a);
                break;

            case "B":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_b);
                break;

            case "C":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_c);
                break;

            case "D":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_d);
                break;

            case "E":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_e);
                break;
            
            case "H":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_h);
                break;
            
            case "P":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_p);
                break;

            case "U":
                holder.content_shape.setBackgroundResource(R.drawable.background_item_linea_u);
                break;
        }

        holder.itemView.setTag(linea);
        
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDetalle = new Intent(v.getContext(), DetalleLineaActivity.class);
                Log.d(LOG_TAG, lineas.get(position).toString());
                //intentDetalle.putExtra("NOMBRE_LINEA", (android.os.Parcelable) lineas.get(position));
                v.getContext().startActivity(intentDetalle);
            }
        });
        
    }

    @Override
    public int getItemCount() {
        return lineas.size();
    }
    
  
}
