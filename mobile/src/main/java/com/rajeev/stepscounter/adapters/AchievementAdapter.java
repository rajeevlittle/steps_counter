package com.rajeev.stepscounter.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseUser;
import com.rajeev.stepscounter.Achievement;
import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.TaskScheduler;
import com.rajeev.stepscounter.utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AchievementAdapter extends BaseAdapter {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final Activity context;
    private final List<Achievement> achievements;
    FirebaseUser currentUser;


    public AchievementAdapter(Activity context, ArrayList<Achievement> achievements, FirebaseUser currentUser) {
        this.context = context;
        this.achievements = achievements;
        this.currentUser = currentUser;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    @Override
    public int getCount() {
        return achievements.size();
    }

    @Override
    public Achievement getItem(int position) {
        return achievements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final Achievement achievement = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.achievement_item, null);
        }

        TextView title = convertView.findViewById(R.id.achievementTitle);
        TextView description = convertView.findViewById(R.id.achievementDescription);
        TextView reward = convertView.findViewById(R.id.rewardValue);
        final ProgressBar progressBar = convertView.findViewById(R.id.progressBar);
        final Button claimButton = convertView.findViewById(R.id.claimButton);
        claimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressBar.getProgress() >= progressBar.getMax()) {
                    Toast.makeText(context, "Congratulations for completing the task!!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "You haven't completed your achievement yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView medal = convertView.findViewById(R.id.medal);
        switch (achievement.getMedal()) {
            case Bronze:
                medal.setImageDrawable(context.getDrawable(R.drawable.bronze_medal));
                break;
            case Silver:
                medal.setImageDrawable(context.getDrawable(R.drawable.silver_medal));
                break;
            case Gold:
                medal.setImageDrawable(context.getDrawable(R.drawable.gold_medal));
                break;
        }

        title.setText(achievement.getTitle());
        description.setText(achievement.getDescription());
        reward.setText(achievement.getReward() + " xp");
        progressBar.setMax(achievement.getRequiredSteps());
        updateProgressBarAndButton(progressBar, claimButton);

        return convertView;
    }


    /**
     * This function takes the progressbar and starts a timer that will execute once a second and checks whether for the user's steps and refreshes the progress bar value accordingly and checks whether the task has been completed or not. It would stop the timer if a task gets completed.
     *
     * @param progressBar The progressbar to be updated
     * @param button      The button to be updated
     */
    private void updateProgressBarAndButton(final ProgressBar progressBar, final Button button) {
        TaskScheduler timer = new TaskScheduler();
        timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int progress = 0;
                if (currentUser != null && sharedPreferences != null) {
                    String key = currentUser.getUid() + ":" + Utils.getDate();
                    if (sharedPreferences.contains(key)) {
                        String value = sharedPreferences.getString(key, "");
                        progress = Integer.parseInt(value);
                    }
                }
                progressBar.setProgress(progress);
                if (progressBar.getProgress() >= progressBar.getMax()) {
                    Toast.makeText(context, "Well done, you've completed an achievement!!", Toast.LENGTH_SHORT);
                    button.setVisibility(View.VISIBLE);

                }
            }
        },1000);
    }
}