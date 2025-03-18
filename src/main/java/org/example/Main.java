package org.example;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

            System.out.println(jsonElement.toString());  // Optional: Print the entire JSON for inspection

            // Process each book from the JSON response
            JsonArray booksArray = jsonElement.getAsJsonObject().getAsJsonArray("books");
            List<BookAnalysis> analyses = new ArrayList<>();

            for (JsonElement bookElement : booksArray) {
                JsonObject book = bookElement.getAsJsonObject();
                String id = book.get("id").getAsString();
                String title = book.get("title").getAsString();
                String text = book.get("text").getAsString();

                // Create a BookAnalysis object for each book
                BookAnalysis analysis = new BookAnalysis(id, title, text);
                System.out.println(analysis);  // Print the analysis of each book

                // Add the analysis to the list
                analyses.add(analysis);
            }

            // Optionally, print all book analysis results
            System.out.println("All Book Analyses: ");
            for (BookAnalysis analysis : analyses) {
                System.out.println(analysis);
            }

            // After collecting all book analyses, write the results to CSV
            writeResultsToCSV(analyses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeResultsToCSV(List<BookAnalysis> analyses) {
        Path path = Paths.get("results.csv");

        try {
            // Ensure the file exists before writing
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            // Prepare CSV header and data
            List<String> lines = new ArrayList<>();
            lines.add("id\ttitle\tword_count\tmain_word_count\tmensch_count\tlong_words");  // Header

            for (BookAnalysis analysis : analyses) {
                // Debugging: Print what is being written
                System.out.println("Writing to CSV: " + analysis.getId() + " | " + analysis.getTitle());

                lines.add(analysis.getId() + "\t" +
                        analysis.getTitle() + "\t" +
                        analysis.getWordCount() + "\t" +
                        analysis.getMainWordCount() + "\t" +
                        analysis.getMenschCount() + "\t" +
                        String.join(", ", analysis.getLongWords()));  // Convert list to string
            }

            // Write to file using Java NIO
            Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Results successfully written to: " + path.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
