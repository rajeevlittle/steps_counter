package com.rajeev.stepscounter.fragments;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rajeev.stepscounter.MyMarkerView;
import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.StepsAnalysis;
import com.rajeev.stepscounter.StepsRecord;
import com.rajeev.stepscounter.StepsRecordComparator;
import com.rajeev.stepscounter.adapters.StepsRecordsAdapter;
import com.rajeev.stepscounter.utilities.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class HistoryFragment extends Fragment implements AdapterView.OnItemSelectedListener {


    private LineChart lineChart;
    private TextView stepsRecordsTitleLabel;
    private TextView totalRecords;
    private ListView stepsRecordsListView;
    private Spinner spinner;

    private FirebaseUser currentUser;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        spinner = view.findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.records_filter, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        lineChart = (LineChart) view.findViewById(R.id.line);
        stepsRecordsTitleLabel = view.findViewById(R.id.stepsRecordsTitleLabel);
        totalRecords = view.findViewById(R.id.recordsCountTextView);

        //setup the preferences to read from the device locally
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();


        DatabaseReference databaseUsersReference = FirebaseDatabase.getInstance().getReference("users");
        databaseUsersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (currentUser.getUid().equals(child.getKey())) {
                        stepsRecordsTitleLabel.setText(child.child("name").getValue(String.class) + "'s " + " steps records");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


        //create achievements
        StepsRecord[] stepsRecords = getLocalStepsRecords();


        boolean fetched = stepsRecords != null && stepsRecords.length > 0;
        totalRecords.setText("Active days " + (fetched ? stepsRecords.length : 0));

        if (fetched) {
            //populate the list with the steps records
            StepsRecordsAdapter stepsRecordsAdapter = new StepsRecordsAdapter(getActivity(), stepsRecords);
            stepsRecordsListView = (ListView) view.findViewById(R.id.stepsRecordsList);
            stepsRecordsListView.setAdapter(stepsRecordsAdapter);
        }

        return view;
    }


    private List getData(int[] steps) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < steps.length; i++) {
            entries.add(new Entry(i, steps[i]));
        }
        return entries;
    }


    /**
     * This fucntions gets all the local steps records for the current user
     *
     * @return Returns the local steps record for the logged in user
     */
    public StepsRecord[] getLocalStepsRecords() {
        List<StepsRecord> list = new ArrayList<StepsRecord>();
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getKey().startsWith(currentUser.getUid() + ":")) {
                list.add(new StepsRecord(currentUser.getUid(), Integer.valueOf(entry.getValue().toString()), entry.getKey().split(":")[1]));
            }
        }
        //check if there are no any saved steps on the phone locally
        if (list.size() == 0) {
            return null;
        } else {
            StepsRecord[] stepsRecords = new StepsRecord[list.size()];
            for (int i = 0; i < list.size(); i++) {
                stepsRecords[i] = (StepsRecord) list.get(i);
            }
            Arrays.sort(stepsRecords, new StepsRecordComparator());
            return stepsRecords;
        }
    }


    /**
     * Takes the data and puts them into two arrays, a string and an integer and passes them to the renderData
     *
     * @param stepsRecords The steps records array
     */
    public void renderData(StepsRecord[] stepsRecords) {
        String[] dates = new String[stepsRecords.length];
        int[] steps = new int[stepsRecords.length];
        for (int i = 0; i < stepsRecords.length; i++) {
            dates[i] = stepsRecords[i].getDate();
            steps[i] = stepsRecords[i].getSteps();
        }
        renderData(dates, steps);
    }


    /**
     * Renders out dates and steps to the graph in the x/y axises
     *
     * @param dates The dates to be displayed as labels for x axis
     * @param steps The steps to be displayed as labels for the y axis
     */
    public void renderData(final String[] dates, int[] steps) {

        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.customer_marker_view);
        mv.setChartView(lineChart);
        lineChart.setMarker(mv);

        LineDataSet lineDataSet = new LineDataSet(getData(steps), "Steps over days");
        lineDataSet.setColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
        lineDataSet.setValueTextColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimaryDark));


        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_blue);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(drawable);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelRotationAngle(45);
        xAxis.setSpaceMin(0.1f);
        xAxis.setSpaceMax(0.1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                String val = null;
                try {
                    val = dates[(int) value];
                } catch (IndexOutOfBoundsException e) {
                    axis.setGranularityEnabled(false);
                }
                return val;
            }
        };


        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);
        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
        lineChart.animateX(1200);
        lineChart.animateY(1200);
        lineChart.invalidate();
    }


    public StepsRecord[] getStepsRecords(StepsRecord[] stepsRecords, int days) {

        List<StepsRecord> recordsList = new ArrayList<StepsRecord>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = Utils.getDate();

        for (StepsRecord stepsRecord : stepsRecords) {
            String recordDate = stepsRecord.getDate();
            try {
                //calculate date difference and convert them into days
                long diffInMillis = simpleDateFormat.parse(currentDate).getTime() - simpleDateFormat.parse(recordDate).getTime();
                long dateDiffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                //if the difference in between the days and the max allowed satisfy, add it
                if (dateDiffInDays < days) {
                    recordsList.add(stepsRecord);
                }
            } catch (ParseException pe) {
                Log.d("Parsing error [getStepsRecords]", "Current date = " + currentUser + ", record date = " + recordDate);
            }
        }

        //adds records from list to array
        StepsRecord[] records = new StepsRecord[recordsList.size()];
        int total = 0;
        for (int i = 0; i < records.length; i++) {
            records[i] = recordsList.get(i);
        }

        StepsAnalysis analysis = new StepsAnalysis(records);
        Log.d("Analysis", "Min steps = " + analysis.getMin() + ", max steps = " + analysis.getMax() + ", total steps = " + analysis.getTotalSteps() + ", mean/average = " + analysis.getMean() + ", standrd diviation = " + analysis.getStandardDiviation() + ", activity group = " + analysis.getActivityGroup());

        return records;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

        StepsRecord[] stepsRecords = getLocalStepsRecords();
        if (stepsRecords == null || stepsRecords.length == 0) {
            return;
        }


        String value = adapterView.getItemAtPosition(position).toString();
        int days = Integer.MAX_VALUE;

        if (value.equalsIgnoreCase("week")) {
            days = 7;
        } else if (value.equalsIgnoreCase("fortnight")) {
            days = 15;
        } else if (value.equalsIgnoreCase("month")) {
            days = 30;
        } else if (value.equalsIgnoreCase("year")) {
            days = 365;
        }

        StepsRecord[] finalRecords = getStepsRecords(stepsRecords, days);
        renderData(finalRecords);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

