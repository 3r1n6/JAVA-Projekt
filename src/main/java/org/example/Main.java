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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    // MySQL Database credentials (use your own)
    private static final String DB_URL = "jdbc:mysql://htl-projekt.com/2024_4ax_erindvora_ProjektSEW";  // Your database
    private static final String DB_USER = "erindvora";  // Your MySQL username
    private static final String DB_PASSWORD = "!Insy_2023$";  // Your MySQL password

    public static void main(String[] args) {
        List<BookAnalysis> analyses = new ArrayList<>();

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

            // Executor Service with a fixed thread pool for concurrent processing
            ExecutorService executor = Executors.newFixedThreadPool(3);
            List<Future<BookAnalysis>> futures = new ArrayList<>();

            for (JsonElement bookElement : booksArray) {
                JsonObject book = bookElement.getAsJsonObject();
                String id = book.get("id").getAsString();
                String title = book.get("title").getAsString();
                String text = book.get("text").getAsString();

                // Create a BookAnalysis object for each book and submit to the executor
                Future<BookAnalysis> future = executor.submit(() -> new BookAnalysis(id, title, text));
                futures.add(future);
            }

            // Wait for all futures to complete and collect the results
            for (Future<BookAnalysis> future : futures) {
                try {
                    BookAnalysis analysis = future.get(); // Blocking call, waits for the result
                    analyses.add(analysis);
                    System.out.println(analysis);  // Print the analysis of each book
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // Shutdown the executor
            executor.shutdown();

            // After collecting all book analyses, write the results to the CSV file and database
            writeResultsToCSV(analyses);
            saveResultsToDatabase(analyses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to save book analysis results to MySQL database
    private static void saveResultsToDatabase(List<BookAnalysis> analyses) {
        // SQL query to insert records into the results table
        String sqlInsert = "INSERT INTO results (book_id, title, word_count, main_word_count, mensch_count, long_words) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // JDBC code to connect to the MySQL database and execute the query
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {

            // Insert each book analysis result into the database
            for (BookAnalysis analysis : analyses) {
                pstmt.setString(1, analysis.getId());  // Book ID
                pstmt.setString(2, analysis.getTitle());  // Book Title
                pstmt.setInt(3, analysis.getWordCount());  // Word count
                pstmt.setInt(4, analysis.getMainWordCount());  // Main word count
                pstmt.setInt(5, analysis.getMenschCount());  // Mensch count
                pstmt.setString(6, String.join(", ", analysis.getLongWords()));  // Long words

                pstmt.executeUpdate();  // Execute the insert query
            }

            System.out.println("Results successfully saved to MySQL database!");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    // Method to write results to CSV
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
