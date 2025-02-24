package com.example.appbluetoothfinal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {

    private BluetoothAdapter meuBluetoothAdapter;
    private static final int SOLICITA_CONEXAO = 3;
    private static String MAC = null;
    private Button buttonConexao, button, button2, button3;
    BluetoothDevice meuDevice = null;
    boolean conexao = false;
    BluetoothSocket meuSocket = null;
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Launcher para ativação do Bluetooth
    private ActivityResultLauncher<Intent> bluetoothResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "O Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth não foi ativado, o app será fechado", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    // Launcher para solicitação da permissão (Android 12+)
    private ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Se a permissão foi concedida, tenta ativar o Bluetooth
                    if (!meuBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        bluetoothResultLauncher.launch(enableBtIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "O Bluetooth já está ativado", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Permissão de Bluetooth necessária, o app será fechado", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonConexao = findViewById(R.id.buttonConexao);

        meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (meuBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Se o Bluetooth não estiver ativado, solicita a ativação (incluindo a verificação de permissão em Android 12+)
        if (!meuBluetoothAdapter.isEnabled()) {
            solicitarBluetooth();
        } else {
            Toast.makeText(getApplicationContext(), "O Bluetooth já está ativado", Toast.LENGTH_LONG).show();
        }

        // Exemplo de botão para solicitar conexão (abrindo outra Activity)
        buttonConexao.setOnClickListener(v -> {
            if(conexao) {
                try {
                    meuSocket.close();
                    conexao = false;
                    buttonConexao.setText("Conectar");
                    Toast.makeText(getApplicationContext(), "O Bluetooth foi desconectado", Toast.LENGTH_LONG).show();
                }catch(IOException erro){
                    Toast.makeText(getApplicationContext(), "Ocorreu um erro" + erro, Toast.LENGTH_LONG).show();
                }
            }else{
                Intent abreLista = new Intent(MainActivity2.this, ListaDispositos.class);
                startActivityForResult(abreLista, SOLICITA_CONEXAO);
            }

        });
    }

    private void solicitarBluetooth() {
        // Se estiver em Android 12 (API 31) ou superior, verifica a permissão BLUETOOTH_CONNECT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                return;
            }
        }
        // Se a permissão já estiver concedida (ou não for necessária), solicita a ativação do Bluetooth
        if (!meuBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothResultLauncher.launch(enableBtIntent);

        } else {
            Toast.makeText(getApplicationContext(), "O Bluetooth já está ativado", Toast.LENGTH_LONG).show();

        }
    }

    // Tratamento do resultado da Activity de conexão (mantido o onActivityResult para simplicidade)
    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SOLICITA_CONEXAO) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                MAC = data.getExtras().getString(ListaDispositos.ENDERECO_MAC);
                //Toast.makeText(getApplicationContext(), "MAC FINAL: " + MAC, Toast.LENGTH_LONG).show();
                meuDevice = meuBluetoothAdapter.getRemoteDevice(MAC);
                try{
                    meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);
                    meuSocket.connect();
                    conexao = true;
                    buttonConexao.setText("Desconectar");
                    Toast.makeText(getApplicationContext(), "Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();
                }catch(IOException erro){
                    conexao = false;
                    Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Falha ao obter o MAC", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
