package com.dopingdetector.fragment;


import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.dopingdetector.R;
import com.dopingdetector.dataaccess.DataAccess;
import com.dopingdetector.main.MainActivity;

public class Tab1Shear extends Fragment{//class
    private DataAccess da = null;
    private SQLiteDatabase db= null;
    private EditText et1;
    private String Result="";
    private String SP="";


    public static String CodigoS="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1shear, container, false);
        return rootView;
    }


    public void Result() {

        da = new DataAccess((MainActivity) this.getActivity());
        db = da.getWritableDatabase();
        String codigo="";

        et1 = (EditText) getActivity().findViewById(R.id.editTextCodeB);
        String[] consulta = new String[]{et1.getText().toString().toLowerCase()};
        CodigoS = et1.getText().toString();
        if (consulta.equals("") || consulta[0].length() == 0 || consulta == null) {
            Toast.makeText(getActivity(),"El Campo esta Vacío",
                    Toast.LENGTH_SHORT).show();
        } else {

            Cursor c = db.rawQuery("SELECT * FROM  Farmaco WHERE Code=? OR Name=?", new String[]{consulta[0],consulta[0]});

            if (c.moveToFirst()) {
                do {
                    String Code = c.getString(0);
                    String Name = Character.toString(c.getString(1).charAt(0)).toUpperCase()+c.getString(1).substring(1);

                    String Description = c.getString(2);
                    Result = "Nombre del fármaco: " + Name + "\n"
                            + "Código del fármaco: " + Code + "\n"
                            + "Descripción del fármaco: " + Description + "\n"
                            + "Sustancias: ";
                    codigo= Code;
                } while (c.moveToNext());

                Cursor d = db.rawQuery("SELECT * FROM  Sustancia WHERE Code=?",new String[]{codigo});
                if (d.moveToFirst()) {
                    do {
                        String Name = d.getString(2);
                        String[] prohibida = new String[]{Name.toString().toLowerCase()};

                        Cursor a = db.rawQuery("SELECT * FROM  SustanciaProhibida WHERE Name=?",prohibida);
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
                        } else {
                            Result = Result +"<font color='green'>"+Character.toString(Name.charAt(0)).toUpperCase()+Name.substring(1)+"</font>";
                            if (!d.isLast()) {
                                Result = Result +", ";}
                            else{Result = Result + ".";}
                        }

                    }
                    while (d.moveToNext());
                    if (SP == "") {
                        showAlertDialogDNS(Result);
                    } else {
                        showAlertDialog(Result);
                    }
                } else {
                    Result = Result + "No tiene Sustancias" + "\n";

                    showAlertDialogDNS(Result);
                }
            } else {

                Cursor r = db.rawQuery("SELECT * FROM  SustanciaProhibida WHERE Name=?", consulta);
                if (r.moveToFirst()) {
                    do {
                        String NamePro = r.getString(1);
                        String Details = r.getString(2);
                        SP = SP +"*" + NamePro + ": " + Details;
                    }
                    while (r.moveToNext());

                    showAlertDialogDNS(SP);
                } else {

                    Result = "No Existe el Fármaco o Sustancía Prohibida en la Base de Datos: \n" + et1.getText().toString();
                    showAlertDialogS(Result);


                }

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

                        Result="";
                        SP="";
                       clear();
                    }
                })

                .setNeutralButton("Detalles", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        showAlertDialogD(SP);
                    }
                })
                .show();
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
                        Result="";
                        SP="";
                       clear();
                    }
                }).show();

    }
    private void showAlertDialogS(String message) {

        new AlertDialog.Builder(getContext())
                .setTitle("No Existe")
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Sugerir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ViewPager mViewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        mViewPager.setCurrentItem(2);
                       clear();

                    }
                })
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Result = "";
                        SP = "";
                        clear();
                    }
                }).show();


    }


    public void clear(){
        et1.setText("");
    }
}//class
