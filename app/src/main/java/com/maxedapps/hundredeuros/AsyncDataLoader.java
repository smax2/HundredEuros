package com.maxedapps.hundredeuros;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class AsyncDataLoader extends AsyncTask<Void, Void, String[]> {

    private Context mContext;
    private String mStatusMessage;
    private OnDataLoadedListener mListener;

    public interface OnDataLoadedListener {
        public void OnDataLoaded();
    }

    public AsyncDataLoader(Context context, OnDataLoadedListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String[] data = fetchData();
            Bitmap img = downloadImage(data[1]);
            if (img != null) {
                String imgFilename = "img_" + data[0] + ".png";
                FileHelper.saveImage(mContext, img, imgFilename);
                data[1] = imgFilename;
                return data;
            } else {
                mStatusMessage = "Error!";
                return null;
            }
        } else {
            mStatusMessage = "Error!";
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] s) {
        super.onPostExecute(s);
        SharedPreferences prefs = mContext.getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainActivity.KEY_LAST_UPDATE, s[0]);
        editor.putString(MainActivity.KEY_CURRENT_IMAGE_FILENAME, s[1]);
        editor.putString(MainActivity.KEY_CURRENT_DESCRIPTION, s[2]);
        editor.commit();
        mListener.OnDataLoaded();
    }

    private String[] fetchData() {
        InputStream is = null;
        JSONObject jObj = null;
        Locale current = Locale.getDefault();
        String languageCode = current.getLanguage();
        try {
            URL url = new URL("http://192.168.178.44/Android_HundredEuros/getData.php?language=" + languageCode);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int response = connection.getResponseCode();
            if (response != 200) {
                mStatusMessage = "Error!";
                return null;
            }
            is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String json =  sb.toString();
            jObj = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (jObj != null) {
            try {
                String result[] = new String[3];
                result[0] = jObj.getString("Date");
                result[1] = jObj.getString("ImageUrl");
                result[2] = jObj.getString("Description");
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private Bitmap downloadImage(String imageUrl) {
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int response = connection.getResponseCode();
            if (response != 200) {
                mStatusMessage = "Error!";
                return null;
            }
            is = connection.getInputStream();
            bis = new BufferedInputStream(is);
            return BitmapFactory.decodeStream(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
