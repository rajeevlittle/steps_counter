package com.rajeev.stepscounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACTIVITY_RECOGNITION;
import static android.Manifest.permission.BODY_SENSORS;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private String datapath = "/my_path";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextView textView1, textView2, textView3;
    private ImageView activityIcon;

    private SensorManager sensorManager;

    private BroadcastReceiver broadcastReceiver;


    private String previousActivity = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //setup the preferences to read from the device locally
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        textView1 = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);

        activityIcon = (ImageView) findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android" + "" + ".permission.BODY_SENSORS") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{BODY_SENSORS}, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android" + "" + ".permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{ACTIVITY_RECOGNITION}, 0);
        }

        // Enables Always-on
        setAmbientEnabled();

        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));


        //Register to receive local broadcasts, which we'll be creating in the next step//

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

        startTracking();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
        if (countSensor != null && heartRateSensor != null) {
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        StringBuilder builder = new StringBuilder();
        int steps = 0;
        int heartBeat = 0;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            //read the steps and increment them by one
            int readSteps = 0;
            if (sharedPreferences != null && sharedPreferences.contains("steps")) {
                readSteps = Integer.parseInt(sharedPreferences.getString("steps", ""));
            }
            readSteps++;
            String text = readSteps + " steps";

            //update the steps textfield
            textView1.setText(text);

            //update the steps locally
            editor.putString("steps", String.valueOf(readSteps));
            editor.commit();

            new SendMessage(datapath, (text)).start();

        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartBeat = (int) sensorEvent.values[0];
            String text = heartBeat + " bpm";
            textView2.setText(text);
            new SendMessage(datapath, (text)).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void handleUserActivity(int type, int confidence) {
        String label = "";
        int icon = R.drawable.unknown;


        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                icon = R.drawable.vehicle;
                label = "Vehicle";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                icon = R.drawable.bicycle;
                label = "Bicycle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                icon = R.drawable.standing;
                label = "On foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                icon = R.drawable.running;
                label = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                icon = R.drawable.sitting;
                label = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                //icon = new ImageView(R.drawable.tilting);
                label = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
//                label = getString(R.string.activity_walking);
                icon = R.drawable.walking;
                label = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                icon = R.drawable.unknown;
                label = "Unknown";
                break;
            }
        }

        if (confidence > Constants.CONFIDENCE) {
            if (!previousActivity.equals(label)) {
                previousActivity = label;
                activityIcon.setImageResource(icon);
                textView3.setText(label);
                new SendMessage(datapath, ("Activity: " + label)).start();
            }
        }
    }


    /**
     * starts detecting the current user activity
     */
    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(intent);
        } else {
            startService(intent);
        }
    }


    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("pulse");
            int phoneSteps = Integer.parseInt(msg.split(" steps")[0]);
            int watchSteps = 0;

            if (sharedPreferences != null && sharedPreferences.contains("steps")) {
                watchSteps = Integer.valueOf(sharedPreferences.getString("steps", ""));
            }

            if (phoneSteps > watchSteps) {
                editor.putString("steps", String.valueOf(phoneSteps));
                editor.commit();
                textView1.setText(msg);
            }
        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

//Constructor///

        SendMessage(String p, String m) {
            path = p;
            message = m;
        }

//Send the message via the thread. This will send the message to all the currently-connected devices//

        public void run() {

//Get all the nodes//

            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

//Block on a task and get the result synchronously//

                List<Node> nodes = Tasks.await(nodeListTask);

//Send the message to each device//

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
}
