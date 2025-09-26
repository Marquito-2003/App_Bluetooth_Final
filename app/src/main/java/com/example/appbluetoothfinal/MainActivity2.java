package com.example.appbluetoothfinal;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;

// Removidas as importações de Fragment, pois não são mais usadas
// import androidx.fragment.app.Fragment;
// import androidx.fragment.app.FragmentManager;
// import androidx.fragment.app.FragmentTransaction;

public class MainActivity2 extends AppCompatActivity {
    private String pendingMacAddress;

    // --- MUDANÇA: Variáveis de Fragment removidas ---
    // private final Fragment fragmentBraco = new braco_robotico();
    // private final Fragment fragmentGarra = new garra();
    // private Fragment activeFragment = fragmentBraco;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == AppState.MESSAGE_READ) {
                String receivedData = (String) msg.obj;
                // Faça algo com os dados recebidos, se necessário
            }
        }
    };

    // --- LAUNCHERS MODERNOS (sem alteração) ---
    private final ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth é necessário para usar o app.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Permissão de conexão Bluetooth é necessária.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> connectDeviceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    pendingMacAddress = result.getData().getStringExtra(ListaDispositos.ENDERECO_MAC);
                    if (AppState.getInstance().checkBluetoothConnectPermission(this)) {
                        attemptConnection(pendingMacAddress);
                    } else {
                        requestBluetoothConnectPermission();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- MUDANÇA: Define o layout XML correto ---
        setContentView(R.layout.activity_main);


        // --- MUDANÇA: Encontra os botões que existem no novo XML ---
        Button btnConnect = findViewById(R.id.buttonConexao);
        Button btnLed1 = findViewById(R.id.button3);
        Button btnLed2 = findViewById(R.id.button);
        Button btnLed3 = findViewById(R.id.button2);
        Button btnBraco = findViewById(R.id.btn1);

        // --- MUDANÇA: Configura as ações para os novos botões ---
        btnConnect.setOnClickListener(v -> toggleConnection());

        // Ações para os botões de LED
        btnLed1.setOnClickListener(v -> sendCommand("AÇÃO 1")); // Comando pode ser "L1_ON", "1", etc.
        btnLed2.setOnClickListener(v -> sendCommand("AÇÃO 2"));
        btnLed3.setOnClickListener(v -> sendCommand("AÇÃO 3"));

        // Ação para o botão do Braço Robótico
        // Exemplo: Abrir uma nova Activity para controlar o braço
        btnBraco.setOnClickListener(v -> {
            // Crie uma nova Activity chamada 'BracoRoboticoActivity' para esta funcionalidade
            // Intent intent = new Intent(MainActivity2.this, BracoRoboticoActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Abrir tela do Braço Robótico", Toast.LENGTH_SHORT).show();
        });

        // --- MUDANÇA: Toda a lógica de inicialização de fragmentos foi removida ---
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppState.getInstance().setCurrentHandler(handler);
        updateConnectionStatus();
        checkBluetoothState();
    }

    private void toggleConnection() {
        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().disconnect();
            updateConnectionStatus();
            Toast.makeText(this, "Desconectado.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, ListaDispositos.class);
            connectDeviceLauncher.launch(intent);
        }
    }

    private void sendCommand(String command) {
        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().sendData(command);
            Toast.makeText(this, "Comando enviado: " + command, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Não conectado. Conecte-se a um dispositivo primeiro.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBluetoothState() {
        if (!AppState.getInstance().isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
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

    private void attemptConnection(String mac) {
        if (AppState.getInstance().connect(mac, this)) {
            updateConnectionStatus();
            Toast.makeText(this, "Conectado com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha na conexão com o dispositivo.", Toast.LENGTH_SHORT).show();
        }
    }

    // O método loadFragment não é mais necessário e foi removido.
}