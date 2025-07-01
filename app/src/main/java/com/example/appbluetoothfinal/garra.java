package com.example.appbluetoothfinal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/**
 * Fragmento adaptado para controlar o layout visual completo do braço robótico.
 */
public class garra extends Fragment {

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == AppState.MESSAGE_READ) {
                String receivedData = (String) msg.obj;
            }
        }
    };

    public garra() {
        // Construtor vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Carrega o layout visual completo
        View view = inflater.inflate(R.layout.fragment_garra, container, false); // ou R.layout.fragment_garra, dependendo do nome do seu arquivo

        // Encontra o botão de envio no layout. O ID correto é "buttonEnviar".
        Button btnSend = view.findViewById(R.id.buttonEnviar);
        btnSend.setOnClickListener(v -> sendRobotCommand(view));

        // Não precisamos mais de listeners nas SeekBars, pois não há TextViews para atualizar.
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppState.getInstance().setCurrentHandler(handler);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppState.getInstance().setCurrentHandler(null);
    }

    /**
     * Envia o comando com os valores de TODAS as SeekBars.
     * @param view A view do fragmento, usada para encontrar os componentes.
     */
    private void sendRobotCommand(View view) {
        // Encontra todas as SeekBars no momento do clique
        SeekBar sbMotor1 = view.findViewById(R.id.seekBarMotor1);
        SeekBar sbMotor2 = view.findViewById(R.id.seekBarMotor2);
        SeekBar sbBase = view.findViewById(R.id.seekBarBase);
        SeekBar sbGarra = view.findViewById(R.id.seekBarGarra);

        // Cria o comando completo com os 4 valores
        String command = String.format(Locale.US,
                "M1:%d;M2:%d;B:%d;G:%d;",
                sbMotor1.getProgress(),
                sbMotor2.getProgress(),
                sbBase.getProgress(),
                sbGarra.getProgress());

        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().sendData(command);
            Toast.makeText(getContext(), "Enviado: " + command, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Não conectado", Toast.LENGTH_SHORT).show();
        }
    }
}