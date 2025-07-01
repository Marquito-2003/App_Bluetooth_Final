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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity2 extends AppCompatActivity {
    private String pendingMacAddress;

    // --- MUDANÇA 1: Criar instâncias dos fragmentos APENAS UMA VEZ ---
    private final Fragment fragmentBraco = new braco_robotico();
    private final Fragment fragmentGarra = new garra();
    private Fragment activeFragment = fragmentBraco; // Guarda a referência do fragmento ativo

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == AppState.MESSAGE_READ) {
                String receivedData = (String) msg.obj;
            }
        }
    };

    // --- LAUNCHERS MODERNOS ---
    private final ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
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
        setContentView(R.layout.activity_main_misto);

        // --- Encontra os botões que existem no XML ---
        Button btnConnect = findViewById(R.id.buttonConexao);
        Button btnFrente = findViewById(R.id.buttonFrente);
        Button btnRe = findViewById(R.id.buttonRe);
        Button btnEsquerda = findViewById(R.id.buttonEsquerda);
        Button btnDireita = findViewById(R.id.buttonDireita);

        // Botão único para alternar os módulos
        Button btnAlternarModulo = findViewById(R.id.buttonModulos);

        // --- Configura as ações ---
        btnConnect.setOnClickListener(v -> toggleConnection());
        btnFrente.setOnClickListener(v -> sendCommand("FRENTE"));
        btnRe.setOnClickListener(v -> sendCommand("RE"));
        btnEsquerda.setOnClickListener(v -> sendCommand("ESQUERDA"));
        btnDireita.setOnClickListener(v -> sendCommand("DIREITA"));

        // --- MUDANÇA 2: Configuração inicial dos fragmentos ---
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentGarra, "2").hide(fragmentGarra)
                    .add(R.id.fragment_container, fragmentBraco, "1")
                    .commit();
            activeFragment = fragmentBraco;
        }

        // --- MUDANÇA 3: Lógica para alternar com HIDE e SHOW ---
        btnAlternarModulo.setOnClickListener(v -> {
            Fragment nextFragment = (activeFragment == fragmentBraco) ? fragmentGarra : fragmentBraco;
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(nextFragment)
                    .commit();
            activeFragment = nextFragment;
        });
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
        } else {
            Intent intent = new Intent(this, ListaDispositos.class);
            connectDeviceLauncher.launch(intent);
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
            Toast.makeText(this, "Conectado: " + mac, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show();
        }
    }

    // O método loadFragment não é mais necessário com a abordagem show/hide
}
