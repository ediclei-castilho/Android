package com.datacsv.datacsv;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main extends AppCompatActivity implements LocationListener {

    private TextView exibirData;
    private TextView exibirLocalizacao;
    private LocationManager mLocationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDateFormat data_formatada = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss, ");

        Date data = new Date();

        Calendar calendario = Calendar.getInstance();
        calendario.setTime(data);

        Date data_atual = calendario.getTime();
        String dataFormatada = data_formatada.format(data_atual);


        exibirData = (TextView) findViewById(R.id.vData);
        exibirData.setText(dataFormatada);

        exibirLocalizacao = (TextView) findViewById(R.id.vLocalizacao);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        onLocationChanged(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        exibirLocalizacao.setText("Longitude: " + longitude + "\nLatitude: " + latitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void saveLogOnClick(View view){

        String FILENAME = "data2.csv";
        String entrada = exibirData.getText().toString() + "," + exibirLocalizacao.getText().toString() + "\n";


        PrintWriter csvWriter;
        try {
            StringBuffer oneLineStringBuffer = new StringBuffer();
            File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
            if (!file.exists()) {
                file = new File(Environment.getExternalStorageDirectory(), FILENAME);
            }
            csvWriter = new PrintWriter(new FileWriter(file, true));

            oneLineStringBuffer.append(entrada);
            csvWriter.print(oneLineStringBuffer);
            csvWriter.close();
        } catch (Exception e) { e.printStackTrace(); }

    }
}
