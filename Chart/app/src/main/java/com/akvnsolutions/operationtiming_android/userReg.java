package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class userReg extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/LoginSVC.asmx";
    EditText eTMail;
    Button btngetotp;
    Button btnReg;
    Button btnlogin;
    EditText eTMobNo;
    EditText eTCompany;
    EditText eTUsername;
    EditText eTOtp;
    SqliteDatabase sqliteDatabase;
    public String deviceid;
    String[] permissions = new String[]{
            Manifest.permission.GET_ACCOUNTS
    };
    int REQUEST_CODE = 0;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg);

        eTMail = findViewById(R.id.eTMail);
        btngetotp = findViewById(R.id.btnGetOtp);
        Bundle extras = getIntent().getExtras();
        eTUsername = findViewById(R.id.eTUsername);
        eTMobNo = findViewById(R.id.eTMobNo);
        eTCompany = findViewById(R.id.eTCompany);
        eTOtp = findViewById(R.id.eTOtp);
        btnReg = findViewById(R.id.btnReg);
        btnlogin = findViewById(R.id.btnLogin);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        sqliteDatabase = new SqliteDatabase(this);



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


        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(userReg.this, UserLogin.class);
                startActivity(intent);

            }
        });


        btngetotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!emailValidator(eTMail.getText().toString())) {

                    Toast.makeText(userReg.this, "Invalid email", Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(eTUsername.getText().toString())) {
                    eTUsername.setError("Username field must not be empty");
                } else if (TextUtils.isEmpty(eTCompany.getText().toString())) {
                    eTCompany.setError("Company field must not be empty");
                } else if (TextUtils.isEmpty(eTMobNo.getText().toString())) {
                    eTMobNo.setError("MobileNumber field must not be empty");
                } else {

                    (new sendOtp()).execute(new String[]{eTMobNo.getText().toString()});

                }
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(eTOtp.getText().toString())) {

                    eTOtp.setError("OTP field must not be empty");

                } else {

                    String[] params = new String[]{eTUsername.getText().toString(), eTMobNo.getText().toString()
                            , eTMail.getText().toString(), eTCompany.getText().toString(), eTOtp.getText().toString()
                    };

                    (new userRegistration()).execute(params);
                }
            }
        });

    }


    class userRegistration extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(userReg.this);
            pdia.setMessage("Registering User..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... params) {
            String[] paras = {"username", "mobno", "mailid", "companyname", "otp"};
            String[] values = {params[0], params[1], params[2], params[3], params[4]};
            String methodname = "usercreate";
            return Helper.WebServiceCall(paras, values, methodname, NAMESPACE, URL);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {

                sqliteDatabase.saveit(eTUsername.getText().toString(), eTCompany.getText().toString(), eTMail.getText().toString(), eTMobNo.getText().toString());
                Helper.Msg("user created successfully", 1, userReg.this);
                finish();
                Intent intent = new Intent(userReg.this, MainActivity.class);
                intent.putExtra("number", eTMobNo.getText().toString());
                startActivity(intent);

            } else
                Helper.InfoMsg("Error", "Emailid Already Registrated...Please Login", userReg.this);
            pdia.dismiss();
        }
    }

    class sendOtp extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(userReg.this);
            pdia.setMessage("Sending OTP..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... arg) {
            String responsetring = "";
            try {
                SoapObject request = new SoapObject(NAMESPACE, "sendotp");
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                // request.addProperty("deviceid", arg[0]);
                request.addProperty("mobno", arg[0]);

                HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
                try {
                    AndroidHttpTransportSE.call(NAMESPACE + "sendotp", envelope);
                } catch (IOException | XmlPullParserException e) {
                    Helper.Msg("Something Went Wrong..", 0, userReg.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring = response.toString();
                } catch (Exception e) {
                    Helper.Msg("Something Went Wrong..", 0, userReg.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {

                Helper.Msg("OTP sent to your mobile number", 0, userReg.this);

            }
            pdia.dismiss();
        }
    }

    public boolean emailValidator(String email) {

        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }
}
