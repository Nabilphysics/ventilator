package com.crux.ventapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class ChooseActivity extends AppCompatActivity {


    Button button_chooseLight;
    Button button_chooseCar;
    Button btnGraph;
    Button btnSine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        button_chooseLight = findViewById(R.id.button_chooseLight);
        button_chooseCar = findViewById(R.id.button_chooseCar);
        btnGraph =  findViewById(R.id.btnGraph);
        btnSine =  findViewById(R.id.btnSine);


        // button_chooseLight.setOnClickListener(v -> startActivity(new Intent(ChooseActivity.this,LedTestingActivity.class)));
        //
        // button_chooseCar.setOnClickListener(v -> startActivity(new Intent(ChooseActivity.this,RCControlActivity.class)));

        btnGraph.setOnClickListener( v-> startActivity(new Intent(ChooseActivity.this,GraphActivity.class)));

        btnSine.setOnClickListener(v-> startActivity(new Intent(ChooseActivity.this,SineGraphActivity.class)));

    }
}
