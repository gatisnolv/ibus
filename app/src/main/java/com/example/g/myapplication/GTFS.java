package com.example.g.myapplication;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;
import java.util.*;

import android.content.*;

import de.siegmar.fastcsv.reader.*;

//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;


public class GTFS {
    private static final int BUS = 3;
    private static final int TROLLEY = 800;
    private static final int TRAM = 900;

    private static final int STOP_ID = 0;
    private static final int STOP_NAME = 2;
    private static final int STOP_LAT = 4;
    private static final int STOP_LON = 5;

    private static final int SHAPE_ID = 0;
    private static final int SHAPE_PT_LAT = 1;
    private static final int SHAPE_PT_LON = 2;
    private static final int SHAPE_PT_SEQUENCE = 3;

    private static final int ROUTE_ID = 0;
    private static final int ROUTE_SHORT_NAME = 1;
    private static final int ROUTE_LONG_NAME = 2;
    private static final int ROUTE_TYPE = 4;

    private static final int CALENDAR_SERVICE_ID = 0;
    private static final int MONDAY = 1;
    private static final int TUESDAY = 2;
    private static final int WEDNESDAY = 3;
    private static final int THURSDAY = 4;
    private static final int FRIDAY = 5;
    private static final int SATURDAY = 6;
    private static final int SUNDAY = 7;

    private static final int STOP_TIMES_TRIP_ID = 0;
    private static final int ARRIVAL_TIME = 1;
    private static final int DEPARTURE_TIME = 2;
    private static final int STOP_TIMES_STOP_ID = 3;
    private static final int STOP_SEQUENCE = 4;

    private static final int TRIPS_ROUTE_ID = 0;
    private static final int SERVICE_ID = 1;
    private static final int TRIP_ID = 2;
    private static final int TRIP_HEADSIGN = 3;
    private static final int DIRECTION_ID = 4;
    private static final int TRIPS_SHAPE_ID = 6;


    private class Route implements Comparable<Route> {
        private static final String BUS4Z = "4z";
        private static final int GREATER = 1;
        private static final int LESS = -1;


        private String id;

        private String short_name;
        private String long_name;
        private int type;
        private List<Trip> trips;

        Route(String id, String short_name, String long_name, int type) {
            this.id = id;
            this.short_name = short_name;
            this.long_name = long_name;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getShortName() {
            return short_name;
        }

        public String getLongName() {
            return long_name;
        }

        public int getType() {
            return type;
        }

        public void addTrip(Trip trip) {
            trips.add(trip);
        }

        @Override
        public int compareTo(Route input) {
            if (this.short_name.equals(BUS4Z)) {
                if (input.short_name.equals(BUS4Z)) {//both are 4z
                    return 0;
                }
                if (Integer.parseInt(input.short_name) < 5) {
                    return GREATER;
                } else {//input.shortname >=5
                    return LESS;
                }
            } else if (input.short_name.equals(BUS4Z)) {
                if (Integer.parseInt(this.short_name) < 5) {
                    return LESS;
                } else {//this.shortname>=5
                    return GREATER;
                }
            } else {
                return this.id.compareTo(input.id);
            }
        }


    }

    private class Trip {
        private int tripId;
        private int serviceId;
        private String headsign;
        private boolean direction;
        private int shape_id;
        private List<Coordinate> shape;
        private boolean[] calendar;
        List<Stop> stops;


        public Trip(int tripId, int serviceId, String headsign, boolean direction, int shape_id) {
            this.tripId = tripId;
            this.serviceId = serviceId;
            this.headsign = headsign;
            this.direction = direction;
            this.shape_id = shape_id;
        }
    }

    private class Stop {
        private String id;
        private String name;
        private Coordinate coordinate;

        public Stop(String id, String name, Coordinate coordinate) {
            this.id = id;
            this.name = name;
            this.coordinate = coordinate;
        }
    }

    private class Coordinate {
        private float lat;
        private float lon;
    }

    //    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String BOM = "\uFEFF";//Addressing first column by name should be prepended with this, or access fields by index

    private CsvContainer stopsContainer;
    private CsvContainer shapesContainer;
    private CsvContainer routesContainer;
    private CsvContainer calendarContainer;
    private CsvContainer stop_timesContainer;
    private CsvContainer tripsContainer;

    private HashMap<String, Route> busses;
    private HashMap<String, Route> trolleys;
    private HashMap<String, Route> trams;
    private HashMap<String, Route> allTransports;

    private HashMap<Integer, Trip> shapeIdsToTrips;//TODO initialize maps

    private Context cxt;

