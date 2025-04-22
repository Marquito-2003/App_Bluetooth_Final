package com.example.appbluetoothfinal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.SeekBar;
import java.util.Locale;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class braco_robotico extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage( Message msg) {
            if (msg.what == AppState.MESSAGE_READ) {
                String receivedData = (String) msg.obj;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braco_robotico);

        SeekBar sb1 = findViewById(R.id.seekBarMotor1);
        SeekBar sb2 = findViewById(R.id.seekBarMotor2);
        SeekBar sbBase = findViewById(R.id.seekBarBase);
        SeekBar sbG = findViewById(R.id.seekBarGarra);

        setupSeekBar(sb1, findViewById(R.id.tvMotor1), "MOTOR 1");
        setupSeekBar(sb2, findViewById(R.id.tvMotor2), "MOTOR 2");
        setupSeekBar(sbBase, findViewById(R.id.tvBase), "BASE");
        setupSeekBar(sbG, findViewById(R.id.tvGarra), "GARRA");

        Button btnSend = findViewById(R.id.btnEnviar);
        btnSend.setOnClickListener(v -> sendRobotCommand());

        Button btnBack = findViewById(R.id.button_tela_inicial);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppState.getInstance().setCurrentHandler(handler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppState.getInstance().setCurrentHandler(null);
    }

    private void setupSeekBar(SeekBar seekBar, TextView textView, String label) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.format(Locale.getDefault(), "%s = %d°", label, progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void sendRobotCommand() {
        String command = String.format(Locale.US,
                "M1:%d;M2:%d;B:%d;G:%d;",
                ((SeekBar) findViewById(R.id.seekBarMotor1)).getProgress(),
                ((SeekBar) findViewById(R.id.seekBarMotor2)).getProgress(),
                ((SeekBar) findViewById(R.id.seekBarBase)).getProgress(),
                ((SeekBar) findViewById(R.id.seekBarGarra)).getProgress());

        if (AppState.getInstance().isConnected()) {
            AppState.getInstance().sendData(command);
            Toast.makeText(this, "Enviado: " + command, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Não conectado", Toast.LENGTH_SHORT).show();
        }
    }
}