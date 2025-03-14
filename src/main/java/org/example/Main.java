package org.example;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Main {
    public static void main(String[] args) {
        try {
            // The connection to the link
            URL url = new URL("https://htl-assistant.vercel.app/api/projects/sew5");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the JSON Data from the link
            JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(connection.getInputStream()));


            System.out.println(jsonElement.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
