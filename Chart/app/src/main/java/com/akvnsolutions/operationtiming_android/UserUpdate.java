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
import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class UserUpdate extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/LoginSVC.asmx";
    String[] permissions = new String[]{
            Manifest.permission.GET_ACCOUNTS
    };
    int REQUEST_CODE = 0;
    EditText eTMail;
    Button btngetotp;
    Button btnReg, btnEdit, btnCancel;
    EditText eTMobNo;
    EditText eTCompany;
    EditText eTUsername;
    EditText eTOtp;
    SqliteDatabase sqliteDatabase;
    String mobno;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);
        Bundle extras = getIntent().getExtras();
        eTMail = findViewById(R.id.eTMail);
        btngetotp = findViewById(R.id.btnGetOtp);
        btnEdit = findViewById(R.id.btnEdit);
        btnCancel = findViewById(R.id.btnCancel);

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

        if (extras != null) {

            mobno = extras.getString("mobilenumber");

        }

        sqliteDatabase = new SqliteDatabase(this);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new userinfo()).execute(mobno);
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eTMail.setEnabled(false);
                btngetotp.setEnabled(true);
                eTUsername.setEnabled(true);
                eTMobNo.setEnabled(false);
                eTCompany.setEnabled(true);
                eTOtp.setEnabled(true);
                btnReg.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.INVISIBLE);
            }
        });
        eTUsername = findViewById(R.id.eTUsername);
        eTMobNo = findViewById(R.id.eTMobNo);
        eTCompany = findViewById(R.id.eTCompany);
        eTOtp = findViewById(R.id.eTOtp);
        btnReg = findViewById(R.id.btnReg);

        (new userinfo()).execute(mobno);

        btngetotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(eTUsername.getText().toString())) {
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
                    (new userup()).execute(params);
                }

            }
        });

        if (ActivityCompat.checkSelfPermission(UserUpdate.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserUpdate.this, permissions, REQUEST_CODE);
        } else {
            /*AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType("com.google");
            // Account[] accounts = manager.getAccounts();
            List<String> possibleEmails = new LinkedList<String>();

            for (Account account : accounts) {
                // TODO: Check possibleEmail against an email regex or treat
                // account.name as an email address only for certain account.type values.
                possibleEmails.add(account.name);
            }
            if (possibleEmails.size() > 0)
                eTMail.setText(possibleEmails.get(0));*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(UserUpdate.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserUpdate.this, permissions, REQUEST_CODE);
        } else {
            AccountManager manager = AccountManager.get(this);
            //  Account[] accounts = manager.getAccountsByType("com.google");
            Account[] accounts = manager.getAccounts();
            List<String> possibleEmails = new LinkedList<String>();

            for (Account account : accounts) {
                // TODO: Check possibleEmail against an email regex or treat
                // account.name as an email address only for certain account.type values.
                possibleEmails.add(account.name);
            }
            if (possibleEmails.size() > 0)
                eTMail.setText(possibleEmails.get(0));
        }
    }

    class userup extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(UserUpdate.this);
            pdia.setMessage("Registering User..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... arg) {
            String responsetring = "";
            try {
                SoapObject request = new SoapObject(NAMESPACE, "userupdate");
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                request.addProperty("username", arg[0]);
                request.addProperty("mobno", arg[1]);
                request.addProperty("mailid", arg[2]);
                request.addProperty("companyname", arg[3]);
                request.addProperty("otp", arg[4]);

                HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
                try {
                    AndroidHttpTransportSE.call(NAMESPACE + "userupdate", envelope);
                } catch (IOException | XmlPullParserException e) {
                    Helper.Msg("Something Went Wrong..", 0, UserUpdate.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring = response.toString();
                } catch (Exception e) {
                    Helper.Msg("Something Went Wrong..", 0, UserUpdate.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {

                Intent intent = new Intent(UserUpdate.this, MainActivity.class);
                boolean isUpdate = sqliteDatabase.updateData(eTUsername.getText().toString(),
                        eTCompany.getText().toString(),
                        eTMail.getText().toString(), eTMobNo.getText().toString());

                if (isUpdate == true) {
                    Helper.Msg("user info updated successfully", 1, UserUpdate.this);
                } else {
                    Toast.makeText(UserUpdate.this, "Data not Updated", Toast.LENGTH_LONG).show();
                }
                startActivity(intent);

            } else {
                Helper.InfoMsg("Error", result, UserUpdate.this);
            }

            pdia.dismiss();
        }
    }


    class sendOtp extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(UserUpdate.this);
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

                request.addProperty("mobno", arg[0]);

                HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
                try {
                    AndroidHttpTransportSE.call(NAMESPACE + "sendotp", envelope);
                } catch (IOException | XmlPullParserException e) {
                    Helper.Msg("Something Went Wrong..", 0, UserUpdate.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring = response.toString();
                } catch (Exception e) {
                    Helper.Msg("Something Went Wrong..", 0, UserUpdate.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {

                Helper.Msg("OTP sent to your mobile number", 0, UserUpdate.this);
            }
            pdia.dismiss();
        }
    }

    class userinfo extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(UserUpdate.this);
            pdia.setMessage("Checking device details..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... params) {
            String responsetring = "";
            try {

                SoapObject request = new SoapObject(NAMESPACE, "checkdevice");
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                request.addProperty("mobileno", params[0]);
                HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
                try {
                    AndroidHttpTransportSE.call(NAMESPACE + "checkdevice", envelope);
                } catch (IOException | XmlPullParserException e) {
                    Helper.Msg("Something Went Wrong..", 0, UserUpdate.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring = response.toString();
                } catch (Exception e) {
                    Helper.Msg("Something Went Wrong..", 0, UserUpdate.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("[]")) {

            } else {
                try {
                    String OrderStyle = "";
                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobj = jsonarray.getJSONObject(i);
                        eTUsername.setText(jsonobj.getString("username"));
                        eTCompany.setText(jsonobj.getString("companyname"));
                        eTMobNo.setText(jsonobj.getString("mobno"));
                        eTMail.setText(jsonobj.getString("mailid"));
                    }
                    eTMail.setEnabled(false);
                    btngetotp.setEnabled(false);
                    eTUsername.setEnabled(false);
                    eTMobNo.setEnabled(false);
                    eTCompany.setEnabled(false);
                    eTOtp.setEnabled(false);
                    btnReg.setVisibility(View.INVISIBLE);
                    btnCancel.setVisibility(View.INVISIBLE);
                } catch (Exception er) {
                    Helper.InfoMsg("Info", "Data Parse Failed..", UserUpdate.this);
                }
            }
            pdia.dismiss();
        }
    }
}
