package com.rajeev.stepscounter.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

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

public class HomeFragment extends Fragment {


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    long uniqueStepsId = -1;
    int lastestFirebaseSteps = 0;
    long maxId = 0;


    TextView heartTextView;
    TextView stepsTextView;
    TextView caloriesTextView;
    TextView activityTextView;
    TextView distanceTextView;
    ImageView activityImageView;
    ImageView heartImageView;

    DatabaseReference databaseReference;
    FirebaseUser currentUser;


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //initialize the components
        initializeComponents(view);

        //setup the preferences to read from the device locally
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();


        // Initialize the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //message recieving from the watch
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver, messageFilter);


        databaseReference = FirebaseDatabase.getInstance().getReference("steps");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    maxId = dataSnapshot.getChildrenCount();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        uniqueStepsId = -1; //reset steps
                        String childUserId = child.child("uuid").getValue(String.class);
                        int childSteps = Integer.valueOf(child.child("steps").getValue(Integer.class));
                        String date = child.child("date").getValue(String.class);
                        StepsRecord stepsRecord = new StepsRecord(childUserId, childSteps, date);
                        if (currentUser != null && stepsRecord.getUuid().equals(currentUser.getUid()) && stepsRecord.getDate().equals(Utils.getDate())) {
                            uniqueStepsId = Long.valueOf(child.getKey());
                            lastestFirebaseSteps = stepsRecord.getSteps();
                            updateSteps();
                        }
                    }
                }
                if (uniqueStepsId == -1) {
                    uniqueStepsId = maxId + 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //update the textfield value for the steps
        updateStepsCountTextField();

        return view;
    }


    /**
     * Initializes the components
     *
     * @param view Our view
     */
    private void initializeComponents(View view) {
        stepsTextView = view.findViewById(R.id.textView);
        heartTextView = view.findViewById(R.id.textView2);
        caloriesTextView = view.findViewById(R.id.caloriesBurnedTextView);
        activityTextView = view.findViewById(R.id.textView3);
        distanceTextView = view.findViewById(R.id.distanceWalkedTextView);
        activityImageView = view.findViewById(R.id.imageView);
        heartImageView = view.findViewById(R.id.imageView2);
        heartImageView.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.pulse));
    }


    /**
     * Gets the local steps count stored on the phone by the user id
     *
     * @return Returns the count of steps stored on the phone
     */
    public int getLocalSteps() {
        if (!userRecordAlreadyExistsForTheDay()) {
            return 0;
        }
        String recordKey = currentUser.getUid() + ":" + Utils.getDate();
        String value = sharedPreferences.getString(recordKey, "");
        int steps = Integer.valueOf(value);
        return steps;
    }


    /**
     * Checks if there exist a local record for the user with the current username and date
     *
     * @return Returns the current record of the user for the day if exists
     */
    public boolean userRecordAlreadyExistsForTheDay() {
        if (sharedPreferences != null && currentUser != null) {
            String currentDate = Utils.getDate();
            String record = currentUser.getUid() + ":" + currentDate;
            if (sharedPreferences.contains(record)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the latest database steps
     *
     * @return Returns the latest database steps
     */
    public int getDatabaseSteps() {
        return lastestFirebaseSteps;
    }

    /**
     * This function compares both local steps and the database one and gets the higher amongst them
     *
     * @return Returns the highest step
     */
    public int getHigherSteps() {
        int localSteps = getLocalSteps();
        //compare firebase steps against the local one and update according to the bigger
        return lastestFirebaseSteps > localSteps ? lastestFirebaseSteps : localSteps;
    }

    /**
     * Checks whether the database steps are higher than the local steps
     *
     * @return Returns whether the database steps are higher than the local steps or not
     */
    public boolean isDatabaseStepsHigherThanLocal() {
        return lastestFirebaseSteps > getLocalSteps();
    }

    /**
     * Updates the local steps by a new value
     *
     * @param steps The steps in which the local steps will be set to
     */
    public void updateLocalSteps(int steps) {
        if (editor != null && currentUser != null) {
            String date = Utils.getDate(); //gets today's date
            editor.putString(currentUser.getUid() + ":" + date, String.valueOf(steps));
            editor.commit();
            Log.d("UPDATE LOCAL STEPS", "Assigning " + steps + " steps to uuid " + currentUser.getUid() + " for the date " + date);
        }
    }


    /**
     * Updates the database steps by the passed parameter
     *
     * @param steps The steps to be updated to the database
     */
    public void updateDatabaseSteps(int steps) {
        String date = Utils.getDate();
        Log.d("Update database steps", "passed steps are " + steps + " and the unique id is " + uniqueStepsId + " date " + date + " maxid is " + maxId);
        if (databaseReference != null && currentUser != null && uniqueStepsId != -1) {
            StepsRecord stepsRecord = new StepsRecord(currentUser.getUid(), steps, date);
            databaseReference.child(String.valueOf(uniqueStepsId)).setValue(stepsRecord);
            databaseReference.push();
        }
    }

    /**
     * Updates the steps text field based on the higher steps (either from the database or locally)
     */
    public void updateStepsCountTextField() {
        int steps = getHigherSteps();
        updateTextFields(steps);
    }

    /**
     * updates the steps text field count by a specified number
     *
     * @param steps The number of steps in which should be displayed in the steps text view
     */
    public void updateTextFields(int steps) {
        if (stepsTextView != null) {
            stepsTextView.setText(steps + " steps");
        }

        if (caloriesTextView != null) {
            caloriesTextView.setText(Utils.getCaloriesBurned(steps) + " calories");
        }

        if (distanceTextView != null) {
            distanceTextView.setText(Utils.getDistanceWalkedInKiloMeters(steps) + " km");
        }
    }

    /**
     * This method will get the highest steps amongst the local and the database one and will compare if the database is higher than the local value, then update the local value, otherwise update the database value
     */
    public void updateSteps() {
        if (getDatabaseSteps() != getLocalSteps()) {
            int steps = getHigherSteps();
            Log.d("Update steps methodd", "Databse steps are " + lastestFirebaseSteps + " and local steps are " + getLocalSteps() + " higher is " + steps + " database steps are higher? " + isDatabaseStepsHigherThanLocal());
            if (isDatabaseStepsHigherThanLocal()) {
                updateLocalSteps(steps);
            } else {
                updateDatabaseSteps(steps);
            }
            updateTextFields(steps);
        }
    }


    /**
     * Increment the local steps by one
     */
    public void incrementLocalSteps() {
        int steps = getLocalSteps() + 1;
        updateLocalSteps(steps);
    }


    /**
     * Updates the hear beats text field count
     *
     * @param msg The heart beats
     */
    public void updateHeatBeatsField(String msg) {
        if (heartTextView != null) {
            heartTextView.setText(msg);
        }
    }


    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String msg = intent.getStringExtra("pulse");


            //received a hear beat message from the watch
            if (msg.contains("bpm")) {
                heartTextView.setText(msg);
            }

            //received a steps message from the watch
            else if (msg.contains("steps")) {
                int steps = Integer.parseInt(msg.split(" ")[0]);

                //override the database and local steps with the received one if the received one is higher
                if (steps > getHigherSteps()) {
                    updateDatabaseSteps(steps);
                    updateLocalSteps(steps);
                }
                updateStepsCountTextField(); //update the steps textfield
            }

            //received an activity message from the watch
            else if (msg.contains("Activity: ")) {
                String activity = msg.split("Activity: ")[1];
                int icon = R.drawable.unknown;
                switch (activity) {
                    case "Vehicle": {
                        icon = R.drawable.vehicle;
                        break;
                    }
                    case "Bicycle": {
                        icon = R.drawable.bicycle;
                        break;
                    }
                    case "On foot": {
                        icon = R.drawable.standing;
                        break;
                    }
                    case "Running": {
                        icon = R.drawable.running;
                        break;
                    }
                    case "Still": {
                        icon = R.drawable.sitting;
                        break;
                    }
                    case "Tilting": {
                        //icon = new ImageView(R.drawable.tilting);
                        break;
                    }
                    case "Walking": {
                        icon = R.drawable.walking;
                        break;
                    }
                    case "Unknown": {
                        icon = R.drawable.unknown;
                        break;
                    }
                }
                activityTextView.setText(activity);
                activityImageView.setImageResource(icon);
            }
        }
    }
}

