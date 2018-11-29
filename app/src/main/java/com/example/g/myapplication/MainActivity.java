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

//import org.apache.commons.csv.*;

public class MainActivity extends AppCompatActivity {
    //    private Iterable<CsvContainer> stops;
//    private Iterable<CsvContainer> shapes;
    private CsvContainer stops;
    private CsvContainer shapes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final TextView helloTextView = findViewById(R.id.text_view_id);
//        helloTextView.setText(R.string.stops);
        System.out.println("something");
        try{testfn2();}
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void testfn2() throws IOException {
        String fileName = "/home/g/Downloads/gtfs.zip";
        ZipFile zipFile = new ZipFile(getFile(R.string.gtfs));
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        // zipFile.close();
        ZipEntry ze;

        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);

//        CsvContainer csv = csvReader.read(file, StandardCharsets.UTF_8);
//        for (CsvRow row : csv.getRows()) {
//            System.out.println("First column of line: " + row.getField("name"));
//        }

        while (entries.hasMoreElements()) {
            ze = entries.nextElement();
            InputStream is;
            if (ze.getName().equals("stops.txt")) {
                is = zipFile.getInputStream(ze);
                Reader bufferedReader = new BufferedReader(new InputStreamReader(is));
                stops = csvReader.read(bufferedReader);
            } else if (ze.getName().equals("shapes.txt")) {

            }
        }
        for (CsvRow row : stops.getRows()) {
            System.out.println("stop: " + row.getField("stop_name"));
        }

//        for (CsvContainer record : stops) {
//            String name = record.get("stop_name");
//            String lat = record.get("stop_lat");
//            String lon = record.get("stop_lon");
//            System.out.println(name + ": " + "lat: " + lat + " lon: " + lon);
//        }
    }

    private void testfn() {
        GtfsReader reader = new GtfsReader();
        System.out.println("blabla");
        try {
//            reader.setInputLocation(getFile(R.string.gtfs));
            reader.setInputLocation(getFile(R.string.gtfs));
            Log.d("here", "there");
            GtfsDaoImpl store = new GtfsDaoImpl();
            reader.setEntityStore(store);
            System.out.println("blabla2");
            reader.run();


            // Access entities through the store
            for (Route route : store.getAllRoutes()) {
                System.out.println("route: " + route.getShortName());
            }
        } catch (IOException e) {
//            System.out.println("testfn IOExc");
            //Logging exception
            e.printStackTrace();
        }


    }

    private File getFile(int filenameId) throws IOException {
//        try {
        BufferedInputStream bis = new BufferedInputStream(getAssets().open(getString(filenameId)));
//            in = new BufferedReader(new InputStreamReader(inputStream));

        File f = new File(this.getFilesDir().getPath().toString() + getString(filenameId));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
//        FileOutputStream bos=openFileOutput(getString(filenameId), this.MODE_PRIVATE);

        byte buffer[] = new byte[1024];
//            int length = 0;

        int numberOfBytes;
        while ((numberOfBytes = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, numberOfBytes);
        }

        bis.close();
        bos.close();

        return f;
//        } catch (IOException e) {
        //Logging exception
//        }
    }

//    private void readCSVFile(){
//        Reader in;
//        try {
//            InputStream inputStream = getAssets().open(getString(R.string.stops));
//            in = new BufferedReader(new InputStreamReader(inputStream));
//            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
//            for (CSVRecord record : records) {
//                String name = record.get("stop_name");
//                String lat = record.get("stop_lat");
//                String lon = record.get("stop_lon");
//                System.out.println(name+": "+"lat: "+lat+" lon: "+lon);
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
