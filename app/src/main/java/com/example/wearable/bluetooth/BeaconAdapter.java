package com.example.wearable.bluetooth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Vector;

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder>  {

    private Vector<Item> items;
    private Context context;

    BeaconAdapter(Vector<Item> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public BeaconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_beacon, parent, false);
        ViewHolder vh = new ViewHolder(linearLayout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            holder.rssiTv.setText(""+items.get(position).getRssi() +"dBm");
            holder.txPowerTv.setText(""+items.get(position).getTxPower() +"dBm");
            holder.distanceTv.setText(""+items.get(position).getDistance() +"m");
            holder.majorTv.setText(""+items.get(position).getAddress()+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView rssiTv, txPowerTv, distanceTv, majorTv;

        public ViewHolder(LinearLayout v) {
            super(v);
            rssiTv = v.findViewById(R.id.rssiTv);
            txPowerTv = v.findViewById(R.id.txPowerTv);
            distanceTv = v.findViewById(R.id.distanceTv);
            majorTv = v.findViewById(R.id.majorTv);
        }
    }

}
