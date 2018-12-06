package com.example.g.ibus;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

        public List<Trip> getTrips() {
            return trips;
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
        private Route route; //routeId
        private int tripId;
        private int serviceId;
        private String headsign;
        private boolean direction;
        private String shape_id;
        private List<Coordinate> shape;
        private boolean[] calendar;
        List<Stop> stops;


        public Trip(int tripId, int serviceId, String headsign, boolean direction, String shape_id) {
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
        private List<Route> routesWhoseTripsHaveThisStop;
//        private List<Trip> tripsWithThisStop;//or maybe map stop to route - not sure yet

        public Stop(String id, String name, Coordinate coordinate) {
            this.id = id;
            this.name = name;
            this.coordinate = coordinate;
        }

        public void addRoute(Route input) {
            routesWhoseTripsHaveThisStop.add(input);
        }

        public List<Route> getRoutes() {
            return routesWhoseTripsHaveThisStop;
        }
    }

    private class Coordinate {
        private static final int NO_SEQUENCE = 0;
        private float lat;
        private float lon;
        private int seqNr;

        Coordinate(float lat, float lon) {
            this(lat, lon, NO_SEQUENCE);
        }

        Coordinate(float lat, float lon, int seqNr) {
            this.lat = lat;
            this.lon = lon;
            this.seqNr = seqNr;
        }


        public float getLat() {
            return lat;
        }

        public float getLon() {
            return lon;
        }

        public double getDifference(Coordinate input) {
            float absLatDiff = Math.abs(this.lat - input.lat);
            float absLonDiff = Math.abs(this.lon - input.lon);

            return Math.sqrt(Math.pow(absLatDiff, 2) + Math.pow(absLonDiff, 2));//TODO implement
        }
    }

    private static final String BOM = "\uFEFF";//Addressing first column by name should be prepended with this, or alternatively access fields by index

    private CsvContainer stopsContainer;
    private CsvContainer shapesContainer;
    private CsvContainer routesContainer;
    private CsvContainer calendarContainer;
    private CsvContainer stop_timesContainer;
    private CsvContainer tripsContainer;
    private Context cxt;

    private HashMap<String, Route> routeIdsToBusRoutes;////TODO initialize hashmaps
    private HashMap<String, Route> routeIdsToTrolleyRoutes;
    private HashMap<String, Route> routeIdsToTramRoutes;
    private HashMap<String, Route> routeIdsToAllRoutes;

//    private HashMap<Integer, Trip> tripIdsToTrips;//likely not needed

    private HashMap<String, Stop> stopIdsToStops;
    private HashMap<Integer, boolean[]> serviceIdsToCalendars;
    private HashMap<String, List<Coordinate>> shapeIdsToShapes;


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
            map = routeIdsToBusRoutes;
        } else if (routeType == TROLLEY) {
            map = routeIdsToTrolleyRoutes;
        } else {//TRAM
            map = routeIdsToTramRoutes;
        }
        Route route = new Route(routeId, routeShortName, routeLongName, routeType);
        map.put(routeId, route);
        routeIdsToAllRoutes.put(routeId, route);
    }

    private void readTrip(CsvRow input) {
        String routeId = input.getField(TRIPS_ROUTE_ID);
        int serviceId = Integer.parseInt(input.getField(SERVICE_ID));
        int tripId = Integer.parseInt(input.getField(TRIP_ID));
        String headsign = input.getField(TRIP_HEADSIGN);
        boolean direction = Boolean.parseBoolean(input.getField(DIRECTION_ID));
        String shapeId = input.getField(TRIPS_SHAPE_ID);

        Trip trip = new Trip(tripId, serviceId, headsign, direction, shapeId);
        routeIdsToAllRoutes.get(routeId).addTrip(trip);
    }

    private void readStop(CsvRow input) {
        String id = input.getField(STOP_ID);
        String name = input.getField(STOP_NAME);
        float stopLat = Float.parseFloat(input.getField(STOP_LAT));
        float stopLon = Float.parseFloat(input.getField(STOP_LON));

        Coordinate coordinate = new Coordinate(stopLat, stopLon);
        Stop stop = new Stop(id, name, coordinate);
        stopIdsToStops.put(id, stop);
    }

    private void readCalendar(CsvRow input) {
        int serviceId = Integer.parseInt(input.getField(CALENDAR_SERVICE_ID));
        boolean monday = Boolean.parseBoolean(input.getField(MONDAY));
        boolean tuesday = Boolean.parseBoolean(input.getField(TUESDAY));
        boolean wednesday = Boolean.parseBoolean(input.getField(WEDNESDAY));
        boolean thursday = Boolean.parseBoolean(input.getField(THURSDAY));
        boolean friday = Boolean.parseBoolean(input.getField(FRIDAY));
        boolean saturday = Boolean.parseBoolean(input.getField(SATURDAY));
        boolean sunday = Boolean.parseBoolean(input.getField(SUNDAY));
        boolean[] calendar = {monday, tuesday, wednesday, thursday, friday, saturday, sunday};

        serviceIdsToCalendars.put(serviceId, calendar);
    }


    //    private void readStopTime(CsvRow input) {
//        int tripId = Integer.parseInt(input.getField(STOP_TIMES_TRIP_ID));
//        LocalTime arrivalTime = LocalTime.parse(input.getField(ARRIVAL_TIME));
//        LocalTime departureTime = LocalTime.parse(input.getField(DEPARTURE_TIME));
//        String stopId = input.getField(STOP_TIMES_STOP_ID);
//        int stopSequence = Integer.parseInt(input.getField(STOP_SEQUENCE));
//
//
//    }

    //    private void readShapePt(CsvRow input) {
//        String shapeId = input.getField(SHAPE_ID);
//        float shapePtLat = Float.parseFloat(input.getField(SHAPE_PT_LAT));
//        float shapePtLon = Float.parseFloat(input.getField(SHAPE_PT_LON));
//        int shapePtSeq = Integer.parseInt(input.getField(SHAPE_PT_SEQUENCE));
//
//        Coordinate shapePt = new Coordinate(shapePtLat, shapePtLon, shapePtSeq);
//
//        //process whole shape and only then add to shapeIdsToShapes map, so not here where only one point is processed, but higher up (method call wise)
//    }
    private void readShapes() {
//TODO implement

//        for (CsvRow shapePt : shapesContainer.getRows()) {
//            readShapePt(shapePt);
//        }

    }

    private void readStopTimes() {
//TODO implement
//        for (CsvRow stopTime : stop_timesContainer.getRows()) {
//            readStopTime(stopTime);
//        }

    }

    private void getGTFS() {
        for (CsvRow route : routesContainer.getRows()) {
            readRoute(route);
        }
        for (CsvRow stop : stopsContainer.getRows()) {
            readStop(stop);
        }
        for (CsvRow calendar : calendarContainer.getRows()) {
            readCalendar(calendar);
        }
        readShapes();
        readStopTimes();
        for (CsvRow trip : tripsContainer.getRows()) {
            readTrip(trip);
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