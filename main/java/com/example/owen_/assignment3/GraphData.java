package com.example.owen_.assignment3;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.os.Bundle;
import android.view.View;

public class GraphData extends View {


    public static boolean LINE = true;

    private Paint paint;
    private float[] values;
    private String[] str;
    private String[] verlabels;
    private String title;
    private boolean type;
    Context context;


    public GraphData(Context context, float[] values, String title, String[] str, String[] verlabels, boolean type) {

        super(context);
        if (values == null) {
            values = new float[0];
        }
        else {
            this.values = values;
        }
        if (title == null) {
            title = "";
        }
        else {
            this.title = title;
        }
        if (str == null) {
            this.str = new String[0];
        }
        else {
            this.str = str;
        }
        if (verlabels == null) {
            this.verlabels = new String[0];
        }
        else {
            this.verlabels = verlabels;
            this.type = type;
            paint = new Paint();
        }
    }

    /*Creating the canvas and the graph that will be displayed to the user
    * we set the parameters for the vertical and horizontal lines,
    * the colors we want and create the graph itself*/
    @Override
    protected void onDraw(final Canvas canvas) {
        context=getContext();
        float border = 15;
        float horstart = border * 2;
        float height = getHeight();
        float width = getWidth();
        float max = getMax();
        Log.w("max", ""+max);
        float min = getMin();
        Log.w("min", ""+min);
        float diff = max - min;
        float graphheight = height - (2 * border);
        float graphwidth = width - (2 * border);

        paint.setTextAlign(Paint.Align.LEFT);
        int vers = verlabels.length;
        for (int i = 0; i < verlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            float y = ((graphheight / vers) * i) + border;
            canvas.drawLine(horstart, y, width, y, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(10);
            canvas.drawText(verlabels[i], 0, y, paint);
        }

        int hors = values.length;
        for (int i = 0; i < str.length; i++) {
            paint.setColor(Color.DKGRAY);
            float x = ((graphwidth / hors) * i) + horstart;
            canvas.drawLine(x, height - border, x, border, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            if (i==str.length)
                paint.setTextAlign(Paint.Align.RIGHT);
            if (i==0)
                paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.WHITE);
            paint.setTextSize(9);
            canvas.drawText( str[i], x, height - 4, paint);
        }

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

        if (max != min) {
            int color = ContextCompat.getColor(context, R.color.graph);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);

            if (type==true) {
                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;
                for (int i = 0; i < values.length; i++) {
                    float graph_h = getHeight()-(border*2);
                    float ind_h = graph_h/7;
                    float t = values[i]/5;
                    float top = (graph_h - ind_h*(t));
                    float acc = ind_h/5;
                    acc = acc * (values[i]%5);
                    canvas.drawRect((i * colwidth) + horstart, top+border-acc , ((i * colwidth) + horstart) + (colwidth - 1), graph_h+border, paint);
                }
            } else {
                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;
                float halfcol = colwidth / 2;
                float lasth = 0;
                for (int i = 0; i < values.length; i++) {
                    float val = values[i] - min;
                    float rat = val / diff;
                    float h = graphheight * rat;
                    if (i > 0)
                        canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight, (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
                    lasth = h;
                }
            }
        }
    }



    private float getMax() {
        float largest = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] > largest)
                largest = values[i];
        return largest;
    }

    private float getMin() {
        float smallest = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] < smallest)
                smallest = values[i];
        return smallest;
    }


}