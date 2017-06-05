package mobi.devteam.demofalldetector.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 6/6/2017.
 */

public class Utils {

    public static String get_calendar_time(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:ss");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String get_calendar_date(Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }

}
