package com.rajeev.stepscounter.activities;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.StepsRecord;
import com.rajeev.stepscounter.adapters.TabAdapter;
import com.rajeev.stepscounter.fragments.AchievementFragment;
import com.rajeev.stepscounter.fragments.HistoryFragment;
import com.rajeev.stepscounter.fragments.HomeFragment;
import com.rajeev.stepscounter.fragments.LeaderboardFragment;
import com.rajeev.stepscounter.receivers.AlarmReceiver;
import com.rajeev.stepscounter.utilities.NetworkUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACTIVITY_RECOGNITION;
import static android.Manifest.permission.BODY_SENSORS;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private PendingIntent pendingIntent;


    private HomeFragment fragment;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabAdapter adapter;

    private NetworkReciever networkReciever;

    private SensorManager sensorManager;
    private FirebaseUser currentUser;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_delete_account) {
            deleteAccount();
            return true;
        } else if (id == R.id.action_exit) {
            finish();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Starts a repeating notification reminder activity
     */
    public void startNotificationReminderActivity() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60 * 60;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        startNotificationReminderActivity();


        // Initialize the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //setup the preferenc¶es to read from the device locally
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();


        //create the tabs
        setupTabs();

        //creating a new instance for a network reciever
        networkReciever = new NetworkReciever();


        //request body sensor if it was not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android" + "" + ".permission.BODY_SENSORS") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{BODY_SENSORS}, 0);
        }


        //request activity recognition if it was not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android" + "" + ".permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{ACTIVITY_RECOGNITION}, 0);
        }


        // Check if user is signed in (non-null) and update UI accordingly.
        if (currentUser == null) {
            Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent1);
        } else {
            Toast.makeText(getApplicationContext(), "Logged in as " + currentUser.getEmail() + "!", Toast.LENGTH_LONG).show();
        }


        configureSteps();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    }


    protected void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        //register internet reciever
        registerReceiver(networkReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        if (countSensor != null || heartRateSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        unregisterReceiver(networkReciever);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        StringBuilder builder = new StringBuilder();
        int steps = 0;
        int heartBeat = 0;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            fragment.incrementLocalSteps();
            fragment.updateSteps();
            new NewThread("/my_path", fragment.getHigherSteps() + " steps").start();
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartBeat = (int) sensorEvent.values[0];
            String text = heartBeat + " bpm";
            fragment.updateHeatBeatsField(text);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }




    /**
     * Gets the currently visible fragment
     *
     * @return Returns the currently visible fragment
     */
    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }


    /**
     * This function creates 4 tabs and initializes them and sets everything up
     */
    private void setupTabs() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        adapter = new TabAdapter(getSupportFragmentManager(), 0);
        adapter.addFragment(new HomeFragment(), "Home");
        adapter.addFragment(new AchievementFragment(), "Achivements");
        adapter.addFragment(new LeaderboardFragment(), "Leaderboard");
        adapter.addFragment(new HistoryFragment(), "History");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_home_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.achievement);
        tabLayout.getTabAt(2).setIcon(R.drawable.leaderboard);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_baseline_history_24);


        fragment = (HomeFragment) adapter.getItem(0);

    }

    /**
     * logs out the account and goes to the login page
     */
    private void logout() {
        Toast.makeText(getApplicationContext(), "logging out...", Toast.LENGTH_SHORT);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }





    /**
     * Deletes currently logged in account from the firebase authentication upon confirmation
     */
    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your " + getString(R.string.app_name) + "'s account?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Account successfully deleted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            //TODO clear shared preferences for that user
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //what if it is completely not on the database??
    public void configureSteps() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("steps");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String childUserId = child.child("uuid").getValue(String.class);
                        int childSteps = Integer.valueOf(child.child("steps").getValue(Integer.class));
                        String date = child.child("date").getValue(String.class);
                        StepsRecord stepsRecord = new StepsRecord(childUserId, childSteps, date);
                        if (currentUser != null && stepsRecord.getUuid().equals(currentUser.getUid())) {
                            String key = currentUser.getUid() + ":" + date;
                            if (sharedPreferences.contains(key)) {
                                int localValue = Integer.valueOf(sharedPreferences.getString(key, ""));
                                if (localValue > stepsRecord.getSteps()) {
                                    Log.d("Daily steps update", "local value is bigger than the database one, updating database. Local steps are " + (localValue) + " and db value " + (stepsRecord.getSteps()));
                                    databaseReference.child(child.getKey()).setValue(stepsRecord);
                                    databaseReference.push();
                                } else if (stepsRecord.getSteps() > localValue) {
                                    Log.d("Daily steps update", "database value is bigger than the local one, updating local");
                                    editor.putString(stepsRecord.getUuid() + ":" + date, String.valueOf(childSteps));
                                    editor.apply();
                                }
                            } else {
                                //value doesn't exist locally, create a new one
                                Log.d("Daily steps update", "value doesn't exist locally, adding it!");
                                editor.putString(stepsRecord.getUuid() + ":" + date, String.valueOf(childSteps));
                                editor.apply();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    class NewThread extends Thread {
        String path;
        String message;

        NewThread(String p, String m) {
            path = p;
            message = m;
        }


        public void run() {

            Task<List<Node>> wearableList =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        Integer result = Tasks.await(sendMessageTask);
                    } catch (ExecutionException exception) {
                        exception.printStackTrace();
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (ExecutionException exception) {
                exception.printStackTrace();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }

        }
    }


    public class NetworkReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = NetworkUtils.hasNetworkConnection(context);
            if (!status) {
                Toast.makeText(context, "Connection lost", Toast.LENGTH_SHORT).show();
            }
            Fragment fragment = getVisibleFragment();

            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

            if (!status) {
                boolean currentFragmentNeedsInternet = !(fragment instanceof HomeFragment || fragment instanceof AchievementFragment || fragment instanceof HistoryFragment);
                if ((currentFragmentNeedsInternet || fragment == null) && !cn.getClassName().contains("GrantPermissionsActivity")) {
                    // go to the offline activity
                    Intent i = new Intent(context, OfflineActivity.class);
                    context.startActivity(i);
                }
            }
        }
    }
}