package mobi.devteam.demofalldetector.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.myInterface.OnRequestPermissionListener;

/**
 * Created by Administrator on 7/18/2017.
 */

public class AppPermission {

    private Activity mActivity;
    private OnRequestPermissionListener mListener;

    public AppPermission(Activity mActivity, OnRequestPermissionListener mListener) {
        this.mActivity = mActivity;
        this.mListener = mListener;
    }

    private final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
    };

    public boolean check_permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSION_LIST) {
                if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return false;
    }

    /**
     * Use this method to send the request permission
     * Use ActivityCompat.requestPermissions if it was activity
     * Use requestPermissions if was Fragment
     * Override the method onRequestPermissionsResult and take care for the permission
     */
    public void request_permission() {

        boolean flag = false;

        for (String permission : PERMISSION_LIST) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                flag = true;
                break;
            }
        }

        if (mListener != null) {
            if (flag) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.request_permissions)
                        .setMessage(R.string.request_permissions_desc)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.showSettingIntent();
//                                ((MainActivity) mActivity).setWaitingForSettingChange(true);
//                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
//                                AppPermission.this.mActivity.startActivity(intent);
                            }
                        });
                builder.show();
            } else {
                // No explanation needed, we can request the permission.
                mListener.requestPermissions(PERMISSION_LIST);
            }
        }

    }
}
