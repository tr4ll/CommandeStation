package fr.commandestation.outils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by trall on 05/12/2015.
 */
public class Outils {

    public static String convertTimeMiliToDateFr(Long timeMili) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date dateView = new Date();
        dateView.setTime(timeMili);

        return format.format(dateView);
    }
}
