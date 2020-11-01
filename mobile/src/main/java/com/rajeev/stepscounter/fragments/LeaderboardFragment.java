package com.rajeev.stepscounter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.StepsRecord;
import com.rajeev.stepscounter.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment implements AdapterView.OnItemSelectedListener {


    List<StepsRecord> records;

    Map<String, String> users;


    private LinearLayout layout;
    private Spinner spinner;

    DatabaseReference databaseUsersReference, databaseStepsReference;
    FirebaseUser currentUser;

    private Switch toggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        layout = (LinearLayout) view.findViewById(R.id.linear_layout);

        spinner = view.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.records_leaderboard_filter, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        records = new ArrayList<StepsRecord>();
        users = new HashMap<String, String>();


        toggle = view.findViewById(R.id.switch1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateRecords();
            }
        });


        databaseUsersReference = FirebaseDatabase.getInstance().getReference("users");
        databaseUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("users", "info " + child.getValue() + " and " + child.child(child.getKey()).getValue());
                    if (!users.containsKey(child.getKey())) {
                        users.put(child.getKey(), child.child("name").getValue(String.class));
                    }
                }
                updateRecords();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        databaseStepsReference = FirebaseDatabase.getInstance().getReference("steps");
        databaseStepsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                records.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String uuid = child.child("uuid").getValue(String.class);
                    int steps = child.child("steps").getValue(Integer.class);
                    String date = child.child("date").getValue(String.class);
                    StepsRecord stepsRecord = new StepsRecord(uuid, steps, date);
                    records.add(stepsRecord);

                }
                updateRecords();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        return view;

    }


    /**
     * Check whether we should display daily or all the time records based on the toggle whether it is checked or not
     */
    private void updateRecords() {
        if (toggle.isChecked()) {
            addRecordsOfToday();
        } else {
            addRecordsOfAllTime();
        }
    }


    private boolean matchesActivityCategoryFilter(int steps) {
        String currentChosenActivityCategory = spinner.getSelectedItem().toString();
        if(currentChosenActivityCategory.equals("Any")){
            return true;
        }else if (steps < 5000 && currentChosenActivityCategory.equals("Sedentary Lifestyle (0-5000)")) {
            return true;
        } else if (steps >= 5000 && steps < 7500 && currentChosenActivityCategory.equals("Low Active (5000-7499)")) {
            return true;
        } else if (steps >= 7500 && steps < 10000 && currentChosenActivityCategory.equals("Somewhat Active (7500-9999)")) {
            return true;
        } else if (steps >= 10000 && steps < 12500 && currentChosenActivityCategory.equals("Active (10000-12499)")) {
            return true;
        } else if (steps >= 12500 && currentChosenActivityCategory.equals("Highly active (12500+)")) {
            return true;
        }
        return false;
    }



    /**
     * Adds all the records to the list
     */
    private void addRecordsOfAllTime() {
        layout.removeAllViews();
        Map<String, Integer> finalRecords = new HashMap<String, Integer>();
        for (int i = 0; i < records.size(); i++) {
            int steps = records.get(i).getSteps();
            if (matchesActivityCategoryFilter(steps)) {
                String uuid = records.get(i).getUuid();
                if (!finalRecords.containsKey(uuid)) {
                    finalRecords.put(uuid, steps);
                } else {
                    int currentTotalSteps = finalRecords.get(uuid);
                    finalRecords.put(uuid, currentTotalSteps + steps);
                }
            }
        }

        finalRecords = Utils.sortMapByValue(finalRecords);
        for (Map.Entry<String, Integer> record : finalRecords.entrySet()) {
            addRecord(users.get(record.getKey()), record.getValue());
        }
    }




    /**
     * Adds today's records
     */
    private void addRecordsOfToday() {
        Map<String, Integer> finalRecords = new HashMap<String, Integer>();
        layout.removeAllViews();
        for (StepsRecord stepsRecord : records) {
            if (stepsRecord.getDate().equals(Utils.getDate()) && matchesActivityCategoryFilter(stepsRecord.getSteps())) {
                finalRecords.put(stepsRecord.getUuid(), stepsRecord.getSteps());
            }
        }
        finalRecords = Utils.sortMapByValue(finalRecords);
        for (Map.Entry<String, Integer> record : finalRecords.entrySet()) {
            addRecord(users.get(record.getKey()), record.getValue());
        }
    }


    /**
     * Adds a record to the leaderboards table
     *
     * @param nickName The nick name of the user
     * @param steps    The steps of the user
     */
    public void addRecord(String nickName, int steps) {

        View view = getLayoutInflater().inflate(R.layout.leaderboard_record, layout, false);
        TextView emailTextView = view.findViewById(R.id.nickNameTextView);
        TextView stepsTextView = view.findViewById(R.id.stepsTextView);
        TextView avatar = view.findViewById(R.id.letter_with_circle);

        avatar.setText(nickName == null ? "" : String.valueOf(Character.toUpperCase(nickName.charAt(0))));
        emailTextView.setText(nickName);
        stepsTextView.setText(steps + " steps");

        layout.addView(view, 0);

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        updateRecords();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
