package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;

public class UserLogin extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/LoginSVC.asmx";
    EditText eTmob,eTotp;
    Button btnlogin,btnotp;
    LinearLayout linearLayout;
    SqliteDatabase sqliteDatabase;
    public JSONArray jsonArray = null;
    String username,company,mailid;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        eTmob = findViewById(R.id.eTmob);
        eTotp = findViewById(R.id.eTotp);
        btnlogin = findViewById(R.id.btnlogin);
        btnotp = findViewById(R.id.btnotp);
        linearLayout = findViewById(R.id.linearlayout);

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

        sqliteDatabase = new SqliteDatabase(this);


        btnotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(eTmob.getText().toString())) {

                    eTmob.setError("MobileNumber field must not be empty");

                } else {

                    (new UserMobileNumberVerify()).execute(eTmob.getText().toString());
                }
            }
        });

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(eTmob.getText().toString())) {

                    eTmob.setError("Email or MobileNumber field must not be empty");

                } else if (TextUtils.isEmpty(eTotp.getText().toString())) {

                    eTotp.setError("OTP field must not be empty");

                } else {

                    (new UsersLogin()).execute(eTmob.getText().toString(),eTotp.getText().toString());

                }
            }
        });
    }


    public class UserMobileNumberVerify extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(UserLogin.this);
            pdia.setMessage("Login Verify..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... params) {
            String[] paras = {"mobno"};
            String[] values = {params[0]};
            String methodname = "UserMobileNumberVerify";
            return Helper.WebServiceCall(paras, values, methodname, NAMESPACE, URL);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdia.dismiss();

            if (result.equals("OK")) {

                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btnotp.getWindowToken(), 0);

                linearLayout.setVisibility((linearLayout.getVisibility() == View.VISIBLE)
                        ? View.VISIBLE : View.VISIBLE);
                (new getUserDetails()).execute(eTmob.getText().toString());
                Toast.makeText(UserLogin.this, "OTP sent to your registered mobile number", Toast.LENGTH_SHORT).show();

            } else {

                Helper.InfoMsg("Error", result , UserLogin.this);
                //Toast.makeText(UserLogin.this, "User not Valid....Please registered your mobile number", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class UsersLogin extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(UserLogin.this);
            pdia.setMessage("Sending OTP..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... arg) {
            String responsetring = "";
            try {
                SoapObject request = new SoapObject(NAMESPACE, "UserLogin");
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);

                request.addProperty("mobno", arg[0]);
                request.addProperty("otp", arg[1]);

                HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
                try {
                    AndroidHttpTransportSE.call(NAMESPACE + "UserLogin", envelope);
                } catch (IOException | XmlPullParserException e) {
                    Helper.Msg("Something Went Wrong..", 0, UserLogin.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring = response.toString();
                } catch (Exception e) {
                    Helper.Msg("Something Went Wrong..", 0, UserLogin.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdia.dismiss();
            if (result.equals("ok")) {

                sqliteDatabase.saveit(username,company,mailid,eTmob.getText().toString());
                Intent intent = new Intent(UserLogin.this,MainActivity.class);
                intent.putExtra("number",eTmob.getText().toString());
                startActivity(intent);
            } else {

                Helper.InfoMsg("Error", result , UserLogin.this);
            }

        }
    }


    public class getUserDetails extends AsyncTask<String,String,String>{

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(UserLogin.this);
            pdia.setMessage("Loading..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String[] paras = {"mobno"};
            String[] values = {params[0]};
            String methodname = "getUserDetails";
            return Helper.WebServiceCall(paras, values, methodname, NAMESPACE, URL);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdia.dismiss();

            try {
                jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonobj = new JSONObject(jsonArray.get(i).toString());
                    username = jsonobj.getString("username");
                    company = jsonobj.getString("companyname");
                    mailid = jsonobj.getString("mailid");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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