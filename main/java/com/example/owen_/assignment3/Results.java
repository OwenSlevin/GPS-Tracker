package com.example.owen_.assignment3;

import android.content.DialogInterface;
import android.graphics.PointF;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class Results extends AppCompatActivity {

    TextView speed,altitude,distance,time;
    Button graph;
    View view;

    /*
    * Brings in all gps data. Then displays statistics to the user*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        speed=(TextView) findViewById(R.id.speed);
        altitude=(TextView) findViewById(R.id.alt);
        distance=(TextView) findViewById(R.id.dist);
        time=(TextView) findViewById(R.id.time);
        graph =(Button) findViewById(R.id.graph);
        view =(View) findViewById(R.id.view2);

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGraphDialog();
                }
        });
        if(getIntent()!=null) {
            speed.setText(getIntent().getStringExtra("speed")+"m/s");
            distance.setText(getIntent().getStringExtra("distance")+"meters");
            time.setText(getIntent().getStringExtra("time")+" seconds");
            altitude.setText(getIntent().getStringExtra("Altitude"));
        }
    }

    /*
    * Method to display the graph for the user and buttons*/
    private void showGraphDialog() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(Results.this);
        alert.setTitle("Graph");
        float[] values = new float[] {5,3,6,4,3,4};

        String[] verlabels = new String[] { "Race 1","Race 2","Race 3" };
        String[] horlabels = new String[] { "Your Values"};
        GraphData graphView = new GraphData(Results.this, values,"GraphView",horlabels, verlabels,true );

        alert.setView(graphView);

        alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,  int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

}
