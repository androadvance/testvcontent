package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String FORMAT = "%02d:%02d:%02d";
    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/TimingSVC.asmx";
    GridView gridView;
    ImageButton btnStart, btnEnd, btnReport;
    TextView tVtotalTime;
    Date start, end;
    List<Map<String, Date>> matrix = new ArrayList<>();
    public int operationcount = 0;
    public Context context;
    public String mobno;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    Handler handler;
    int Seconds, Minutes, MilliSeconds;
    EditText eTstyleNo;
    ScrollView scrollView;
    ActionBar actionBar;
    int opreached = 0;
    SqliteDatabase sqliteDatabase;
    private AdView mAdView;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WriteCount();
        context = getApplicationContext();
        actionBar = getActionBar();
        operationcount = getCount();
        btnStart = findViewById(R.id.btnStart);
        btnEnd = findViewById(R.id.btnEnd);
        btnReport = findViewById(R.id.btnReport);
        tVtotalTime = findViewById(R.id.tVtotalTime);
        Bundle extras = getIntent().getExtras();
        eTstyleNo = findViewById(R.id.eTstyleNo);
        scrollView = findViewById(R.id.l1);

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

        if (extras != null) {

            mobno = extras.getString("number");

        }


        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start == null) {
                    Helper.InfoMsg("Warning", "Please Click Start", MainActivity.this);
                    return;
                }
                if (end == null) {
                    Helper.InfoMsg("Warning", "Please Click End", MainActivity.this);
                    return;
                }
                (new insertOpTiming()).execute(mobno);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(eTstyleNo.getText().toString())){
                    eTstyleNo.setError("Enter Style Number");
                } else {
                    opreached = 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
                    String currentDateandTime = sdf.format(new Date());
                    start = new Date();
                    matrix.clear();
                    Map<String, Date> row = new HashMap<>();
                    row.put("0", start);
                    matrix.add(row);
                    eTstyleNo.setEnabled(false);
                    handler = new Handler();
                    StartTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 0);
                    end = null;
                }
            }
        });
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start == null) {
                    Helper.InfoMsg("Warning", "Please Click Start", MainActivity.this);
                    return;
                } else if (end != null) {
                    Helper.InfoMsg("Warning", "Task Already Completed", MainActivity.this);
                    return;
                }
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
                String currentDateandTime = sdf.format(new Date());
                end = new Date();
                //    tVEnd.setText(currentDateandTime.toString());
                String result = substractDates(start, end);
                //    tVtotalTime.setText(result);
                Map<String, Date> row = new HashMap<>();
                row.put(Integer.toString(operationcount + 1), end);
                matrix.add(row);

            }
        });
        TableLayout layout = findViewById(R.id.tblmaster);
        int i = 1;
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().

                getDefaultDisplay().

                getMetrics(displayMetrics);

        double height = displayMetrics.heightPixels;
        double width = displayMetrics.widthPixels;
        int boxwidth = 0, boxheight = 0;
        int density = getResources().getDisplayMetrics().densityDpi;
        //   Toast.makeText(context, height+","+width, Toast.LENGTH_SHORT).show();
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                // Toast.makeText(context, "LDPI", Toast.LENGTH_SHORT).show();
                boxwidth = 100;
                boxheight = 100;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                // Toast.makeText(context, "MDPI", Toast.LENGTH_SHORT).show();
                boxwidth = 100;
                boxheight = 100;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                //  Toast.makeText(context, "HDPI", Toast.LENGTH_SHORT).show();
                boxwidth = 100;
                boxheight = 100;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                boxwidth = 100;
                boxheight = 100;
                //  Toast.makeText(context, "XHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                boxwidth = 100;
                boxheight = 100;
                //    Toast.makeText(context, "XXHDPI", Toast.LENGTH_SHORT).show();
            case DisplayMetrics.DENSITY_XXXHIGH:
                boxwidth = 100;
                boxheight = 100;
                //  Toast.makeText(context, "XXXHDPI", Toast.LENGTH_SHORT).show();
            default:
                boxwidth = 100;
                boxheight = 100;
        }

        //deciding row and column length
        Double clmn = 0.0;
        while ((width - (boxwidth + 10)) >= 0) {
            width = width - (boxwidth + 10);
            clmn = clmn + 1.00;
        }

        double r1 = Math.ceil(operationcount / (clmn));
        height = (boxheight + 10) * r1;
        int r = 0;
        while ((height - boxheight + 10) >= 0) {
            height = height - boxheight + 10;
            r++;
        }

        Button btn;
        for (
                int k = 1;
                k <= r; k++) {
            TableRow row = new TableRow(this);
            innerloop:
            for (int l = 1; l <= clmn; l++) {
                btn = new Button(this);
                TableRow.LayoutParams tr = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                tr.setMargins(10, 10, 10, 10);
                //  layout.setWeightSum(12.0f);
                tr.weight = 0;
                //   btn.setBackgroundResource(R.drawable.icbtn);
                btn.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                btn.setTextColor(getResources().getColor(R.color.colorWhite));
                btn.setLayoutParams(tr);
                btn.setHeight(boxheight);
                btn.setWidth(boxwidth);
                btn.setMinHeight(boxheight);
                btn.setMinimumHeight(boxheight);
                btn.setMinimumWidth(boxheight);
                btn.setMinWidth(boxwidth);
                btn.setId(i);


                //btn.setTextSize(pxFromDp(18, MainActivity.this));
                btn.setText(Integer.toString(i));
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (start != null) {
                            opreached++;
                            Button bt = (Button) v;
                            v.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                            ((Button) v).setTextColor(getResources().getColor(R.color.colorBlack));
                            v.setEnabled(false);
                            Toast.makeText(getApplicationContext(), ((Button) v).getText(), Toast.LENGTH_SHORT).show();
                            Map<String, Date> row = new HashMap<>();
                            Date d = new Date();
                            row.put(((Button) v).getText().toString(), d);
                            matrix.add(row);
                            if (opreached == operationcount) {
                                TimeBuff += MillisecondTime;
                                handler.removeCallbacks(runnable);
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
                                String currentDateandTime = sdf.format(new Date());
                                end = new Date();
                                //    tVEnd.setText(currentDateandTime.toString());
                                String result = substractDates(start, end);
                                //    tVtotalTime.setText(result);
                                row = new HashMap<>();
                                row.put(Integer.toString(operationcount + 1), end);
                                matrix.add(row);

                            }
                        } else
                            Helper.InfoMsg("Warning", "Please Click Start ", MainActivity.this);
                    }
                });
                row.addView(btn);
                i++;
                if (i > operationcount)
                    break;
            }
            layout.addView(row);
            if (i > operationcount)
                break;
        }

    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);
            final String s = "" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds);
            //   actionBar        .setTitle(s);

            (new UiUpdate()).execute(s);

            handler.postDelayed(this, 0);
        }

    };

    private String substractDates(Date date1, Date date2) {
        long mills = date2.getTime() - date1.getTime();
        long hr = (mills / (1000 * 60 * 60)) % 60;
        long mins = (mills / (1000 * 60)) % 60;
        long scns = (mills / (1000)) % 60;

        return Long.toString(hr) + ":" + Long.toString(mins) + ":" + Long.toString(scns);
    }

    public static float pxFromDp(float dp, Context mContext) {
        return dp * mContext.getResources().getDisplayMetrics().density;
    }

    class UiUpdate extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            tVtotalTime.setText(result);
        }
    }

    public String GetSno() {
        SoapObject request = new SoapObject(NAMESPACE, "S_NextOpReportNo");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
        try {
            AndroidHttpTransportSE.call(NAMESPACE + "S_NextOpReportNo", envelope);
        } catch (IOException | XmlPullParserException e) {
            Helper.Msg("Something Went Wrong..", 0, MainActivity.this);
        }
        SoapPrimitive response;
        try {
            response = (SoapPrimitive) envelope.getResponse();
            return response.toString();
        } catch (Exception e) {
            Helper.Msg("Something Went Wrong..", 0, MainActivity.this);
        }
        return "0";
    }

    class insertOpTiming extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(MainActivity.this);
            pdia.setMessage("Generating Report..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... username) {
            String responsetring = "";
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start);
                long strtMSec = calendar.getTimeInMillis();

                int sno = Integer.parseInt(GetSno());
                int i = 0;
                for (Map<String, Date> map : matrix) {
                    for (Map.Entry<String, Date> mapEntry : map.entrySet()) {
                        String key = mapEntry.getKey();
                        Date value = mapEntry.getValue();
                        SoapObject request = new SoapObject(NAMESPACE, "insertOpTiming");
                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                        envelope.dotNet = true;
                        envelope.setOutputSoapObject(request);
                        calendar.setTime(value);
                        long endMSec = calendar.getTimeInMillis();
                        request.addProperty("mobno", mobno);
                        request.addProperty("OperationId", key);
                        request.addProperty("StartTime", strtMSec);
                        request.addProperty("EndTime", endMSec);
                        request.addProperty("DiffTime", endMSec - strtMSec);
                        request.addProperty("OrderStyle", eTstyleNo.getText().toString());
                        request.addProperty("sno", sno);

                        HttpTransportSE AndroidHttpTransportSE = new HttpTransportSE(URL);
                        try {
                            AndroidHttpTransportSE.call(NAMESPACE + "insertOpTiming", envelope);
                        } catch (IOException | XmlPullParserException e) {
                            Helper.Msg("Something Went Wrong..", 0, MainActivity.this);
                        }
                        SoapPrimitive response;
                        try {
                            response = (SoapPrimitive) envelope.getResponse();
                            responsetring = response.toString();
                            if (responsetring.equalsIgnoreCase("ok")) {
                            } else {
                                Helper.Msg(responsetring, 0, MainActivity.this);
                            }
                        } catch (Exception e) {
                            Helper.Msg("Something Went Wrong..", 0, MainActivity.this);
                        }
                    }
                    i++;
                }

                return Integer.toString(sno);
            } catch (Exception e) {
                e.printStackTrace();
                return "0";
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!result.equalsIgnoreCase("0")) {
                Helper.Msg("Loading Report", 0, context);
                Intent intent = new Intent(MainActivity.this, ReportView.class);
                intent.putExtra("sno", result);
                intent.putExtra("styleno", eTstyleNo.getText().toString());
                startActivity(intent);
            }
            pdia.dismiss();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        if (id == R.id.newitem) {
            this.recreate();
            return true;
        } else if (id == R.id.edit_myprofile) {
            Intent intent = new Intent(MainActivity.this, UserUpdate.class);
            intent.putExtra("mobilenumber",mobno);
            startActivity(intent);
        } else if (id == R.id.changeparticular) {
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.prompts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // get user input and set it to result
                                    // edit text
                                    WriteCount(userInput.getText().toString());
                                    MainActivity.this.recreate();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        } else if (id == R.id.reports) {
            Intent intent = new Intent(MainActivity.this, OpTimingReport.class);
            intent.putExtra("mobno", mobno);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void WriteCount(String data) {
        try {
            File testFile = new File(this.getFilesDir(), "particularconfig.txt");
            if (!testFile.exists())
                testFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, false));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
        }
    }

    private void WriteCount() {

        try {
            File testFile = new File(this.getFilesDir(), "particularconfig.txt");
            if (!testFile.exists()) {
                testFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, false /*append*/));
                writer.write("31");
                writer.close();
            }

        } catch (IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the TestFile.txt file.");
        }
    }

    private Integer getCount() {
        String textFromFile = "0";

        File testFile = new File(this.getFilesDir(), "particularconfig.txt");
        if (testFile != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(testFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    textFromFile += line.toString();
                }
                reader.close();
            } catch (Exception e) {
                textFromFile = "0";
                Log.e("ReadWriteFile", "Unable to read the TestFile.txt file.");
            }
        }
        return Integer.parseInt(textFromFile.toString());
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
