package com.maxedapps.hundredeuros;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by Maximilian on 27.08.14.
 */
public class FileHelper {

    public static void saveImage(Context context, Bitmap image, String filename) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100,fos);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllImagesExcept(Context context, String filenameException) {
        File file = context.getFilesDir();
        File[] files = file.listFiles();
        for (File tempFile : files) {
            if (tempFile.getName() != filenameException) {
                tempFile.delete();
            }
        }
    }

    public static Bitmap loadImage(Context context, String filename) {
        FileInputStream fis = null;
        try {
            fis  = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fis != null){

            Bitmap img = BitmapFactory.decodeStream(fis);

            return img;
        }
        return null;
    }
}
