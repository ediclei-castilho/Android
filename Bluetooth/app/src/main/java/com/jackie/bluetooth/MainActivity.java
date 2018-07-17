package com.jackie.bluetooth;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    Button btnconect, btn1, btn2, btn3;
    //ListView mensagem;
    TextView mostrarDados;
    //EditText et__name;
    //private static EditText logDados;
    Handler h;
    //static TextView statusMessage;
    final int RECEIVE_MESSAGE = 1;

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
        //et__name = (EditText) findViewById(R.id.et_name);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Deu ruim papai, vc não tem bluetooth", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent ativaBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBT, SOLICITA_BT_ACT);
        }


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
                if (conexao) {
                    connectedThread.enviar("Iniciar");
                } else {
                    Toast.makeText(getApplicationContext(), "A conexão não rolou papai", Toast.LENGTH_LONG).show();
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (conexao) {
                    connectedThread.enviar("Encerrar");
                } else {
                    Toast.makeText(getApplicationContext(), "A conexão não rolou papai", Toast.LENGTH_LONG).show();
                }


            }
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


        //h.sendEmptyMessage(0);



    /*private void saveTextAsFile(String filename, String content) {
        String fileName = filename + ".csv";

        //create file

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);

        //write to file

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this,"Saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,"File not found!", Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
             e.printStackTrace();
             Toast.makeText(this,"Error Saving!", Toast.LENGTH_SHORT).show();
        }


    }*/
    /*
    public void OnRequestPermissionsResult(int requestCode, @NonNull String [] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }*/

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECEIVE_MESSAGE: // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1); // create
                        // string
                        // from
                        // bytes array
                        mostrarDados.setText("Data from Arduino: " + strIncom); // update TextView
                        break;
                }
            }
        };
    }/*mHandler = new Handler() {
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
                            //Intent msgLista = new Intent(MainActivity.this,ListaDados.class);
                            //startActivityForResult(msgLista, LST_MSG_BT);




                        }

                        dadosBth.delete(0, dadosBth.length());
                    }
                }
            }
        };*/



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

            //TextView newramon = (TextView) findViewById(R.id.logDados);
            //newramon.setText("dadosBtorrada");
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer); // Get number of bytes and
                    // message in "buffer"
                    h.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget(); // Send
                    // to
                    // message
                    // queue
                    // Handler
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
