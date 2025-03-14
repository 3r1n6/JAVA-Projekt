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

            JsonArray booksArray = jsonElement.getAsJsonObject().getAsJsonArray("books");
            for (JsonElement bookElement : booksArray) {
                JsonObject book = bookElement.getAsJsonObject();
                String id = book.get("id").getAsString();
                String title = book.get("title").getAsString();
                String text = book.get("text").getAsString();


                BookAnalysis analysis = new BookAnalysis(id, title, text);
                System.out.println(analysis);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
