package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class SplashScreen extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URLip = "http://182.76.164.193/opTiming/LoginSVC.asmx";
    String versioncode = "0";
    SqliteDatabase sqliteDatabase;
    TelephonyManager telephonyManager;
    public static String deviceid = "";
    public static String mobno = "";
    String mobilenumber;
    int REQUEST_CODE = 0;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.RESTART_PACKAGES,
            Manifest.permission.INSTALL_PACKAGES
    };
    private AdView mAdView;
    ImageView imageView;
    TextView titile;
    Animation side,bottom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageView = findViewById(R.id.imageView);
        titile = findViewById(R.id.title);

        side = AnimationUtils.loadAnimation(this,R.anim.side);
        bottom = AnimationUtils.loadAnimation(this,R.anim.bottom);

        imageView.setAnimation(side);
        titile.setAnimation(bottom);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdClosed() {
                //    Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //    Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                //    Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });

        getSupportActionBar().hide();
        int secondsDelayed = 3;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Context context = getApplicationContext(); // or activity.getApplicationContext()
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            versioncode = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            versioncode = "1";
        }


        sqliteDatabase = new SqliteDatabase(SplashScreen.this);
        //  deviceId();



        new Handler().postDelayed(new Runnable() {
            public void run() {

                (new CheckVersion()).execute(versioncode, getResources().getString(R.string.app_name));

            }
        }, secondsDelayed * 1500);


    }


    public class CheckVersion extends AsyncTask<String, String, String> {

        SweetAlertDialog bar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar = new SweetAlertDialog(SplashScreen.this, SweetAlertDialog.PROGRESS_TYPE);
            bar.setCancelable(false);
            bar.setContentText("Please Wait");
            bar.setCanceledOnTouchOutside(false);
            bar.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String[] paras = {"Version", "Appname"};
            String[] values = {params[0], params[1]};
            String methodname = "Appcheck";
            return Helper.WebServiceCall(paras, values, methodname, NAMESPACE, URLip);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            bar.dismiss();

            try {

                if (result.equals("false")) {

                    Helper.dialog_error(SplashScreen.this, "Warning", "No AppInfo Available For This Application..Please Update or Contact IT-Team", "ok");

                } else {

                    String[] splitrows = result.split(";");
                    String Status = splitrows[2];
                    String ExpiryDate = splitrows[3];
                    String Life = splitrows[4];
                    int ExpireDayCount = Integer.parseInt(splitrows[5]) + 1;

                    if (Status.equalsIgnoreCase("true")) {

                        if (Life.equalsIgnoreCase("false")) {

                            Helper.dialog_error(SplashScreen.this, "Warning", "Your Application Is Expired", "ok");

                        } else {

                            if (ExpireDayCount < 5) {

                                Helper.dialog_error(SplashScreen.this, "Warning", "Your Application going To Expire within" + splitrows[5] + " Days", "ok");

                            } else if (ExpireDayCount < 0) {

                                Helper.dialog_error(SplashScreen.this, "Warning", "Your Application  Expired please update new one", "ok");

                            } else if (Status.equalsIgnoreCase("false")) {

                                Helper.dialog_error(SplashScreen.this, "Warning", "Your Application Currently Stopped", "ok");

                            } else {

                                openactivity();
                            }

                        }
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Helper.dialog_error(SplashScreen.this, "Warning", e.toString(), "ok");
            }
        }
    }


    public void openactivity() {

        Cursor c;
        c = sqliteDatabase.fetchData();
        c.moveToFirst();

        if (c.getCount() > 0) {

            String email = c.getString(c.getColumnIndex("Mailid"));

            Cursor c1;
            c1 = sqliteDatabase.fetchMobilenumber(email);
            c1.moveToFirst();
            mobilenumber = c1.getString(c1.getColumnIndex("MobileNumber"));
        }

        String s = sqliteDatabase.getuser();

        if (!s.isEmpty()) {

            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.putExtra("number", mobilenumber);
            startActivity(intent);

        } else {

            Intent intent1 = new Intent(SplashScreen.this, userReg.class);
            startActivity(intent1);
        }
    }

   /* private void deviceId() {

        telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
            return;
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
                        return;
                    }
                  //  deviceid = telephonyManager.getDeviceId();
                    mobno = telephonyManager.getLine1Number();
                    Toast.makeText(SplashScreen.this, deviceid, Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(SplashScreen.this, "Without permission we check", Toast.LENGTH_LONG).show();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/
}

