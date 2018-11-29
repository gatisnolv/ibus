package com.example.g.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
//import org.apache.commons.csv.*;
import org.onebusaway.gtfs.impl.*;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.*;

import de.siegmar.fastcsv.reader.*;

import java.util.zip.*;
import java.util.*;


public class MainActivity extends AppCompatActivity {
    private GTFS gtfs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final TextView helloTextView = findViewById(R.id.text_view_id);
//        helloTextView.setText(R.string.stops);
        System.out.println("something");
        gtfs = new GTFS(this);
    }

//    private File getFile(int filenameId) throws IOException {
//        BufferedInputStream bis = new BufferedInputStream(getAssets().open(getString(filenameId)));
//        File f = new File(this.getFilesDir().getPath().toString() + getString(filenameId));
//        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
//        byte buffer[] = new byte[1024];
//        int numberOfBytes;
//        while ((numberOfBytes = bis.read(buffer)) != -1) {
//            bos.write(buffer, 0, numberOfBytes);
//        }
//        bis.close();
//        bos.close();
//        return f;
//    }


}
