package com.dopingdetector.fragment;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dopingdetector.actions.CameraPreview;
import com.dopingdetector.main.MainActivity;
import com.dopingdetector.R;
import com.dopingdetector.dataaccess.DataAccess;
import com.google.zxing.Result;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Tab2Scan extends Fragment  implements ZXingScannerView.ResultHandler{

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private ImageScanner scanner;
    private FloatingActionButton fab;
    public static boolean barcodeScanned = false;
    public static boolean previewing = true;
    private DataAccess da = null;
    private SQLiteDatabase db= null;
    private String SP = "";
    private String Result;
    private String CodigoS;

    public static EditText editText;
    private ZXingScannerView mScannerView;

    static {
        System.loadLibrary("iconv");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2scan, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mScannerView = new ZXingScannerView(getActivity());
        FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.cameraPreview);
        preview.addView(mScannerView);
        return rootView;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(this.isVisible()) {
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
            if (!isVisibleToUser) {
                mScannerView.stopCamera();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


    public void handleResult(com.google.zxing.Result rawResult) {
        String code = rawResult.getText();
        ScanResult(code);
    }

    public void ScanResult(String message) {
        da = new DataAccess((MainActivity) this.getActivity());
        db = da.getWritableDatabase();
        CodigoS= message;
        String[] consulta = new String[]{message.toString()};
        if (message.equals("") || message.length() == 0 || message == null) {
            Toast.makeText(getActivity(),"El Código esta Vacío",
                    Toast.LENGTH_SHORT).show();
            mScannerView.resumeCameraPreview(this);
        } else {
            Cursor c = db.rawQuery("SELECT * FROM  Farmaco WHERE Code=?", consulta);
            Cursor d = db.rawQuery("SELECT * FROM  Sustancia WHERE Code=?", consulta);
            if (c.moveToFirst()) {
                do {
                    String Code = c.getString(0);
                    String Name = Character.toString(c.getString(1).charAt(0)).toUpperCase()+c.getString(1).substring(1);
                    String Description = c.getString(2);
                    Result = "Nombre del fármaco: " + Name + "\n"
                            + "Código del fármaco: " + Code + "\n"
                            + "Descripción del fármaco: " + Description + "\n"
                            + "Sustancias: " + "\n";
                }while (c.moveToNext());

                if (d.moveToFirst()) {
                    do {
                        String Name = d.getString(2);
                        String[] prohibida = new String[]{Name.toString()};
                        Cursor a = db.rawQuery("SELECT * FROM  SustanciaProhibida WHERE Name=?", prohibida);
                        if (a.moveToFirst()) {
                            do {
                                String NamePro = a.getString(1);
                                String Details = a.getString(2);

                                Result = Result +"<font color='red'>"+Character.toString(NamePro.charAt(0)).toUpperCase()+NamePro.substring(1)+"</font>";
                                SP = SP +"*" +Character.toString(NamePro.charAt(0)).toUpperCase()+NamePro.substring(1) + ": " + Details;
                                if (!d.isLast()) {SP = SP + "\n";
                                    Result = Result +", ";}
                                else{Result = Result + ".";}
                            }
                            while (a.moveToNext());
                        }else{
                            Result = Result +"<font color='green'>"+Character.toString(Name.charAt(0)).toUpperCase()+Name.substring(1)+"</font>";
                            if (!d.isLast()) {
                                Result = Result +", ";}
                            else{Result = Result + ".";}
                        }
                    }
                    while (d.moveToNext());
                    if(SP==""){
                        showAlertDialogDNS(Result);
                    }else{
                        showAlertDialog(Result);}
                }else{
                    Result = Result  + "No tiene Sustancias" + "\n";
                    showAlertDialogDNS(Result);
                }
            } else {
                Result="No Existe el Fármaco\n"+"Código: "+CodigoS;
                showAlertDialogS(Result);
            }
        }
    }
    private void showAlertDialog(String message) {
        String color2 = message.replace("\n", "<br />");
        AlertDialog A = new AlertDialog.Builder(getContext())
                .setTitle(Html.fromHtml("<font color='red'>Doping Detector</font>"))
                .setCancelable(false)
                .setMessage(Html.fromHtml(color2))
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mScannerView.resumeCameraPreview(Tab2Scan.this);
                        Result="";
                        SP="";
                    }
                })
                .setNeutralButton("Detalles", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showAlertDialogD(SP);
                    }
                }).show();
        A.getButton(A.BUTTON_NEUTRAL).setTextColor(Color.RED);
    }
    private void showAlertDialogD(String message) {

        String color2 = "<font color='red'>"+message+"</font>";
        color2 = color2.replace("\n", "<br />");
        new AlertDialog.Builder(getContext())
                .setTitle(Html.fromHtml("<font color='red'>Sustancia Prohibida</font>"))
                .setCancelable(false)
                .setMessage(Html.fromHtml(color2))
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showAlertDialog(Result);
                    }
                }).show();
    }
    private void showAlertDialogDNS(String message) {

        String color, color2;
        if(Result!=""){color= "<font color='green'>Doping Detector</font>";
            color2 = message;
        }
        else {color= "<font color='red'>Sustancia Prohibida</font>";
            color2 = "<font color='red'>"+message+"</font>";
        }
        color2 = color2.replace("\n", "<br />");
        new AlertDialog.Builder(getContext())
                .setTitle(Html.fromHtml(color))
                .setCancelable(false)
                .setMessage(Html.fromHtml(color2))
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mScannerView.resumeCameraPreview(Tab2Scan.this);
                        Result="";
                        SP="";
                    }
                }).show();
    }
    private  void showAlertDialogS(String message) {

        new AlertDialog.Builder(getContext())
                .setTitle(getResources().getString(R.string.app_name))
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Sugerir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editText = (EditText) getActivity().findViewById(R.id.editTextCode);
                        editText.setText(CodigoS);
                        mScannerView.resumeCameraPreview(Tab2Scan.this);
                        ViewPager mViewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        mViewPager.setCurrentItem(2);
                    }
                })
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mScannerView.resumeCameraPreview(Tab2Scan.this);
                        Result="";
                        SP="";
                    }
                }).show();
    }
}
