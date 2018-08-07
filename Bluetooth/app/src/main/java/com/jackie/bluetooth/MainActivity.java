package com.jackie.bluetooth;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;



import android.Manifest;
import android.app.Activity;
import android.app.MediaRouteButton;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
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
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import java.util.Calendar;

import static android.R.*;
import static android.R.layout.*;

public class MainActivity extends AppCompatActivity {

    Button btnconect, btn1, btn2, btn3, save;
    private static TextView mostrarDados;
    EditText mEditText;
    Handler h;
    public static final int REQUEST_PERMISSIONS_CODE = 128;

    final int RECEIVE_MESSAGE = 1;
    private static final String FILE_NAME = "dados.csv";
    private TextView exibirData;
    private TextView exibirLocalizacao;
    private LocationManager mLocationManager;


    

    private StringBuilder sb = new StringBuilder();


    private static final int SOLICITA_BT_ACT = 1;
    private static final int SOLICITA_BT_CON = 2;
    private static final int LER_MSG_BT = 3;
    private static final int LST_MSG_BT = 4;
    ConnectedThread connectedThread;
    Handler mHandler;
    StringBuilder dadosBth = new StringBuilder();
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
        btn3 = (Button) findViewById(R.id.btn3);
        mostrarDados = (TextView) findViewById(R.id.mostrarDados);
        mEditText = findViewById(R.id.edit_text);
        save = (Button) findViewById(R.id.save);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Deu ruim papai, vc não tem bluetooth", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent ativaBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBT, SOLICITA_BT_ACT);
        }

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



        btnconect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (conexao) {
                    //desconectar
                    try {
                        mSocket.close();
                        conexao = false;
                        btnconect.setText("Conectar");


                        Toast.makeText(getApplicationContext(), "Acabou a palhaçada, desconectado.", Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Deu pau no vararis, erro: " + erro, Toast.LENGTH_LONG).show();
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
                String FILENAME = "data2.csv";
                String entrada = exibirData.getText().toString() + ", " +mEditText.getText().toString()+", " + exibirLocalizacao.getText().toString() + "\n";


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
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String FILENAME = "data2.csv";
                String entrada = exibirData.getText().toString() + ", " +mEditText.getText().toString()+", " + exibirLocalizacao.getText().toString() + "\n";


                PrintWriter csvWriter;
                try {
                    csvWriter.close();
                } catch (Exception e) { e.printStackTrace(); }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conexao) {
                    connectedThread.enviar("Reiniciar");
                } else {
                    Toast.makeText(getApplicationContext(), "A conexão não rolou papai", Toast.LENGTH_LONG).show();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String FILENAME = "data2.csv";
                String entrada = exibirData.getText().toString() + ", " +mEditText.getText().toString()+", " + exibirLocalizacao.getText().toString() + "\n";


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
        });



        //h.sendEmptyMessage(0);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECEIVE_MESSAGE: // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;

                        /*String readMessage = new String(readBuf, 0, msg.arg1);
                        //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                        mostrarDados.setText(readMessage);
                        break;*/
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);												// append string
                        int endOfLineIndex = sb.indexOf("\n");							// determine the end-of-line
                        if (endOfLineIndex > 0) { 											// if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);				// extract string
                            sb.delete(0, sb.length());
                            mostrarDados.setText("Data from Arduino: ");
                            // and clear
                            mEditText.setText(sbprint); 	        // update TextView

                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };
    };

    /*mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == LER_MSG_BT) {

                    String recebidos = (String) msg.obj;
                    Bundle params = new Bundle();
                    dadosBth.append(recebidos);

                    int fimInfo = dadosBth.indexOf("}");

                    if (fimInfo > 0) {
                        String dadoscompletos = dadosBth.substring(0, fimInfo);

                        int tamanhoInfo = dadoscompletos.length();

                        if (dadosBth.charAt(0) == '{') {

                            String dadosfinais = dadosBth.substring(1, tamanhoInfo);

                            Log.d("Recebidos: ", dadosfinais);
                            TextView log_dados = (TextView)findViewById(R.id.mostrarDados);
                            //logDados.setText(dadosfinais);
                            Toast.makeText(getApplicationContext(),"Mensagem = " + dadosfinais, Toast.LENGTH_LONG).show();
                            
                            //startActivityForResult(msgLista, LST_MSG_BT);




                        }

                        dadosBth.delete(0, dadosBth.length());
                    }
                }
            }
        };*/

    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        exibirLocalizacao.setText(longitude + ", " + latitude);
    }
    public void save(View v) {
        String text = mEditText.getText().toString();
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());

            mEditText.getText().clear();
            Toast.makeText(this, "Saved to " + getFilesDir() + "/storage/sdcard0/Download/" + FILE_NAME,
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void load(View v) {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            mEditText.setText(sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
                    Toast.makeText(getApplicationContext(),"Esse é o bixão mesmo! Vamo lá...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Deu ruim papai, sem bluetooth eu vou embora", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(),"Você é ráquizão memo, tamo online no arduino ", Toast.LENGTH_LONG).show();

                    } catch (IOException erro){

                        conexao = false;
                        Toast.makeText(getApplicationContext(),"Ih carai, deu ruim aqui na conexão, erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Deu ruim papai,MAC lixo não pegou", Toast.LENGTH_LONG).show();
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

    /*public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
            }
        }*/




}