    GTFS(Context context) {
        this.cxt = context;
        try {
            readGTFSFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        makeGTFSObjects();

    }

    private void readGTFSFile() throws IOException {
        ZipFile zipFile = new ZipFile(getFile(R.string.gtfs));
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry ze;
        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);

        while (entries.hasMoreElements()) {
            ze = entries.nextElement();
            if (ze.getName().equals(cxt.getString(R.string.stops))) {
                stopsContainer = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.shapes))) {
                shapesContainer = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.routes))) {
                routesContainer = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.calendar))) {
                calendarContainer = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.stop_times))) {
                stop_timesContainer = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            } else if (ze.getName().equals(cxt.getString(R.string.trips))) {
                tripsContainer = csvReader.read(new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze))));
            }
        }

    }

    private void readRoute(CsvRow input) {
        String routeId = input.getField(ROUTE_ID);
        String routeShortName = input.getField(ROUTE_SHORT_NAME);
        String routeLongName = input.getField(ROUTE_LONG_NAME);
        int routeType = Integer.parseInt(input.getField(ROUTE_TYPE));
        HashMap<String, Route> map;

        if (routeType == BUS) {
            map = busses;
        } else if (routeType == TROLLEY) {
            map = trolleys;
        } else {//TRAM
            map = trams;
        }
        Route route = new Route(routeId, routeShortName, routeLongName, routeType);
        map.put(routeId, route);
        allTransports.put(routeId, route);
    }

    private void readTrip(CsvRow input) {
        String routeId = input.getField(TRIPS_ROUTE_ID);
        int serviceId = Integer.parseInt(input.getField(SERVICE_ID));
        int tripId = Integer.parseInt(input.getField(TRIP_ID));
        String headsign = input.getField(TRIP_HEADSIGN);
        boolean direction = Boolean.parseBoolean(input.getField(DIRECTION_ID));
        int shapeId = Integer.parseInt(input.getField(TRIPS_SHAPE_ID));

        Trip trip = new Trip(tripId, serviceId, headsign, direction, shapeId);
        allTransports.get(routeId).addTrip(trip);
        shapeIdsToTrips.put(shapeId, trip);
    }

    private void readStop(CsvRow input) {

    }

    private void readShapePt(CsvRow input) {

    }

    private void readCalendar(CsvRow input) {

    }

    private void readStopTime(CsvRow input) {

    }

    private void getGTFS() {
        //TODO implement
        for (CsvRow route : routesContainer.getRows()) {
            readRoute(route);
        }
        for (CsvRow trip : tripsContainer.getRows()) {
            readTrip(trip);
        }
        for (CsvRow stop : stopsContainer.getRows()) {
            readStop(stop);
        }
        for (CsvRow shapePt : shapesContainer.getRows()) {
            readShapePt(shapePt);
        }
        for (CsvRow calendar : calendarContainer.getRows()) {
            readCalendar(calendar);
        }
        for (CsvRow stopTime : stop_timesContainer.getRows()) {
            readStopTime(stopTime);
        }

    }

    private void test() {
        //        zipFile.close();
//        System.out.println("tstcount: "+stopsContainer.getRowCount());
//        System.out.println("tstcount: "+shapesContainer.getRowCount());
//        System.out.println("tstcount: "+routesContainer.getRowCount());
//        System.out.println("tstcount: "+calendarContainer.getRowCount());
//        System.out.println("tstcount: "+stop_timesContainer.getRowCount());
//        System.out.println("tstcount: "+tripsContainer.getRowCount());

        System.out.println("tstcount1: " + stopsContainer.getRow(1664).getField(STOP_NAME));
        System.out.println("tstcount2: " + shapesContainer.getRow(65854).getField(SHAPE_ID));
        System.out.println("tstcount3: " + routesContainer.getRow(80).getField(ROUTE_ID));
//        System.out.println("tstcount4: " + calendarContainer.getRow(152).getField(SERVICE_ID));
        System.out.println("tstcount5: " + stop_timesContainer.getRow(351083).getField(TRIP_ID));
        System.out.println("tstcount6: " + tripsContainer.getRow(14426).getField(TRIP_HEADSIGN));

//        for (CsvRow row : stopsContainer.getRows()) {
////            System.out.printf("stop: " + row.getField(STOP_NAME)+"\n");
//            Log.d("stag", "stop: " + row.getField(STOP_NAME) + "\n");
//        }
        System.out.printf("end stop: " + stopsContainer.getRow(1664).getField(STOP_NAME) + "\n");
//        Log.d("stag", "end stop: " + stopsContainer.getRow(1664).getField(STOP_NAME) + "\n");
//        for (CsvRow row : shapesContainer.getRows()) {
//            System.out.println("shape_id: " + row.getField(SHAPE_ID));
//        }
        System.out.printf("end shape_id: " + shapesContainer.getRow(65854).getField(SHAPE_ID) + "\n");

        for (CsvRow row : routesContainer.getRows()) {
            System.out.println("route_id: " + row.getField(ROUTE_ID));
        }
        System.out.printf("end route_id: " + routesContainer.getRow(80).getField(ROUTE_ID) + "\n");

//        for (CsvRow row : calendarContainer.getRows()) {
//            System.out.println("service_id: " + row.getField(SERVICE_ID));
//        }
//        System.out.printf("end service_id: " + calendarContainer.getRow(152).getField(SERVICE_ID) + "\n");

        //        for (CsvRow row : stop_timesContainer.getRows()) {
//            System.out.println("trip_id: " + row.getField(TRIP_ID));
//        }
//        System.out.printf("end trip_id: "+stop_timesContainer.getRow(351083).getField(TRIP_ID)+"\n");

//        for (CsvRow row : tripsContainer.getRows()) {
//            System.out.println("trip_headsign: " + row.getField(TRIP_HEADSIGN));
//        }
        System.out.printf("end trip_headsign: " + tripsContainer.getRow(14426).getField(TRIP_HEADSIGN) + "\n");

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
