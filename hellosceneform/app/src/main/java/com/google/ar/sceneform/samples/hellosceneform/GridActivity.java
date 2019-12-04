package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GridActivity extends AppCompatActivity {
    Integer activeButton = 0;

    GridView gridView;
    Button floorButton;
    Button wallButton;
    Button treeButton;
    Button barrelButton;
    Button saveButton;
    Button ARButton;

    final Integer FLOOR_INT = 0;
    final Integer WALL_INT = 1;
    final Integer TREE_INT = 2;
    final Integer BARREL_INT = 3;

    public GridOfModels gridOfModels;
    Integer[] myGrid = new Integer[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        initViews();
        setUpButtons();

        Intent intent = getIntent();
        int[] grid = intent.getIntArrayExtra("gridData");
        if (grid != null) {
            for (int i = 0; i < grid.length; i++) {
                myGrid[i] = grid[i];
            }
        }
        gridOfModels = new GridOfModels(myGrid);
        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1, gridOfModels.getGrid());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myGrid[position] = activeButton;
                gridOfModels.setGrid(myGrid);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void setUpButtons() {
        floorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeButton = FLOOR_INT;
            }
        });
        wallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeButton = WALL_INT;
            }
        });
        treeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeButton = TREE_INT;
            }
        });
        barrelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeButton = BARREL_INT;
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = openFileOutput("finalProjectVRGrid.txt", MODE_PRIVATE);
                    for (Integer currInt : myGrid) {
                        fileOutputStream.write(currInt.toString().getBytes());
                        fileOutputStream.write("\n".getBytes());
                    }
                    fileOutputStream.close();
                    Log.d("Save", "Save was successful");
                    Toast.makeText(v.getContext(), "File Saved", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Unable to save file", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Unable to save file", Toast.LENGTH_LONG).show();

                }

            }
        });
        ARButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), HelloSceneformActivity.class);
                int[] grid = new int[myGrid.length];
                for (int i = 0; i < myGrid.length; i++) {
                    grid[i] = myGrid[i];
                }
                intent.putExtra("gridValues", grid);
                startActivity(intent);
            }
        });
    }

    public void initViews() {
        gridView = (GridView) findViewById(R.id.gridView);
        floorButton = (Button) findViewById(R.id.floorButton);
        wallButton = (Button) findViewById(R.id.wallButton);
        treeButton = (Button) findViewById(R.id.treeButton);
        barrelButton = (Button) findViewById(R.id.barrelButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        ARButton = (Button) findViewById(R.id.renderButton);
    }
}
