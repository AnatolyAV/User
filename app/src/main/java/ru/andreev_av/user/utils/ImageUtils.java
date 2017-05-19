package ru.andreev_av.user.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.FileOutputStream;

public class ImageUtils {

    private Context context;

    public ImageUtils(Context context) {
        this.context = context;
    }

    public void saveBitmap(Bitmap bitmap, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(context.getCacheDir().toString() + fileName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}