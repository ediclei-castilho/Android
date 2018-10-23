package com.jackie.bluetooth;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    Button btnconect, btn1, btn2, save;
    private static TextView mostrarDados;
    EditText mEditText;
    Handler h;
    public static final int REQUEST_PERMISSIONS_CODE = 128;

    final int RECEIVE_MESSAGE = 1;
    private TextView exibirData;
    private TextView exibirLocalizacao;
    private LocationManager mLocationManager;


    

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

        btnconect = (Button) findViewById(R.id.btnconect);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        mostrarDados = (TextView) findViewById(R.id.mostrarDados);
        mEditText = findViewById(R.id.edit_text);
        save = (Button) findViewById(R.id.save);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Adaptador Bluetooth não encontrado!", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent ativaBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBT, SOLICITA_BT_ACT);
        }

        SimpleDateFormat data_formatada = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

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



        btnconect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (conexao) {
                    //desconectar
                    try {
                        mSocket.close();
                        conexao = false;
                        btnconect.setText("Conectar");


                        Toast.makeText(getApplicationContext(), "Sensor desconectado.", Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                } else {
                    //conectar
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_BT_CON);
                }

            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper = 1;

            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper = 0;
            }
        });



        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
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
                            mostrarDados.setText("Data from Arduino: ");
                            // and clear
                            mEditText.setText(sbprint); 	        // update TextView
                            String FILENAME = "Download/LogSensores.csv";
                            String entrada = exibirData.getText().toString() + "," + sbprint +"," + exibirLocalizacao.getText().toString() + "\n";

                            GraphView graph = (GraphView) findViewById(R.id.graph);
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                                    new DataPoint(0, 1),
                                    new DataPoint(1, 5),
                                    new DataPoint(2, 3),
                                    new DataPoint(3, 2),
                                    new DataPoint(4, 6)
                            });
                            graph.addSeries(series);

                            PrintWriter csvWriter;
                            if( helper == 1){
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
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };
    };


    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        exibirLocalizacao.setText(longitude + "," + latitude);
    }
  
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //Log.i(TAG, "test");
        switch( requestCode ){
            case REQUEST_PERMISSIONS_CODE:
                for( int i = 0; i < permissions.length; i++ ){

                    if( permissions[i].equalsIgnoreCase( Manifest.permission.ACCESS_FINE_LOCATION )
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){

                        //readMyCurrentCoordinates();
                    }
                    else if( permissions[i].equalsIgnoreCase( Manifest.permission.WRITE_EXTERNAL_STORAGE )
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){

                        //createDeleteFolder();
                    }
                    else if( permissions[i].equalsIgnoreCase( Manifest.permission.READ_EXTERNAL_STORAGE )
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){

                        //readFile(Environment.getExternalStorageDirectory().toString() + "/myFolder");
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case SOLICITA_BT_ACT:
                if(resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),"Bluetooth ativado com sucesso.", Toast.LENGTH_LONG).show();
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

                        btnconect.setText("Desconectar");
                        Toast.makeText(getApplicationContext(),"Conexão efetuada com sucesso.", Toast.LENGTH_LONG).show();

                    } catch (IOException erro){

                        conexao = false;
                        Toast.makeText(getApplicationContext(),"Falha durante a conexão, erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Erro ao obter o Endereço MAC.", Toast.LENGTH_LONG).show();
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
                    bytes = mmInStream.read(buffer); // Get number of bytes and
                    // message in "buffer"
                    h.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget(); // Send
                    // to message queue // Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void enviar (String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }


    }

    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String sourceFileUri = "storage/sdcard0/Download/LogSensores.csv";

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = "http://website.com/abc.php?";

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);

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
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"bill\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

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

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);

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