package MapHelperClasses;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vasonomics on 22-11-2015.
 */
public class ParseJSON {

    private LatLng currentLocationLatLng;
    private LatLng destinationLatLng;

    private static final String TAG = ParseJSON.class.getSimpleName();

    private int distance;

    private static final String API_KEY = "AIzaSyBFrD3ebreJpnvAXKbvPAtpd8omYLmwsiI";

    public ParseJSON(LatLng mLastLocationLatLng, LatLng mDestinationLatLng) {
        Log.i(TAG, "Creating ParseJSON Object. Inside Constructor.");

        currentLocationLatLng = mLastLocationLatLng;
        destinationLatLng = mDestinationLatLng;

        double currentLatitude = currentLocationLatLng.latitude;
        double currentLongitude = currentLocationLatLng.longitude;

        Log.i(TAG, "Current LatLng is = "+mLastLocationLatLng);

        double destinationLatitude = destinationLatLng.latitude;
        double destinationLongitude = destinationLatLng.longitude;

        Log.i(TAG, "Destination LatLng is = "+mDestinationLatLng);
    }

    public String makeURL(){
        Log.i(TAG, "Reached makeURL");

        double currentLatitude = currentLocationLatLng.latitude;
        double currentLongitude = currentLocationLatLng.longitude;

        Log.i(TAG, "Current LatLng is = "+currentLatitude+", "+currentLongitude);

        double destinationLatitude = destinationLatLng.latitude;
        double destinationLongitude = destinationLatLng.longitude;

        Log.i(TAG, "Destination LatLng is = " + destinationLatitude + ", " + destinationLongitude);

        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json?");
        urlString.append("origin=");// from
        urlString.append(Double.toString(currentLatitude));
        urlString.append(",");
        urlString.append(Double.toString(currentLongitude));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destinationLatitude));
        urlString.append(",");
        urlString.append(Double.toString(destinationLongitude));
        //urlString.append("&mode=bicycling");
        urlString.append("&alternatives=true");
        urlString.append("&avoid=highways");
        urlString.append("&key=");
        urlString.append(API_KEY);
        Log.i(TAG, "URL = "+urlString.toString());
        return urlString.toString();
    }

    public ArrayList<Route> parse(String result){
        Log.i(TAG, "Reached parse");
        ArrayList<Route> routes = new ArrayList<>();

        if (result == null){
            Log.i(TAG, "Result is null");
            return null;
        }

        try{
            Log.i(TAG, "getting Json Object");
            final JSONObject json = new JSONObject(result);

            Log.i(TAG, "getting JsonRoutes");
            JSONArray jsonRoutes = json.getJSONArray("routes");

            Log.i(TAG, "JsonRoutes length = " + jsonRoutes.length());
            for (int i = 0; i < jsonRoutes.length(); i++){
                Log.i(TAG, "Getting Route");

                Route route = new Route();

                Segment segment = new Segment();

                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                //Get the bounds - northeast and southwest
                final JSONObject jsonBounds = jsonRoute.getJSONObject("bounds");
                final JSONObject jsonNortheast = jsonBounds.getJSONObject("northeast");
                final JSONObject jsonSouthwest = jsonBounds.getJSONObject("southwest");

                route.setLatLgnBounds(new LatLng(jsonNortheast.getDouble("lat"), jsonNortheast.getDouble("lng")), new LatLng(jsonSouthwest.getDouble("lat"), jsonSouthwest.getDouble("lng")));

                final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);
                final JSONArray steps = leg.getJSONArray("steps");
                final int numSteps = steps.length();

                route.setName(leg.getString("start_address") + " to " + leg.getString("end_address"));
                route.setCopyright(jsonRoute.getString("copyrights"));
                route.setDurationText(leg.getJSONObject("duration").getString("text"));
                route.setDurationValue(leg.getJSONObject("duration").getInt("value"));
                route.setDistanceText(leg.getJSONObject("distance").getString("text"));
                route.setDistanceValue(leg.getJSONObject("distance").getInt("value"));
                route.setEndAddressText(leg.getString("end_address"));
                route.setLength(leg.getJSONObject("distance").getInt("value"));

                if (!jsonRoute.getJSONArray("warnings").isNull(0)) {
                    route.setWarning(jsonRoute.getJSONArray("warnings").getString(0));
                }

                for (int y = 0; y < numSteps; y++) {
                    Log.i(TAG, "Getting steps");

                    final JSONObject step = steps.getJSONObject(y);
                    final JSONObject start = step.getJSONObject("start_location");
                    final LatLng position = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
                    segment.setPoint(position);
                    final int length = step.getJSONObject("distance").getInt("value");
                    distance += length;
                    segment.setLength(length);
                    segment.setDistance((double) distance / (double) 1000);
                    segment.setInstruction(step.getString("html_instructions").replaceAll("<(.*?)*>", ""));
                    route.addStartLocation(sendStartLocation(new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                            step.getJSONObject("start_location").getDouble("lng"))));
                    route.addEndLocation(sendEndLocation(new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                            step.getJSONObject("end_location").getDouble("lng"))));
                    route.addPoints(decodePolyLine(step.getJSONObject("polyline").getString("points")));
                    route.addSegment(segment.copy());
                    if (!step.has("maneuver")) {
                        route.setManeuver("Straight");
                    }
                    else {
                        route.setManeuver(step.getString("maneuver"));
                        Log.i(TAG, "Extracting maneuver: "+step.getString("maneuver"));
                    }
                }

                routes.add(route);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return routes;
    }

    private List<LatLng> decodePolyLine(final String poly) {
        Log.i(TAG, "Decoding polyline");

        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    private List<LatLng> sendStartLocation(LatLng startLocation){
        List<LatLng> extract = new ArrayList<LatLng>();
        extract.add(startLocation);
        return extract;
    }

    private List<LatLng> sendEndLocation(LatLng endLocation){
        List<LatLng> extract = new ArrayList<LatLng>();
        extract.add(endLocation);
        return extract;
    }
}
