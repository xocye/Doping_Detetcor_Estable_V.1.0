package com.dopingdetector.dataaccess;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dopingdetector.main.MainActivity;

public class DataAccess extends SQLiteOpenHelper{

        private static final  String dbname = "DD.db";

    public DataAccess(MainActivity context) {

        super(context, dbname, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
     // si no existe la  base de datos la crea  y  ejecuta los sigeunetes comandos


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // se  elimina la  versión anterior  de la tabala
      db.execSQL("DROP TABLE IF EXISTS Farmaco");

        // se  crea la nueva version de la tabla

    }
}