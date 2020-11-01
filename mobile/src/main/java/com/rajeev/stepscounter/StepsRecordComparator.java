package com.rajeev.stepscounter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

public class StepsRecordComparator implements Comparator<StepsRecord> {

    @Override
    public int compare(StepsRecord stepsRecord1, StepsRecord stepsRecord2) {
        String stringDate1 = stepsRecord1.getDate();
        String stringDate2 = stepsRecord2.getDate();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        try {
            if (formatter.parse(stringDate1).before(formatter.parse(stringDate2))) {
                return -1;
            } else if (formatter.parse(stringDate1).equals(formatter.parse(stringDate2))) {
                return 0;
            } else {
                return 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}