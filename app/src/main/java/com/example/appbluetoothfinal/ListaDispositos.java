package com.example.appbluetoothfinal;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Set;

public class ListaDispositos extends ListActivity {
    private BluetoothAdapter meuBluetoothAdapter2 = null;

    static String ENDERECO_MAC = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        meuBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();
        @SuppressLint("MissingPermission") Set<BluetoothDevice> dispositivosPareados = meuBluetoothAdapter2.getBondedDevices();
        if(dispositivosPareados.size() > 0){

            for(BluetoothDevice dispositivo : dispositivosPareados){

                @SuppressLint("MissingPermission") String nomeBt = dispositivo.getName();
                String macBt = dispositivo.getAddress();
                ArrayBluetooth.add(nomeBt + "\n" + macBt);
            }
        }
        setListAdapter(ArrayBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String informacaogeral = ((TextView)v).getText().toString();
        //Toast.makeText(getApplicationContext(), "Info: " + informacaogeral, Toast.LENGTH_LONG).show();
        String enderecoMac = informacaogeral.substring(informacaogeral.length() - 17);
        //Toast.makeText(getApplicationContext(), "mac: " + enderecoMac, Toast.LENGTH_LONG).show();
        Intent retornaMac = new Intent();
        retornaMac.putExtra(ENDERECO_MAC, enderecoMac);
        setResult(RESULT_OK,retornaMac);
        finish();
    }
}
