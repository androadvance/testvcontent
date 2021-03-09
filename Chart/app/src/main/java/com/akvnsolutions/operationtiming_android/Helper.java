package com.akvnsolutions.operationtiming_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Helper extends Application {

    private static final String NAMESPACE = "http://bazaarv2_login.org/";
    private static String URL;

    static void InfoMsg(String title, String msg, Activity act)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act) ;

        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(title);
        alertDialogBuilder.setIcon(R.drawable.icerrorred);
        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public static String WebServiceCall(String[] a, String[] b, String Method, String Namespace, String URL) {
        String responsestring="";
        try
        {
            SoapObject request = new SoapObject(Namespace, Method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            if(a!=null) {
                for (int i = 0; i <= a.length - 1; i++) {
                    request.addProperty(a[i], b[i]);
                }
            }
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            try {
                androidHttpTransport.call(Namespace + Method,
                        envelope);
            } catch (IOException | XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SoapPrimitive response = null;
            try {
                response = (SoapPrimitive) envelope.getResponse();
                responsestring = response.toString();
            } catch (SoapFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "false " + e.toString();
            }
            return responsestring;
        }
        catch(Exception e)
        {
            return "false " + e.toString();       //	return false;
        }
    }


    static void Msg(String string, int i, Context cntxt) {
        Toast.makeText(cntxt, string, i).show();
    }




    static void warning(String title, String msg, Activity cnt)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cnt) ;

        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(title);
        alertDialogBuilder.setIcon(R.drawable.warning);
        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public static void dialog_error(final Activity cntxt, String title, String message, String button) {
        new PromptDialog(cntxt)
                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                .setAnimationEnable(true)
                .setTitleText(title).setContentText(message)
                .setPositiveListener(button, new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
