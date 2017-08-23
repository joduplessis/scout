package com.robotmonsterlabs.scout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joduplessis on 2015/08/02.
 */
public class AdaptorBluetooth extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String,String>> data;

    public AdaptorBluetooth(Context context, ArrayList<HashMap<String, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // the basic system service for inflating the views
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // we check if the view is null, unless scrolling, etc. http://stackoverflow.com/questions/14745780/understanding-convertview-parameter-of-getview-method
        if (convertView == null) {

            // assign the XML view to this BaseAdaptor's convertView
            convertView = inflater.inflate(R.layout.adaptor_bluetooth, null);

            // get our data, basic Hashmap stuff here
            HashMap<String,String> obj = data.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView subtitle = (TextView) convertView.findViewById(R.id.subtitle);

            ImageView barOne = (ImageView) convertView.findViewById(R.id.bar_one);
            ImageView barTwo = (ImageView) convertView.findViewById(R.id.bar_two);
            ImageView barThree = (ImageView) convertView.findViewById(R.id.bar_three);
            ImageView barFour = (ImageView) convertView.findViewById(R.id.bar_four);
            ImageView barFive = (ImageView) convertView.findViewById(R.id.bar_five);

            title.setText(obj.get("name"));

            if (obj.get("ibeacon").equals("yes"))
                subtitle.setText("Apple iBeacon compatible device");
            else
                subtitle.setText("Standard Bluetooth device");

            int getLevel = Integer.parseInt(obj.get("level")) * -1;
            if (getLevel>25) barOne.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.signal_strength_on));
            if (getLevel>50) barTwo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.signal_strength_on));
            if (getLevel>75) barThree.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.signal_strength_on));
            if (getLevel>100) barFour.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.signal_strength_on));
            if (getLevel>125) barFive.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.signal_strength_on));

        }

        return convertView;

    }
}
