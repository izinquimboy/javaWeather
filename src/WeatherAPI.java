import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class WeatherAPI {
    public static void main(String[] args) {
        try {
            Scanner scnr = new Scanner(System.in);
            String city;
            do {
                System.out.println("++++++++++++++++++++++++++++++++++++");
                System.out.print("Enter City(Say \"No\" to Quit): ");
                city = scnr.nextLine();

                if(city.equalsIgnoreCase("No")) {
                    break;
                }

                JSONObject cityLocationData = (JSONObject) getLocationData(city);
                double lat = (double) cityLocationData.get("latitude");
                double longi = (double) cityLocationData.get("longitude");

                displayWeatherData(lat, longi);
            } while (!city.equalsIgnoreCase("No"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getLocationData(String city) {
        city = city.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + 
                city + "&count=1&language=en&format=json";
        
        try {
            //fetch api response
            HttpURLConnection apiConnection = fetchApiResponse(urlString);


            //successsful connection = code 200
            if(apiConnection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // Read response and convert to string 
            String jsonResponse = readApiResponse(apiConnection);
            
            //Parse string to JSON object
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(jsonResponse);

            //Retreive location data 
            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            return (JSONObject) locationData.get(0);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void displayWeatherData(double lat, double longi) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + 
                "&longitude="+ longi + "&current=temperature_2m," +
                    "relative_humidity_2m&hourly=temperature_2m";

            HttpURLConnection apiConnection = fetchApiResponse(url);

            if(apiConnection.getResponseCode() != 200) {
                System.out.print("Error: Could not connect to API");
                return;
            }

            String jsonResponse = readApiResponse(apiConnection);

            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(jsonResponse);

            JSONObject weatherData = (JSONObject) resultsJsonObj.get("current");

            String time = (String) weatherData.get("time");
            System.out.println("Current time: " + time.substring(11));

            double temperature = (double) weatherData.get("temperature_2m");
            System.out.println("Current temperature (C): " + temperature);

            long humid = (long) weatherData.get("relative_humidity_2m");
            System.out.println("Current Relative Humidity: " + humid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            //attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //make request method get
            conn.setRequestMethod("GET");
            return conn;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //no connection found 
    }

    private static String readApiResponse(HttpURLConnection apiConnection) {
        try {
            // Create stringbuilder to store json data
            StringBuilder resultJson = new StringBuilder();

            //Scan inpustream of httpconnnection
            Scanner scnr = new Scanner(apiConnection.getInputStream());
            
            //loop through each token and append to string builder
            while(scnr.hasNext()) {
                resultJson.append(scnr.nextLine());
            }

            scnr.close();
            
            //return resulting json as a string 
            return resultJson.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}