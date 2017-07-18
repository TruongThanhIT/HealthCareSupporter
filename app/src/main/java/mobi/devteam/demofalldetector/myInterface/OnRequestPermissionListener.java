package mobi.devteam.demofalldetector.myInterface;

/**
 * Created by Administrator on 7/18/2017.
 */

public interface OnRequestPermissionListener {

    /**
     * Use this method to start the intent ( set wiating for request)
     * and override the onResume then check for the permission
     * when back to the activity or fragment
     */
    void showSettingIntent();

    /**
     * Use this method to send the request permission
     * Use ActivityCompat.requestPermissions if it was activity
     * Use requestPermissions if was Fragment
     * Override the method onRequestPermissionsResult and take care for the permission
     * @param permissions
     */
    void requestPermissions(String[] permissions);
}
