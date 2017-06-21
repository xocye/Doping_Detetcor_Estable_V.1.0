package com.dopingdetector.main;

import android.app.ActionBar;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.dopingdetector.R;
import com.dopingdetector.dataaccess.DataAccess;
import com.dopingdetector.fragment.Tab1Shear;
import com.dopingdetector.fragment.Tab2Scan;
import com.dopingdetector.fragment.Tab3Form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.dopingdetector.R.id.container;

/**
 * Doping Detector VERSION  ESTABLE
 */

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private Button btn1, btn2;
    private SQLiteDatabase db;
    private DataAccess da;
    private Tab1Shear tab1;
    private Tab2Scan tab2;
    private Tab3Form tab3;
    private FloatingActionButton fab;
    private InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);// Para iniciar en la prosiÃ³n 1
        mViewPager.callOnClick();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //llamado
        da = new DataAccess(this);
        tab1 = new Tab1Shear();
        tab2 = new Tab2Scan();
        tab3 = new Tab3Form();
        ;
        // base  de   datos
        try {
            base();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // base  de   datos
        db = da.getReadableDatabase();
        // Botones
        btn1 = (Button) findViewById(R.id.btnBusqueda);
        btn2 = (Button) findViewById(R.id.btnEnviar);

    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btnAcerca) {
            Toast.makeText(this, "Doping Detector Version 1.0",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case (R.id.btnBusqueda):
                tab1.Result();
                //tab1.Vista();
                break;

            case (R.id.btnEnviar):
                tab3.EnviarCorreo();

                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            switch (position) {
                case 0:
                    return tab1;

                case 1:

                    return tab2;
                case 2:

                    return tab3;
                default:
                    return null;
            }

        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Busqueda";
                case 1:
                    return "Escaner";

                case 2:
                    return "Sugerencia";
            }
            return null;
        }
    }

    // Cargar  Base de  datos

    private void base() throws IOException {

        //Open your local db as the input stream
        String packageName = getApplicationContext().getPackageName();
        String DB_PATH = "/data/data/" + packageName + "/databases/";
        //Create the directory if it does not exist
        File directory = new File(DB_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String DB_NAME = "DD.db"; //The name of the source sqlite file

        InputStream myInput = getAssets().open("DD.db");

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }


}////class
