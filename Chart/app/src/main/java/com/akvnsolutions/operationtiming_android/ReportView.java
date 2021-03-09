package com.akvnsolutions.operationtiming_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class ReportView extends AppCompatActivity {

    TextView tVStyleono;
    ImageButton imgPitchChart, imgBarChart, imgMail, imggrid;
    String sno, styleno;
    WebView webview;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);

        Bundle bundle = getIntent().getExtras();
        webview = (WebView) findViewById(R.id.wVReport);
        webview.getSettings().setJavaScriptEnabled(true);
        tVStyleono = findViewById(R.id.tVStyleNo);
        imgPitchChart = findViewById(R.id.imgPitchChart);
        imgBarChart = findViewById(R.id.imgBarChart);
      //  imgMail = findViewById(R.id.imgMail);

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

       /* imgMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(new sendmail()).execute(sno);
                Helper.Msg("Under Developement", 0, ReportView.this);
            }
        });*/

        imggrid = findViewById(R.id.imgGrid);
        imggrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportView.this, ReportView.class);
                intent.putExtra("sno", sno);
                intent.putExtra("styleno", styleno);
                startActivity(intent);
                finish();
            }
        });

        imgBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportView.this, GraphReport.class);
                intent.putExtra("sno", sno);
                intent.putExtra("styleno", styleno);
                startActivity(intent);
                finish();
            }
        });

        imgPitchChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportView.this, LineChartReport.class);
                intent.putExtra("sno", sno);
                intent.putExtra("styleno", styleno);
                startActivity(intent);
                finish();
            }
        });

        if (bundle != null) {
            sno = bundle.getString("sno");
            styleno = bundle.getString("styleno");
            tVStyleono.setText(styleno);
            webview.loadUrl("http://182.76.164.193/opTiming/OpTimingReport.aspx?Sno=" + sno);
        }
    }

}
