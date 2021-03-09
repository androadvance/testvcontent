package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OpTimingReport extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/TimingSVC.asmx";
    ListView lv;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op_timing_report);

        lv= (ListView) findViewById(R.id.LvOpList);

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

        // Create a List from String Array elements
        Bundle bundle=getIntent().getExtras();

        (new GetOpReport()).execute(bundle.getString("mobno"));

        // Set an item click listener for ListView
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);
                String[]  strings=selectedItem.split("/");
                //  Intent intent = new Intent(OpTimingReport.this, GraphReport.class);
                Intent intent = new Intent(OpTimingReport.this, ReportView.class);
                intent.putExtra("styleno",strings[1]);
                intent.putExtra("sno",strings[0]);
                startActivity(intent);
                finish();
            }
        });
    }
    class GetOpReport extends AsyncTask<String, String, String> {
        private ProgressDialog pdia;
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(OpTimingReport.this);
            pdia.setMessage("Checking device details..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }
        protected String doInBackground(String... username) {
            String responsetring="";
            try
            {
                SoapObject request=new SoapObject(NAMESPACE,"S_GetOpTimingDtl");
                SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet=true;
                envelope.setOutputSoapObject(request);
                request.addProperty("mobno",username[0]);
                HttpTransportSE AndroidHttpTransportSE=new HttpTransportSE(URL);
                try{
                    AndroidHttpTransportSE.call(NAMESPACE+"S_GetOpTimingDtl",envelope);
                }
                catch(IOException | XmlPullParserException e){
                    Helper.Msg( "Something Went Wrong..",0, OpTimingReport.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring=response.toString();
                }
                catch(Exception e){
                    Helper.Msg( "Something Went Wrong..",0, OpTimingReport.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            List<String> fruits_list = new ArrayList<String>();



            // Populate ListView with items from ArrayAdapter

            if(result.equals("[]")){

            }
            else {
                try {
                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobj = jsonarray.getJSONObject(i);
                        String sno=jsonobj.getString("Sno");
                        String OrderStyle=jsonobj.getString("OrderStyle");
                        if(OrderStyle.isEmpty())OrderStyle="-";
                        String entrydate=jsonobj.getString("entrydate");
                        fruits_list.add(sno+" / "+OrderStyle +" / "+ConvertJsonDate(entrydate,"dd.MMM.yyyy"));
                    }
                    // Create a ArrayAdapter from List
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                            (OpTimingReport.this, android.R.layout.simple_list_item_1, fruits_list);
                    lv.setAdapter(arrayAdapter);
                }
                catch (Exception er)
                {
                    Helper.InfoMsg("Info", "Data Parse Failed..", OpTimingReport.this);
                }
            }

            pdia.dismiss();
        }
    }
    public    String ConvertJsonDate(String jsondate,String pattern)
    {


        jsondate=jsondate.replace("/Date(", "").replace(")/", "");
        long time = Long.parseLong(jsondate);
        Date d= new Date(time);


        return new SimpleDateFormat(pattern).format(d).toString();
    }
    public  String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}

