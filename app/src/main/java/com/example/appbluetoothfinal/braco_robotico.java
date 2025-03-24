package com.example.appbluetoothfinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class braco_robotico extends AppCompatActivity {
    private Button button_voltar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braco_robotico);
        button_voltar = findViewById(R.id.button_tela_inicial);
        button_voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrimeiraTela();
            }
        });

    }

    private void PrimeiraTela(){
        Intent varialvel2 = new Intent(this, MainActivity2.class);
        startActivity(varialvel2);
    }

}