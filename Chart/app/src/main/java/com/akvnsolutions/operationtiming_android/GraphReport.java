package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

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


public class GraphReport extends AppCompatActivity {

    private static final String NAMESPACE = "http://com.akvnitsolution.operationtiming/";
    private static String URL = "http://182.76.164.193/opTiming/TimingSVC.asmx";
    TextView tVStyleono;
    ImageButton imgPitchChart,imgBarChart,imggrid;
    String sno,styleno;
    BarChart barchart;
    ArrayList<BarEntry> barEntries;
    ArrayList<String> labelnames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_report);

        Bundle b=getIntent().getExtras();
        barchart = findViewById(R.id.barchart);
        tVStyleono=findViewById(R.id.tVStyleNo);
        imgPitchChart=findViewById(R.id.imgPitchChart);
        imgBarChart=findViewById(R.id.imgBarChart);

        imggrid=findViewById(R.id.imgGrid);
        imggrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphReport.this, ReportView.class);
                intent.putExtra("sno",sno);
                intent.putExtra("styleno",styleno);
                startActivity(intent);
                finish();
            }
        });
        imgBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphReport.this, GraphReport.class);
                intent.putExtra("sno",sno);
                intent.putExtra("styleno",styleno);
                startActivity(intent);
                finish();
            }
        });

        imgPitchChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphReport.this, LineChartReport.class);
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


        barEntries = new ArrayList<>();
        labelnames = new ArrayList<>();

    }


    class GetOpReport extends AsyncTask<String, String, String> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(GraphReport.this);
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
                    Helper.Msg( "Something Went Wrong..",0, GraphReport.this);
                }
                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring=response.toString();
                }
                catch(Exception e){
                    Helper.Msg( "Something Went Wrong..",0, GraphReport.this);
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
                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobj = jsonarray.getJSONObject(i);
                        Long DiffTime=jsonobj.getLong("DiffTime");
                        DiffTime=DiffTime/1000;
                        String OperationId=jsonobj.getString("OperationId");
                        OrderStyle=jsonobj.getString("OrderStyle");

                        barEntries.add(new BarEntry(i,DiffTime));
                        labelnames.add(OperationId);

                        BarDataSet barDataSet = new BarDataSet(barEntries,"Operation Timing");
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                        Description description = new Description();
                        description.setText(OrderStyle);
                        barchart.setDescription(description);
                        BarData barData = new BarData(barDataSet);
                        barchart.setData(barData);

                        XAxis xAxis = barchart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelnames));

                        xAxis.setPosition(XAxis.XAxisPosition.TOP);
                        xAxis.setDrawGridLines(false);
                        xAxis.setDrawAxisLine(false);
                        xAxis.setGranularity(1f);
                        xAxis.setLabelCount(labelnames.size());
                        xAxis.setLabelRotationAngle(270);
                        barchart.animateY(2000);
                        barchart.invalidate();

                    }

                }
                catch (Exception er)
                {
                    Helper.InfoMsg("Info", "Data Parse Failed..", GraphReport.this);
                }
            }
            pdia.dismiss();
        }
    }
}
