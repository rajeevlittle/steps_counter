package com.rajeev.stepscounter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rajeev.stepscounter.Achievement;
import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.adapters.AchievementAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class AchievementFragment extends Fragment {

    ListView achievementsListView;

    FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_achievement, container, false);


        //get the current logged in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        //create achievements
        ArrayList<Achievement> achievements = new ArrayList<>(Arrays.asList(
                new Achievement(Achievement.Medal.Bronze, "Achievement 1", "You have to walk for a total of 1500 steps to complete this achievement", 25, 1500),
                new Achievement(Achievement.Medal.Bronze, "Achievement 2", "You have to walk for a total of 4000 steps to complete this achievement", 100, 4000),
                new Achievement(Achievement.Medal.Bronze, "Achievement 3", "You have to walk for a total of 6000 steps to complete this achievement", 180, 6000),
                new Achievement(Achievement.Medal.Silver, "Achievement 4", "You have to walk for a total of 10000 steps to complete this achievement", 300, 10000),
                new Achievement(Achievement.Medal.Gold, "Achievement 5", "You have to walk for a total of 15000 steps to complete this achievement", 500, 15000)
        ));

        //populate the list with the achievements
        AchievementAdapter achievementAdapter = new AchievementAdapter(getActivity(), achievements, currentUser);
        achievementsListView = (ListView) view.findViewById(R.id.achievementList);
        achievementsListView.setAdapter(achievementAdapter);

        return view;
    }
}
