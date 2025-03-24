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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {

    private BluetoothAdapter meuBluetoothAdapter;
    private static final int SOLICITA_CONEXAO = 3;
    private static final int MESSAGE_READ = 4;
    ConnectedThread connectedThread;
    private static String MAC = null;
    private Button buttonConexao, button, button2, button3, button1;
    BluetoothDevice meuDevice = null;
    boolean conexao = false;
    BluetoothSocket meuSocket = null;
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    Handler handler;
    StringBuilder dadosBluetooth = new StringBuilder();
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

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DECLARAÇÃO DOS BOTÕES, SEM A DECLARAÇÃO O APP FECHA
        buttonConexao = findViewById(R.id.buttonConexao);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button1 = findViewById(R.id.btn1);
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
            if (conexao) {
                try {
                    meuSocket.close();
                    conexao = false;
                    buttonConexao.setText("Conectar");
                    Toast.makeText(getApplicationContext(), "O Bluetooth foi desconectado", Toast.LENGTH_LONG).show();
                } catch (IOException erro) {
                    Toast.makeText(getApplicationContext(), "Ocorreu um erro" + erro, Toast.LENGTH_LONG).show();
                }
            } else {
                Intent abreLista = new Intent(MainActivity2.this, ListaDispositos.class);
                startActivityForResult(abreLista, SOLICITA_CONEXAO);
            }

        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("AÇÃO 1");
                } else {
                    Toast.makeText(getApplicationContext(), "Não conectado", Toast.LENGTH_LONG).show();
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("AÇÃO 2");
                } else {
                    Toast.makeText(getApplicationContext(), "Não conectado", Toast.LENGTH_LONG).show();
                }
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SegundaTela();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connectedThread.enviar("AÇÃO 3");
                } else {
                    Toast.makeText(getApplicationContext(), "Não conectado", Toast.LENGTH_LONG).show();
                }
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String recebidos = (String) msg.obj;
                    dadosBluetooth.append(recebidos);
                    int fimInformacao = dadosBluetooth.indexOf("}");
                    if (fimInformacao > 0) {
                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);
                        int tamInformacao = dadosCompletos.length();
                        if(dadosBluetooth.charAt(0)=='{') {
                            String dadosFinais = dadosBluetooth.substring(1, tamInformacao);
                            Log.d("Recebidos", dadosFinais);

                            // PARA TESTES E CASOS DE PRECISAR ALTERAR O TEXTO DO BOTÃO:
                            if (dadosFinais.contains("AÇÃO 1")) {
                                button.setText("BOTAO 1 ATIVO");
                            } else if (dadosFinais.contains("AÇÃO 1 OFF")) {
                                button.setText("BOTAO 1 DESATIVADO");
                            }
                        }
                        dadosBluetooth.delete(0, dadosBluetooth.length());
                        }
                    }
                }
        };
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

    private void SegundaTela(){
        Intent variavel =  new Intent(this, braco_robotico.class);
        startActivity(variavel);
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
                try {
                    meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);
                    meuSocket.connect();
                    conexao = true;
                    connectedThread = new ConnectedThread(meuSocket);
                    connectedThread.start();
                    buttonConexao.setText("Desconectar");
                    Toast.makeText(getApplicationContext(), "Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();
                } catch (IOException erro) {
                    conexao = false;
                    Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Falha ao obter o MAC", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

        private class ConnectedThread extends Thread {
            public final InputStream mmInStream;
            private final OutputStream mmOutStream;
            private byte[] mmBuffer;

            public ConnectedThread(BluetoothSocket socket) {

                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {

                }
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() { //RECEBE OS DADOS DO ARDUINO
                mmBuffer = new byte[1024];
                int numBytes; // bytes do read()

                while (true) {
                    try {
                        // Read from the InputStream
                        numBytes = mmInStream.read(mmBuffer);
                       String dadosbt = new String(mmBuffer, 0, numBytes);
                       handler.obtainMessage( MESSAGE_READ, numBytes, -1,dadosbt).sendToTarget();

                    } catch(IOException e) {
                        break;
                    }
                }
            }

            public void enviar(String dadosenviar) {
                byte[] msgBuffer= dadosenviar.getBytes();
                try {
                    mmOutStream.write(msgBuffer);
                } catch(IOException e) {
                }
            }

            }
        }

