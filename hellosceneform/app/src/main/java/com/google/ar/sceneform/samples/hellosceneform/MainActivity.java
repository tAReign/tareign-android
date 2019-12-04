package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    final int SIZE_GRID = 100;
    Button createButton;
    Button loadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setButtons();

    }

    void initViews() {
        createButton = (Button) findViewById(R.id.createButton);
        loadButton = (Button) findViewById(R.id.loadButton);
    }

    void setButtons() {
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GridActivity.class);
                startActivity(intent);
            }
        });
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("loadfile", "Attempting to load a file");
                Intent intent = new Intent(v.getContext(), GridActivity.class);
                int[] myGrid = new int[SIZE_GRID];
                File path = getFilesDir();
                File file = new File(path, "/finalProjectVRGrid.txt");
                try {
                    Scanner scanner = new Scanner(file);
                    for (int i = 0; i < myGrid.length; i++) {
                        String bytes = scanner.nextLine();
                        myGrid[i] = Integer.parseInt(bytes);
                    }
                    Log.d("loadfile", "Load worked");
                } catch (FileNotFoundException e) {
                    myGrid = null;
                    Log.wtf("loadfile", "Load failed");
                }
                intent.putExtra("gridData", myGrid);
                startActivity(intent);
            }
        });
    }
}

