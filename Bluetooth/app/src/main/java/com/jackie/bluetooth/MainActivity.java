package com.jackie.bluetooth;
import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.util.UniversalTimeScale;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.maps.LocationSource;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import java.util.Calendar;

import de.nitri.gauge.Gauge;
import com.spark.submitbutton.SubmitButton;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "LOG";
    public static final int REQUEST_PERMISSIONS_CODE = 128;
    private MaterialDialog mMaterialDialog;

    Button btn1, btn2, btnExibir;
    FloatingActionButton save, btnconect, btntrash;
    //SubmitButton save;
    private EditText mostrarDados;
    private EditText mServerAddress;

    String header = "created_at,sensor,temperature,humidity,co,co2,mp25\n";
    Handler h;

    final int RECEIVE_MESSAGE = 1;
    private TextView exibirData;
    private TextView exibirLocalizacao;
    private TextView mEditText;
    private LocationManager mLocationManager;

    private Gauge mGaugeTemperature;
    private Gauge mGaugeHumidity;
    private Gauge mCoGraph;
    private Gauge mGauge4;
    private Gauge mGauge5;




    

    private StringBuilder sb = new StringBuilder();


    private static final int SOLICITA_BT_ACT = 1;
    private static final int SOLICITA_BT_CON = 2;
    private static int helper = 0;
    ConnectedThread connectedThread;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mDevice = null;
    BluetoothSocket mSocket = null;
    boolean conexao = false;
    private static String MAC = null;
    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    Date currentTime;

    {
        currentTime = Calendar.getInstance().getTime();
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnconect = (FloatingActionButton) findViewById(R.id.btnconect);
        btntrash = (FloatingActionButton) findViewById(R.id.btntrash);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        mostrarDados = findViewById(R.id.mostrarDados);
        exibirData = findViewById(R.id.vData);
        mEditText = findViewById(R.id.edit_text);
        save = (FloatingActionButton) findViewById(R.id.save);
        btnExibir =  findViewById(R.id.btnExibir);
        //mGaugeTemperature = findViewById(R.id.gauge);
        //mGaugeHumidity = findViewById(R.id.gauge2);
        //mCoGraph = findViewById(R.id.gauge3);
        //mGauge4 = findViewById(R.id.gauge4);
        //mGauge5 = findViewById(R.id.gauge5);
        mServerAddress = findViewById(R.id.serveraddress);
        btn1.setEnabled(false);
        btn2.setEnabled(false);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //PERMISSAO BLUETOOTH
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Adaptador Bluetooth não encontrado!", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent ativaBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBT, SOLICITA_BT_ACT);
        }
        //PERMISSAO PARA LER E ESCREVER
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) ){
                callDialog( "É necessário o acesso WRITE_EXTERNAL_STORAGE para geração do LOG csv.", new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} );
            }
            else{
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE );
            }
        }


        SimpleDateFormat data_formatada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        Date data = new Date();

        Calendar calendario = Calendar.getInstance();
        calendario.setTime(data);

        Date data_atual = calendario.getTime();
        String dataFormatada = data_formatada.format(data_atual);


        exibirData = (TextView) findViewById(R.id.vData);
        exibirData.setText(dataFormatada);

        //exibirLocalizacao = (TextView) findViewById(R.id.vLocalizacao);
        //mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.ACCESS_FINE_LOCATION ) ){
                callDialog( "É preciso a permission ACCESS_FINE_LOCATION para apresentação dos eventos locais.", new String[]{Manifest.permission.ACCESS_FINE_LOCATION} );
            }
            else{
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE );
            }
        }
        else{
            //final Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //onLocationChanged(location);
        }


        btnconect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (conexao) {
                    //desconectar
                    try {
                        mSocket.close();
                        conexao = false;
                        //btnconect.setText("Conectar");
                        //tnconect.setBackgroundResource(R.color.Green);
                        btn1.setEnabled(false);
                        btn2.setEnabled(false);
                        save.setEnabled(false);
                        mostrarDados.setEnabled(true);
                        mEditText.setText("");
                        Snackbar.make(findViewById(R.id.action_main), "Disconnected sensor", Snackbar.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), "Sensor desconectado.", Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        //Toast.makeText(getApplicationContext(), "Erro: " + erro, Toast.LENGTH_LONG).show();
                        Snackbar.make(findViewById(R.id.action_main), "Error"+ erro, Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    //conectar
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_BT_CON);
                }
            }
        });


        btnExibir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent grafico = new Intent (MainActivity.this, GraficosActivity.class);

            }
        });

        btntrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File sourceFileUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                if (sourceFileUri.exists()) {
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), "LogSensores.csv").delete();
                    String FILENAME = "LogSensores.csv";
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILENAME);
                    if (!file.exists()) {
                        Snackbar.make(findViewById(R.id.action_main), "Folder deleted", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(R.id.action_main), "Folder delete action was FAIL, take some permissions!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper = 1;
                btn1.setEnabled(false);
                mostrarDados.setEnabled(false);
                btnconect.setEnabled(false);
                btn2.setEnabled(true);

            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper = 0;
                btn1.setEnabled(true);
                mServerAddress.setEnabled(true);
                btn2.setEnabled(false);
                btnconect.setEnabled(true);
                save.setEnabled(true);
                mostrarDados.setEnabled(true);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
                                    public static final String TAG = "onclick";

                                    @Override
                                    public void onClick(View v) {
                                        new UploadFileAsync().execute("");
                                    }
                                });



        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECEIVE_MESSAGE: // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;

                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);												// append string
                        int endOfLineIndex = sb.indexOf("\n");							// determine the end-of-line
                        if (endOfLineIndex > 0) { 											// if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);				// extract string
                            sb.delete(0, sb.length());
                            // and clear
                            mEditText.setText(sbprint); 	        // update TextView
                            String[] attributes = sbprint.split(",");

                            String FILENAME = "Download/LogSensores.csv";
                            String entrada =  exibirData.getText().toString() + "," + mostrarDados.getText().toString() + "," + sbprint + "\n";
                            //String entrada =  mostrarDados.getText().toString() + ", " + sbprint + "\n";

                            for (int i =0; i < 5; i++){
                            //mGaugeTemperature.moveToValue(Float.parseFloat(attributes[i]));
                            //mGaugeHumidity.moveToValue(Float.parseFloat(attributes[i]));
                            //mCoGraph.moveToValue(Float.parseFloat(attributes[i]));
                            //mGauge4.moveToValue(Float.parseFloat(attributes[i]));
                            //mGauge5.moveToValue(Float.parseFloat(attributes[i]));
                            }

                            PrintWriter csvWriter;
                            if( helper == 1){
                                try {
                                    StringBuffer oneLineStringBuffer = new StringBuffer();
                                    File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
                                    if (!file.exists()) {
                                        file = new File(Environment.getExternalStorageDirectory(), FILENAME);
                                        csvWriter = new PrintWriter(new FileWriter(file, true));
                                        csvWriter.print(header);
                                        csvWriter.close();
                                    }
                                    csvWriter = new PrintWriter(new FileWriter(file, true));
                                    oneLineStringBuffer.append(entrada);
                                    csvWriter.print(oneLineStringBuffer);
                                    csvWriter.close();
                            } catch (Exception e) { e.printStackTrace(); }
                            }

                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };
    };


    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        exibirLocalizacao.setText(longitude + " / " + latitude);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case SOLICITA_BT_ACT:
                if(resultCode == Activity.RESULT_OK) {
                        Snackbar.make(findViewById(R.id.action_main), "Bluetooth enabled successfully", Snackbar.LENGTH_SHORT).show();

                    //Toast.makeText(getApplicationContext(),"Bluetooth ativado com sucesso.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Ativação do Bluetooth necessária para a execução da aplicação!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_BT_CON:
                if (resultCode == Activity.RESULT_OK){

                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
                    Toast.makeText(getApplicationContext(),"MAC: " + MAC, Toast.LENGTH_LONG).show();
                    mDevice = mBluetoothAdapter.getRemoteDevice(MAC);

                    try {
                        mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
                        mSocket.connect();

                        conexao = true;

                        connectedThread = new ConnectedThread(mSocket);
                        connectedThread.start();
                        //mostrarDados.setEnabled(false);

                        //btnconect.setText("Desconectar");
                        btn1.setEnabled(true);
                        btnconect.setBackgroundResource(R.color.Red);
                        Snackbar.make(findViewById(R.id.action_main), "Connection successful", Snackbar.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),"Conexão efetuada com sucesso.", Toast.LENGTH_LONG).show();

                    } catch (IOException erro){

                        conexao = false;
                        //Toast.makeText(getApplicationContext(),"Falha durante a conexão, erro: " + erro, Toast.LENGTH_LONG).show();
                        Snackbar.make(findViewById(R.id.action_main), "Failed to connect, error:"+ erro, Snackbar.LENGTH_SHORT).show();
                        mostrarDados.setEnabled(true);
                    }
                }else{
                    //Toast.makeText(getApplicationContext(),"Erro ao obter o Endereço MAC.", Toast.LENGTH_LONG).show();
                    Snackbar.make(findViewById(R.id.action_main), "Error getting MAC address", Snackbar.LENGTH_SHORT).show();

                }



        }
    }
    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        h.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }
    }
    private void callDialog( String message, final String[] permissions ){
        mMaterialDialog = new MaterialDialog(this)
                .setTitle("Permission")
                .setMessage( message )
                .setPositiveButton("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_CODE);
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
        mMaterialDialog.show();
    }

    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //save.setText("AGUARDE...");


            try {
                File sourceFileUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String sourceFileUripath = sourceFileUri + "/LogSensores.csv";

                Log.d("teste:", sourceFileUripath);
                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUripath);

                if (sourceFile.isFile()) {
                    try {
                        mServerAddress.setEnabled(false);
                        String upLoadServerUri = mServerAddress.getText().toString();
                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL("http://192.168.0.4:8000/api/csv/csv_upload/"); //upLoadServerUri

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "text/csv" );
                        //conn.setRequestProperty("bill", sourceFileUripath);
                        Log.d("boundary:", boundary);

                        dos = new DataOutputStream(conn.getOutputStream());

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        if (serverResponseCode == 200) {
                            View view = findViewById(R.id.action_main);
                            Snackbar.make(view, "Sent with success", Snackbar.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(),"Enviado com sucesso.", Toast.LENGTH_LONG).show();
                            if (sourceFileUri.exists()) {
                                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), "LogSensores.csv").delete();
                                String FILENAME = "LogSensores.csv";
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILENAME);
                                if (!file.exists()) {
                                    Log.i(TAG, "Folder DELETED!");
                                } else {
                                    Log.i(TAG, "Folder delete action was FAIL, take some permissions!");
                                }
                            }


                        }
                        else{
                            Snackbar.make(findViewById(R.id.action_main), "Error to send", Snackbar.LENGTH_SHORT).show();
                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block
                else{
                    View view = findViewById(R.id.action_main);
                    Snackbar.make(view, "Empty file", Snackbar.LENGTH_SHORT).show();

                }

                mServerAddress.setEnabled(true);
            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}