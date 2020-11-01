package com.rajeev.stepscounter.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.StepsRecord;

public class StepsRecordsAdapter extends BaseAdapter {


    private final Activity context;
    private StepsRecord[] stepsRecords;


    public StepsRecordsAdapter(Activity context, StepsRecord[] stepsRecords) {
        this.context = context;
        this.stepsRecords = stepsRecords;
    }

    @Override
    public int getCount() {
        return stepsRecords.length;
    }

    @Override
    public StepsRecord getItem(int position) {
        return stepsRecords[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final StepsRecord stepsRecord = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.steps_record, null);
        }

        TextView dateTextView = convertView.findViewById(R.id.recordDate);
        TextView stepsTextView = convertView.findViewById(R.id.recordSteps);


        if (stepsRecord != null) {
            dateTextView.setText(stepsRecord.getDate());
            stepsTextView.setText(String.valueOf(stepsRecord.getSteps()) + " steps");
        }

        return convertView;
    }


}