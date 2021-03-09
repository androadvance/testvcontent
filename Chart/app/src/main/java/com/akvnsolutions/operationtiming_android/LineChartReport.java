package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.ArrayList;


public class LineChartReport extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/TimingSVC.asmx";
    LineChart lineChart;
    TextView tVStyleono;
    ImageButton imgPitchChart,imgBarChart,imgMail,imggrid;
    String sno,styleno;


    ArrayList<Entry> datavalues;
    ArrayList<ILineDataSet> dataSets;
    LineDataSet lineDataSet ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart_report);

        Bundle b=getIntent().getExtras();
        tVStyleono=findViewById(R.id.tVStyleNo);
        imgPitchChart=findViewById(R.id.imgPitchChart);
        imgBarChart=findViewById(R.id.imgBarChart);
        lineChart = findViewById(R.id.lineChart);

        imggrid=findViewById(R.id.imgGrid);
        imggrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LineChartReport.this, ReportView.class);
                intent.putExtra("sno",sno);
                intent.putExtra("styleno",styleno);
                startActivity(intent);
                finish();
            }
        });
        imgPitchChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LineChartReport.this, LineChartReport.class);
                intent.putExtra("sno",sno);
                intent.putExtra("styleno",styleno);
                startActivity(intent);
                finish();
            }
        });
        imgBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LineChartReport.this, GraphReport.class);
                intent.putExtra("sno",sno);
                intent.putExtra("styleno",styleno);
                startActivity(intent);
                finish();
            }
        });
        if(b!=null){
            sno=b.getString("sno");
            styleno=b.getString("styleno");
            tVStyleono.setText(styleno);
            (new GetOpReport()).execute(sno);
        }

        datavalues = new ArrayList<>();
        dataSets = new ArrayList<>();

       // LineDataSet lineDataSet = new LineDataSet(datavalues,"Data set 1");
      //  dataSets = new ArrayList<>();
    //    dataSets.add(lineDataSet);
    //    LineData lineData = new LineData(dataSets);
    //    lineChart.setData(lineData);
    //    lineChart.invalidate();

    }

    private ArrayList<Entry> datavalues(){

        ArrayList<Entry> datavalues = new ArrayList<Entry>();
        datavalues.add(new Entry(0,20));
        datavalues.add(new Entry(1,40));
        datavalues.add(new Entry(2,2));
        datavalues.add(new Entry(3,29));
        datavalues.add(new Entry(4,50));
        datavalues.add(new Entry(5,90));

        return datavalues;
    }

    class GetOpReport extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(LineChartReport.this);
            pdia.setMessage("Checking device details..");
            pdia.show();
            pdia.setCanceledOnTouchOutside(false);
        }
        protected String doInBackground(String... username) {
            String responsetring="";
            try
            {
                SoapObject request=new SoapObject(NAMESPACE,"GetOPReport");
                SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet=true;
                envelope.setOutputSoapObject(request);
                request.addProperty("sno",username[0]);
                HttpTransportSE AndroidHttpTransportSE=new HttpTransportSE(URL);
                try{
                    AndroidHttpTransportSE.call(NAMESPACE+"GetOPReport",envelope);
                }
                catch(IOException | XmlPullParserException e){
                    Helper.Msg( "Something Went Wrong..",0, LineChartReport.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring=response.toString();
                }
                catch(Exception e){
                    Helper.Msg( "Something Went Wrong..",0, LineChartReport.this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("[]")){

            }
            else {
                try {

                    String OrderStyle="";
                    long avgdif=0;
                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobj = jsonarray.getJSONObject(i);
                        Long DiffTime=jsonobj.getLong("DiffTime");
                        DiffTime=DiffTime/1000;
                        if(i<jsonarray.length()-1)
                            avgdif+=DiffTime;
                        String OperationId=jsonobj.getString("OperationId");
                        OrderStyle=jsonobj.getString("OrderStyle");

                        datavalues.add(new BarEntry(DiffTime, i));
                        lineDataSet= new LineDataSet(datavalues, OperationId);
                        dataSets.add(lineDataSet);

                    }

                    avgdif=avgdif/jsonarray.length();
                    Description description = new Description();
                    description.setText(OrderStyle);
                    LineData lineData = new LineData(dataSets);
                    lineChart.setData(lineData);
                    lineChart.invalidate();

                    long lowpitch= (long) Math.ceil(avgdif-(avgdif*(15.00/100.00)));
                    long highpitch= (long) Math.floor(avgdif+(avgdif*(15.00/100.00)));

                    LimitLine lower_limit = new LimitLine(lowpitch, "Low Pitch" +lowpitch);
                    lower_limit.setLineWidth(1f);
                    lower_limit.enableDashedLine(10f, 10f, 0f);
                    lower_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                    lower_limit.setTextSize(10f);
                    YAxis leftAxis = lineChart.getAxisLeft();
                    // reset all limit lines to avoid overlapping lines
                    leftAxis.removeAllLimitLines();
                    leftAxis.addLimitLine(lower_limit);

                    LimitLine high_limit = new LimitLine(highpitch, "High Pitch " +highpitch);
                    high_limit.setLineWidth(1f);
                    high_limit.enableDashedLine(10f, 10f, 0f);
                    high_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                    high_limit.setTextSize(10f);
                    high_limit.setLineColor(getResources().getColor(R.color.colorBlack));
                    leftAxis.addLimitLine(high_limit);
                    LimitLine avg_limit = new LimitLine(avgdif, "Avg Pitch " +avgdif);
                    avg_limit.setLineWidth(2f);
                    avg_limit.enableDashedLine(10f, 10f, 0f);
                    avg_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                    avg_limit.setTextSize(10f);
                    avg_limit.setLineColor(getResources().getColor(R.color.colorAccent));
                    leftAxis.addLimitLine(avg_limit);
                    leftAxis.enableGridDashedLine(10f, 10f, 0f);
                    leftAxis.setDrawZeroLine(false);
                    leftAxis.setDrawLimitLinesBehindData(true);
                    lineChart.getAxisRight().setEnabled(false);

                }
                catch (Exception er)
                {
                    Helper.InfoMsg("Info", "Data Parse Failed..", LineChartReport.this);
                }
            }
            pdia.dismiss();
        }
    }
}
