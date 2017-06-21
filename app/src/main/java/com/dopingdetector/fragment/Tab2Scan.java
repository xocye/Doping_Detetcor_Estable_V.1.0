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

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class Tab2Scan extends Fragment {

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

    static {
        System.loadLibrary("iconv");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2scan, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        // Instance barcode scanner
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(rootView.getContext(), mCamera, previewCb,
                autoFocusCB);
        FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        return rootView;
    }
@Override
public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
       if(this.isVisible()) {
           barcodeScanned = false;
           mCamera.setPreviewCallback(previewCb);
           mCamera.startPreview();
           previewing = true;
           if (!isVisibleToUser) {
               previewing = false;
               mCamera.setPreviewCallback(null);
               mCamera.stopPreview();
               barcodeScanned = true;
           }
       }
}
    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {

                    Log.i("<<<<<<Asset Code>>>>> ",
                            "<<<<Bar Code>>> " + sym.getData());
                    String scanResult = sym.getData().trim();
                    barcodeScanned = true;
                    CodigoS=scanResult;
                    ScanResult(scanResult);

                  /*  Toast.makeText(BarcodeScanner.this, scanResult,
                            Toast.LENGTH_SHORT).show();*/



                    break;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    public void ScanResult(String message) {

        da = new DataAccess((MainActivity) this.getActivity());
        db = da.getWritableDatabase();


        String[] consulta = new String[]{message.toString()};
        if (message.equals("") || message.length() == 0 || message == null) {
            Toast.makeText(getActivity(),"El Código esta Vacío",
                    Toast.LENGTH_SHORT).show();
            if (barcodeScanned) {
                barcodeScanned = false;
                mCamera.setPreviewCallback(previewCb);
                mCamera.startPreview();
                previewing = true;
            }
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
                        if (barcodeScanned) {
                            barcodeScanned = false;
                            mCamera.setPreviewCallback(previewCb);
                            mCamera.startPreview();
                            previewing = true;
                        }
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
                .setTitle(Html.fromHtml("<font color='red'>Sustancía Prohibida</font>"))
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
        else {color= "<font color='red'>Sustancía Prohibida</font>";
            color2 = "<font color='red'>"+message+"</font>";
        }
        color2 = color2.replace("\n", "<br />");
        new AlertDialog.Builder(getContext())

                .setTitle(Html.fromHtml(color))
                .setCancelable(false)
                .setMessage(Html.fromHtml(color2))
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (barcodeScanned) {
                            barcodeScanned = false;
                            mCamera.setPreviewCallback(previewCb);
                            mCamera.startPreview();
                            previewing = true;
                        }
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
                        if (barcodeScanned) {
                            barcodeScanned = false;
                            mCamera.setPreviewCallback(previewCb);
                            mCamera.startPreview();
                           previewing = true;
                        }
                        ViewPager mViewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        mViewPager.setCurrentItem(2);

                    }
                })
        .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (barcodeScanned) {
                    barcodeScanned = false;
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                }
                Result="";
                SP="";
            }
        }).show();

    }
}
