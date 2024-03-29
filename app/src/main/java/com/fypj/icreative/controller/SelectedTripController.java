package com.fypj.icreative.controller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.net.bsd.RExecClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SelectedTripController {

    public String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    /**
     * Receives a JSONObject and returns a list of lists containing latitude and longitude
     */
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        String startAddress = null;
        String endAddress = null;
        try {
            jRoutes = jObject.getJSONArray("routes");
            jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            List path = new ArrayList<>();
            /** Traversing all legs */
            for (int j = 0; j < jLegs.length(); j++) {
                startAddress = (String) ((JSONObject) jLegs.get(j)).get("start_address");
                endAddress = (String) ((JSONObject) jLegs.get(j)).get("end_address");

                jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                /** Traversing all steps */
                for (int k = 0; k < jSteps.length(); k++) {
                    String polyline = "";
                    polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                    List<LatLng> list = decodePoly(polyline);

                    /** Traversing all points */
                    for (int l = 0; l < list.size(); l++) {
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("lat", Double.toString((list.get(l)).latitude));
                        hm.put("lng", Double.toString((list.get(l)).longitude));
                        path.add(hm);
                    }
                }
                routes.add(path);
            }
            if (startAddress != null) {
                HashMap<String, String> startEndAddressHM = new HashMap<>();
                startEndAddressHM.put("startAddress", startAddress);
                startEndAddressHM.put("endAddress", endAddress);
                path.add(startEndAddressHM);
                routes.add(path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception ignore) {
        }


        return routes;
    }

    public List<String> getStartEndAddress(JSONObject jObject) {
        JSONArray jRoutes;
        JSONArray jLegs;
        List<String> startEndAddressList = Collections.emptyList();
        try {
            jRoutes = jObject.getJSONArray("routes");
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                for (int j = 0; j < jLegs.length(); j++) {
                    String startAddress = (String) ((JSONObject) jLegs.get(i)).get("start_address");
                    String endAddress = (String) ((JSONObject) jLegs.get(i)).get("end_address");
                    startEndAddressList.add(startAddress);
                    startEndAddressList.add(endAddress);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return startEndAddressList;
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
