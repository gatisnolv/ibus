package com.example.g.myapplication;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

import android.content.*;

import de.siegmar.fastcsv.reader.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class GTFS {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String BOM="\uFEFF";//Addressing first column by name should be prepended with this, or access fields by index
    private static final String STOP_NAME="stop_name";
    private static final int SHAPE_ID=0;
    private static final int ROUTE_ID=0;
    private static final int SERVICE_ID=0;
    private static final int TRIP_ID=0;
    private static final String TRIP_HEADSIGN="trip_headsign";




    private CsvContainer stops;
    private CsvContainer shapes;
    private CsvContainer routes;
    private CsvContainer calendar;
    private CsvContainer stop_times;
    private CsvContainer trips;

    private Context cxt;

    GTFS(Context context) {
        this.cxt = context;
        try {
            getGTFS();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getGTFS() throws IOException {
        ZipFile zipFile = new ZipFile(getFile(R.string.gtfs));
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry ze;
        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);

        while (entries.hasMoreElements()) {
            ze = entries.nextElement();
            if (ze.getName().equals(cxt.getString(R.string.stops))) {
                stops = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.shapes))) {
                shapes = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.routes))) {
                routes = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.calendar))) {
                calendar = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.stop_times))) {
                stop_times = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.trips))) {
                trips = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            }
        }
//        zipFile.close();
//        System.out.println("tstcount: "+stops.getRowCount());
//        System.out.println("tstcount: "+shapes.getRowCount());
//        System.out.println("tstcount: "+routes.getRowCount());
//        System.out.println("tstcount: "+calendar.getRowCount());
//        System.out.println("tstcount: "+stop_times.getRowCount());
//        System.out.println("tstcount: "+trips.getRowCount());

        System.out.println("tstcount1: "+stops.getRow(1664).getField(STOP_NAME));
        System.out.println("tstcount2: "+shapes.getRow(65854).getField(SHAPE_ID));
        System.out.println("tstcount3: "+routes.getRow(80).getField(ROUTE_ID));
        System.out.println("tstcount4: "+calendar.getRow(152).getField(SERVICE_ID));
        System.out.println("tstcount5: "+stop_times.getRow(351083).getField(TRIP_ID));
        System.out.println("tstcount6: "+trips.getRow(14426).getField(TRIP_HEADSIGN));

        for (CsvRow row : stops.getRows()) {
            System.out.printf("stop: " + row.getField(STOP_NAME)+"\n");
        }
        System.out.printf("end stop: "+stops.getRow(1664).getField(STOP_NAME)+"\n");

        for (CsvRow row : shapes.getRows()) {
            System.out.println("shape_id: " + row.getField(SHAPE_ID));
        }
        System.out.printf("end shape_id: "+shapes.getRow(65854).getField(SHAPE_ID)+"\n");

        for (CsvRow row : routes.getRows()) {
            System.out.println("route_id: " + row.getField(ROUTE_ID));
        }
        System.out.printf("end route_id: "+routes.getRow(80).getField(ROUTE_ID)+"\n");

        for (CsvRow row : calendar.getRows()) {
            System.out.println("service_id: " + row.getField(SERVICE_ID));
        }
        System.out.printf("end service_id: "+calendar.getRow(152).getField(SERVICE_ID)+"\n");

        //        for (CsvRow row : stop_times.getRows()) {
//            System.out.println("trip_id: " + row.getField(TRIP_ID));
//        }
//        System.out.printf("end trip_id: "+stop_times.getRow(351083).getField(TRIP_ID)+"\n");

        for (CsvRow row : trips.getRows()) {
            System.out.println("trip_headsign: " + row.getField(TRIP_HEADSIGN));
        }
        System.out.printf("end trip_headsign: "+trips.getRow(14426).getField(TRIP_HEADSIGN)+"\n");

        System.out.flush();
    }

    private File getFile(int filenameId) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(cxt.getAssets().open(cxt.getString(filenameId)));
        File f = new File(cxt.getFilesDir().getPath().toString() + cxt.getString(filenameId));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        byte buffer[] = new byte[1024];
        int numberOfBytes;
        while ((numberOfBytes = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, numberOfBytes);
        }
        bis.close();
        bos.close();
        return f;
    }
}
