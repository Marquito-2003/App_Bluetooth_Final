package com.example.appbluetoothfinal;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {
    private static final int REQUEST_CONNECT = 3;
    private String pendingMacAddress;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == AppState.MESSAGE_READ) {
                String receivedData = (String) msg.obj;
            }
        }
    };

    private final ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    checkBluetoothState();
                } else {
                    Toast.makeText(this, "Bluetooth necessário", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    if (pendingMacAddress != null) {
                        attemptConnection(pendingMacAddress);
                    }
                } else {
                    Toast.makeText(this, "Permissão necessária", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnConnect = findViewById(R.id.buttonConexao);
        Button btnAction1 = findViewById(R.id.button);
        Button btnAction2 = findViewById(R.id.button2);
        Button btnAction3 = findViewById(R.id.button3);
        Button btnNext = findViewById(R.id.btn1);

        btnConnect.setOnClickListener(v -> toggleConnection());
        btnAction1.setOnClickListener(v -> sendCommand("AÇÃO 1"));
        btnAction2.setOnClickListener(v -> sendCommand("AÇÃO 2"));
        btnAction3.setOnClickListener(v -> sendCommand("AÇÃO 3"));
        btnNext.setOnClickListener(v -> startActivity(new Intent(this, braco_robotico.class)));

        checkBluetoothState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppState.getInstance().setCurrentHandler(handler);
        updateConnectionStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppState.getInstance().setCurrentHandler(null);
    }

    private void toggleConnection() {
        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().disconnect();
            updateConnectionStatus();
        } else {
            startActivityForResult(new Intent(this, ListaDispositos.class), REQUEST_CONNECT);
        }
    }

    private void sendCommand(String command) {
        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().sendData(command);
        } else {
            Toast.makeText(this, "Não conectado", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBluetoothState() {
        if (AppState.getInstance().checkBluetoothConnectPermission(this)) {
            if (!AppState.getInstance().isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtLauncher.launch(enableBtIntent);
            }
        } else {
            requestBluetoothConnectPermission();
        }
    }

    private void requestBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }
    }

    private void updateConnectionStatus() {
        Button btnConnect = findViewById(R.id.buttonConexao);
        btnConnect.setText(AppState.getInstance().isConnected() ? "Desconectar" : "Conectar");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONNECT && resultCode == Activity.RESULT_OK) {
            pendingMacAddress = data.getStringExtra(ListaDispositos.ENDERECO_MAC);
            if (AppState.getInstance().checkBluetoothConnectPermission(this)) {
                attemptConnection(pendingMacAddress);
            } else {
                requestBluetoothConnectPermission();
            }
        }
    }

    private void attemptConnection(String mac) {
        if (AppState.getInstance().connect(mac, this)) {
            updateConnectionStatus();
            Toast.makeText(this, "Conectado: " + mac, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show();
        }
    }
}