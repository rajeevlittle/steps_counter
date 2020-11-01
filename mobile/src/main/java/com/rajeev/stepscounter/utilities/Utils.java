package com.rajeev.stepscounter.utilities;

import com.rajeev.stepscounter.Constants;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public class Utils {

    private static final Random RANDOM = new Random();


    /**
     * This method take a timestap and formats it, then prints it as a string
     *
     * @param timestamp The timestamp
     * @return Returns a formatted date of this tiemstap
     */
    public static String getDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone(); //get your local time zone.
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(tz); //set time zone.
        String localTime = sdf.format(new Date(timestamp));
        return localTime;
    }


    /**
     * Gets the current date
     *
     * @return Returns the current date
     */
    public static String getDate() {
        Calendar calendar;
        SimpleDateFormat dateFormat;
        String date;
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendar.getTime());
        return date;
    }


    /**
     * Gets the average calories burned while walking
     *
     * @param steps The steps walked
     * @return Returns the calories burned
     */
    public static String getCaloriesBurned(int steps) {
        double caloriesPerMile = Constants.WALKING_FACTOR * Constants.DEFAULT_WEIGHT_IN_LBS;
        double strip = Constants.DEFAULT_HEIGHT_IN_CM * 0.415; // for women 0.413
        double stepCountMile = 160934.4 / strip;  //mile in cm
        double conversationFactor = caloriesPerMile / stepCountMile;
        double caloriesBurned = steps * conversationFactor;
        return  new DecimalFormat("#0.00").format(caloriesBurned);
    }

    /**
     * Gets the average distance walked by taking the average steps per mile
     *
     * @param steps The steps walked
     * @return Returns the distance walked in meter
     */
    public static String getDistanceWalkedInKiloMeters(int steps) {
        double strip = Constants.DEFAULT_HEIGHT_IN_CM * 0.415; // for women 0.413
        double distance = (steps * strip) / 100000;
        return new DecimalFormat("#0.00").format(distance);
    }


    public static final int random(int maxValue) {
        if (maxValue <= 0)
            return 0;
        return RANDOM.nextInt(maxValue);
    }


    public static final double random(double min, double max) {
        final double n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random((int) n));
    }


    public static final int random(int min, int max) {
        final int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random(n));
    }


    public static final String getActivityStatus(int steps){
        if(steps < 5000){
            return "Sedentary Lifestyle";
        }else if(steps >= 5000 && steps < 7500){
            return "Low Active";
        }else if(steps >= 7500 && steps < 10000){
            return "Somewhat Active";
        }else if(steps >=10000 && steps <12500){
            return "Active";
        }else if(steps >= 12500){
            return "Highly active";
        }
        return "Unknown";
    }

    /**
     * Sorts elements by values
     *
     * @param map The map to be sorted
     * @return Returns sorted map
     */
    public static Map<String, Integer> sortMapByValue(Map<String, Integer> map) {

        // convert HashMap into List
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
        // sorting the list elements
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });


        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
