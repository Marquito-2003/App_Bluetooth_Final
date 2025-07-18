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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // 👈 MUDANÇA IMPORTANTE

import java.util.Locale;

//           👇 HERDA DE FRAGMENT, NÃO DE APPCOMPATACTIVITY
public class braco_robotico extends Fragment {

    // A lógica do Handler permanece a mesma
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == AppState.MESSAGE_READ) {
                String receivedData = (String) msg.obj;
                // Faça algo com os dados recebidos, se necessário
            }
        }
    };

    // Construtor vazio é necessário para Fragments
    public braco_robotico() {}

    // 👇 MÉTODO PRINCIPAL DO FRAGMENT PARA CRIAR A VIEW
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Infla o layout XML e o atribui a uma variável 'view'
        View view = inflater.inflate(R.layout.activity_braco_robotico, container, false);

        // 2. Usa a 'view' para encontrar os componentes da interface
        SeekBar sb1 = view.findViewById(R.id.seekBarMotor1);
        SeekBar sb2 = view.findViewById(R.id.seekBarMotor2);
        SeekBar sbBase = view.findViewById(R.id.seekBarMotor2);
        SeekBar sbG = view.findViewById(R.id.seekBarBase);

        // A lógica de setup não muda, mas passamos a 'view' para a função
        setupSeekBar(view, sb1, R.id.tvMotor1, "MOTOR 1");
        setupSeekBar(view, sb2, R.id.tvMotor2, "MOTOR 2");
        setupSeekBar(view, sbBase, R.id.tvBase, "BASE");
        setupSeekBar(view, sbG, R.id.tvGarra, "GARRA");

        Button btnSend = view.findViewById(R.id.btnEnviar);
        btnSend.setOnClickListener(v -> sendRobotCommand(view)); // Passamos a 'view'

        Button btnBack = view.findViewById(R.id.button_tela_inicial);
        // Em um fragment, 'finish()' deve ser chamado na Activity pai
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // 3. Retorna a 'view' pronta
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

    // A função de setup precisa da 'view' para encontrar o TextView
    private void setupSeekBar(View view, SeekBar seekBar, int textViewId, String label) {
        TextView textView = view.findViewById(textViewId);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 'getContext()' é usado para obter o contexto no Fragment
                textView.setText(String.format(Locale.getDefault(), "%s = %d°", label, progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // A função de envio precisa da 'view' para encontrar as SeekBars
    private void sendRobotCommand(View view) {
        String command = String.format(Locale.US,
                "M1:%d;M2:%d;B:%d;G:%d;",
                ((SeekBar) view.findViewById(R.id.seekBarMotor1)).getProgress(),
                ((SeekBar) view.findViewById(R.id.seekBarMotor2)).getProgress(),
                ((SeekBar) view.findViewById(R.id.seekBarMotor2)).getProgress(),
                ((SeekBar) view.findViewById(R.id.seekBarBase)).getProgress());

        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().sendData(command);
            // 'getContext()' é a forma correta de pegar o contexto para o Toast
            Toast.makeText(getContext(), "Enviado: " + command, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Não conectado", Toast.LENGTH_SHORT).show();
        }
    }
}