package com.example.appbluetoothfinal;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity2 extends AppCompatActivity {
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;

    Button button, button2, button3, buttonConexao;
    BluetoothAdapter meuBluetoothAdapter = null;

    // Nova API para tratar resultado da ativação do Bluetooth
    private final ActivityResultLauncher<Intent> bluetoothResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "O Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth não foi ativado, o app será fechado", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonConexao = findViewById(R.id.buttonConexao);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (meuBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }

        // Verifica se o Bluetooth está ativado
        if (!meuBluetoothAdapter.isEnabled()) {
            solicitarBluetooth();
        }
        buttonConexao.setOnClickListener(v -> {
            // Quando o botão for pressionado, tenta ativar o Bluetooth (caso não esteja ativado)
            if (meuBluetoothAdapter != null && !meuBluetoothAdapter.isEnabled()) {
                solicitarBluetooth(); // Chama a função que pede para ativar o Bluetooth
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth já está ativado", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void solicitarBluetooth() {
        // Verifica permissões apenas no Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
        }

        // Solicita ativação do Bluetooth usando a nova API
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothResultLauncher.launch(enableBtIntent);
    }

    // Lida com a resposta do usuário para a solicitação de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarBluetooth(); // Agora podemos tentar ativar o Bluetooth
            } else {
                Toast.makeText(this, "Permissão de Bluetooth necessária", Toast.LENGTH_LONG).show();
            }
        }
    }
}
