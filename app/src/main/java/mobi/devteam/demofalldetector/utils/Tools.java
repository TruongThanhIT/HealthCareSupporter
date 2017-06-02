package mobi.devteam.demofalldetector.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by DELL on 5/21/2017.
 */

public class Tools {
    // Category tools
    public static final Bitmap convertImageViewToBitmap(ImageView imgStudent) {
        BitmapDrawable drawable = (BitmapDrawable) imgStudent.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
        return resized;
    }

    public static final byte[] convertBitmapToByteAray(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public static final Bitmap convertByteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }
}
