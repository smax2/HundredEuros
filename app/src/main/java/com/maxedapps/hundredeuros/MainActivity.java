package com.maxedapps.hundredeuros;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends Activity implements AsyncDataLoader.OnDataLoadedListener {
    private AdRequest mAdRequest;
    private TextView mTxtDescription;
    private ImageView mImageView;
    private AdView mAdView;
    private Button mBtnContinue;
    private ProgressBar mProgressBar;
    private SharedPreferences mPrefs;
    private boolean mDataLoaded = false;
    private boolean mAdLoaded = false;

    public static final String SHARED_PREFERENCES = "data_manager";
    public static final String KEY_LAST_UPDATE = "last_update";
    public static final String KEY_CURRENT_IMAGE_FILENAME = "current_image_filename";
    public static final String KEY_CURRENT_DESCRIPTION = "current_description";

    private static final int DURATION_WAIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtDescription = (TextView) this.findViewById(R.id.txtDescription);
        mBtnContinue = (Button) this.findViewById(R.id.btnContinue);
        mImageView = (ImageView) findViewById(R.id.imgMoney);
        mBtnContinue.setVisibility(View.INVISIBLE);
        mTxtDescription.setVisibility(View.INVISIBLE);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mAdRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(mAdRequest);
        mAdView.setVisibility(View.INVISIBLE);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdLoaded = true;
                if (mDataLoaded) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mTxtDescription.setVisibility(View.VISIBLE);
                }
            }
        });
        mPrefs = getSharedPreferences(SHARED_PREFERENCES,MODE_PRIVATE);
        if (isNewDataAvailable()) {
            AsyncDataLoader dataLoader = new AsyncDataLoader(this, this);
            dataLoader.execute();
        } else {
            mDataLoaded = true;
        }
    }

    public void OnImageTouched(View view) {
        if (!mDataLoaded || !mAdLoaded)
            return;
        mBtnContinue.setVisibility(View.VISIBLE);
        mBtnContinue.setEnabled(false);
        startTimer();
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_left_out);
        set.setTarget(mImageView);
        set.start();
        mAdView.setVisibility(View.VISIBLE);
        AnimatorSet set2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_left_in);
        set2.setTarget(mAdView);
        set2.start();
    }

    public void OnAdSkipped(View view) {
        Bitmap image = FileHelper.loadImage(this, mPrefs.getString(KEY_CURRENT_IMAGE_FILENAME,""));

        if (image != null)
            mImageView.setImageBitmap(image);
        else
            mImageView.setImageResource(R.drawable.ic_launcher);
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_left_out);
        set.setTarget(mAdView);
        set.start();
        mImageView.setVisibility(View.VISIBLE);
        AnimatorSet set2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_left_in);
        set2.setTarget(mImageView);
        set2.start();
        mTxtDescription.setText(mPrefs.getString(KEY_CURRENT_DESCRIPTION, "Error!"));
        mTxtDescription.setVisibility(View.VISIBLE);
        mBtnContinue.setVisibility(View.INVISIBLE);
    }

    private void startTimer() {
        CountDownTimer timer = new CountDownTimer(DURATION_WAIT * 1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTxtDescription.setText(getResources().getQuantityString(R.plurals.hint_wait_time, (int) (millisUntilFinished / 1000), (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                mTxtDescription.setVisibility(View.INVISIBLE);
                mBtnContinue.setEnabled(true);
            }
        };
        timer.start();
    }

    private boolean isNewDataAvailable() {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(mPrefs.getString(KEY_LAST_UPDATE,"1900-01-01"));
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            Date now = sdf.parse(year + "-" + month + "-" + day);
            if (date.before(now) || mPrefs.getString(KEY_CURRENT_IMAGE_FILENAME, "") == "" || mPrefs.getString(KEY_CURRENT_DESCRIPTION, "") == "") {
                result = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            result = true;
        }
        return result;
    }

    @Override
    public void OnDataLoaded() {
        mDataLoaded = true;
        if (mAdLoaded) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mTxtDescription.setVisibility(View.VISIBLE);
        }
    }
}
