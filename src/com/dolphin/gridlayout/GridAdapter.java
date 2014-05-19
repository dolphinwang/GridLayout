package com.dolphin.gridlayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GridAdapter extends BaseAdapter {

    LayoutInflater mInflater;

    public GridAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 5;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item, parent, false);
            holder = new Holder();
            convertView.setTag(holder);

            holder.img1 = (ImageView) convertView.findViewById(R.id.img1);
            holder.img2 = (ImageView) convertView.findViewById(R.id.img2);
            holder.img3 = (ImageView) convertView.findViewById(R.id.img3);
            holder.img4 = (ImageView) convertView.findViewById(R.id.img4);
            holder.img5 = (ImageView) convertView.findViewById(R.id.img5);
        } else {
            holder = (Holder) convertView.getTag();
        }

        return convertView;
    }

    private static class Holder {
        ImageView img1;
        ImageView img2;
        ImageView img3;
        ImageView img4;
        ImageView img5;
    }
}
