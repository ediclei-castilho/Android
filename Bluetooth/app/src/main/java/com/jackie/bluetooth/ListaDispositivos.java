package com.jackie.bluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ListaDispositivos extends ListActivity{

    private BluetoothAdapter mBluetoothAdapter2 = null;

    static String ENDERECO_MAC = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosPareados = mBluetoothAdapter2.getBondedDevices();

        if (dispositivosPareados.size() > 0){
            for (BluetoothDevice dispositivo : dispositivosPareados) {
                String nomeBT = dispositivo.getName();
                String mcBT = dispositivo.getAddress();
                ArrayBluetooth.add(nomeBT + "/n" + mcBT);
            }
        }

        setListAdapter(ArrayBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String informacaoGeral = ((TextView) v).getText().toString();

        //Toast.makeText(getApplicationContext(), "Info: " + informacaoGeral, Toast.LENGTH_LONG).show();

        String enderecoMac = informacaoGeral.substring(informacaoGeral.length() - 17);
        //Toast.makeText(getApplicationContext(), "MAC: " + enderecoMac, Toast.LENGTH_LONG).show();

        Intent retornaMc = new Intent();
        retornaMc.putExtra(ENDERECO_MAC, enderecoMac);
        setResult(RESULT_OK, retornaMc);
        finish();

    }
}