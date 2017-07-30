package mobi.devteam.demofalldetector.utils;

public class Constants {
    //Use public => Outside package
    public interface KEY {
        String ITEM_KEY = "mobi.devteam.itemkey";
        String PENDING_ID = "mobi.devteam.id";
    }

    public interface ACTION {
        String DISMISS_ACTION = "action.dismiss";
        String SNOOZE_ACTION = "action.snooze";
        String START_SERVICE = "start.service";
        String STOP_SERVICE = "stop.service";
    }
}
