package com.example.appbluetoothfinal;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class ListaDispositos extends ListActivity {
    public static final String ENDERECO_MAC = "mac_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AppState.getInstance().checkBluetoothConnectPermission(this)) {
            Toast.makeText(this, "Permissão necessária", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Set<BluetoothDevice> devices = AppState.getInstance().getPairedDevices(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        if (devices != null && !devices.isEmpty()) {
            for (BluetoothDevice device : devices) {
                adapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            adapter.add("Nenhum dispositivo pareado");
        }
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String deviceInfo = ((TextView) v).getText().toString();
        String macAddress = deviceInfo.substring(deviceInfo.length() - 17);

        Intent result = new Intent();
        result.putExtra(ENDERECO_MAC, macAddress);
        setResult(RESULT_OK, result);
        finish();
    }
}