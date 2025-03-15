package org.example;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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

        List<String[]> books = Arrays.asList(
                new String[]{"1", "Book One", "Das ist ein Buch. Mensch ist wichtig."},
                new String[]{"2", "Book Two", "Ein weiteres Buch mit Menschen und Ideen."},
                new String[]{"3", "Book Three", "Das Leben eines Menschen ist bedeutend."}
        );

        // Executor Service with a fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit tasks
        List<Future<BookAnalysis>> futures = books.stream()
                .map(book -> new BookAnalysisTask(book[0], book[1], book[2]))
                .map(executor::submit)
                .toList();

        // Retrieve results
        for (Future<BookAnalysis> future : futures) {
            try {
                BookAnalysis analysis = future.get(); // Blocking call, waits for the result
                System.out.println(analysis);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown the executor
        executor.shutdown();
    }
}
