package org.example;

import com.google.gson.*;

import java.io.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    // Logger setup
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    // MySQL Database credentials (use your own)
    private static final String DB_URL = "jdbc:mysql://htl-projekt.com/2024_4ax_erindvora_ProjektSEW";  // Your database
    private static final String DB_USER = "erindvora";  // Your MySQL username
    private static final String DB_PASSWORD = "!Insy_2023$";  // Your MySQL password

    public static void main(String[] args) {
        // Set logging level to FINE to capture all logs
        logger.setLevel(Level.FINE);

        logger.info("Program started...");
        List<BookAnalysis> analyses = new ArrayList<>();
        long startTime = System.currentTimeMillis();  // Start measuring time
        String uuid = null;  // Store UUID

        try {
            logger.info("Connecting to the API...");
            // The connection to the link
            URL url = new URL("https://htl-assistant.vercel.app/api/projects/sew5");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the JSON Data from the link
            JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(connection.getInputStream()));

            // Extract UUID from JSON response
            uuid = jsonElement.getAsJsonObject().get("uuid").getAsString();
            logger.info("✅ Extracted UUID: " + uuid);

            JsonArray booksArray = jsonElement.getAsJsonObject().getAsJsonArray("books");

            // Multi-threaded processing
            ExecutorService executor = Executors.newFixedThreadPool(3);
            List<Future<BookAnalysis>> futures = new ArrayList<>();

            for (JsonElement bookElement : booksArray) {
                JsonObject book = bookElement.getAsJsonObject();
                String id = book.get("id").getAsString();
                String title = book.get("title").getAsString();
                String text = book.get("text").getAsString();

                Future<BookAnalysis> future = executor.submit(() -> new BookAnalysis(id, title, text));
                futures.add(future);
                logger.info("Processed Book: " + title);
            }

            //Collect results from threads
            for (Future<BookAnalysis> future : futures) {
                try {
                    BookAnalysis analysis = future.get(); // Blocking call, waits for the result
                    analyses.add(analysis);
                    logger.fine("Book analysis completed: " + analysis);
                } catch (InterruptedException | ExecutionException e) {
                    logger.severe("Error while processing book: " + e.getMessage());
                }
            }

            executor.shutdown(); // Shutdown threads

            // Save results to CSV & MySQL
            writeResultsToCSV(analyses);
            saveResultsToDatabase(analyses);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.info("Total execution time: " + duration + " milliseconds");

            // Send results to API
            if (uuid != null && !uuid.isEmpty()) {
                logger.info("Sending results to API...");
                sendResultsToAPI(uuid, duration, analyses);  // Call to send the POST request
            } else {
                logger.severe("UUID is NULL or EMPTY. Cannot send POST request.");
            }

            // Extract the UUID from the GET request JSON response
            uuid = jsonElement.getAsJsonObject().get("uuid").getAsString();
            logger.info("✅ Extracted UUID from GET request: " + uuid);


        } catch (Exception e) {
            logger.severe("An error occurred: " + e.getMessage());
        }
    }

    //Method to send results as a POST request to the API
    private static void sendResultsToAPI(String uuid, long duration, List<BookAnalysis> analyses) {
        try {
            logger.info("Preparing to send POST request...");

            URL url = new URL("https://htl-assistant.vercel.app/api/projects/sew5");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create JSON body
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("uuid", uuid);
            resultObject.addProperty("duration", duration);
            resultObject.addProperty("name", "Erind Vora");
            resultObject.addProperty("url", "https://github.com/your-repo"); // Replace with actual GitHub URL

            JsonArray resultsArray = new JsonArray();
            for (BookAnalysis analysis : analyses) {
                JsonObject bookResult = new JsonObject();
                bookResult.addProperty("id", analysis.getId());
                bookResult.addProperty("title", analysis.getTitle());
                bookResult.addProperty("word_count", analysis.getWordCount());
                bookResult.addProperty("main_word_count", analysis.getMainWordCount());
                bookResult.addProperty("mensch_count", analysis.getMenschCount());
                bookResult.addProperty("long_words", String.join(", ", analysis.getLongWords()));
                resultsArray.add(bookResult);
            }
            resultObject.add("results", resultsArray);

            //Send POST request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = resultObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Capture response
            int responseCode = connection.getResponseCode();
            logger.info("POST request sent. Response code: " + responseCode);

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                logger.warning("Failed to read response body: " + e.getMessage());
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        logger.severe("API Error Response: " + errorLine);
                    }
                }
            }

            //Log the response
            if (response.length() > 0) {
                logger.info("API Response: " + response.toString());
            } else {
                logger.warning("No response body received from API.");
            }

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                logger.info("POST request sent successfully!");
            } else {
                logger.warning("POST request failed. Response code: " + responseCode);
            }

        } catch (IOException e) {
            logger.severe("Error sending POST request: " + e.getMessage());
        }
    }

    //Method to save book analysis results to MySQL database
    private static void saveResultsToDatabase(List<BookAnalysis> analyses) {
        String sqlInsert = "INSERT INTO results (book_id, title, word_count, main_word_count, mensch_count, long_words) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {

            for (BookAnalysis analysis : analyses) {
                pstmt.setString(1, analysis.getId());
                pstmt.setString(2, analysis.getTitle());
                pstmt.setInt(3, analysis.getWordCount());
                pstmt.setInt(4, analysis.getMainWordCount());
                pstmt.setInt(5, analysis.getMenschCount());
                pstmt.setString(6, String.join(", ", analysis.getLongWords()));

                pstmt.executeUpdate();
                logger.fine("Inserted Book ID: " + analysis.getId() + " into the database.");
            }

            logger.info("Results successfully saved to MySQL database!");

        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
        }
    }

    //Method to write results to CSV
    private static void writeResultsToCSV(List<BookAnalysis> analyses) {
        Path path = Paths.get("results.csv");

        try {
            if (Files.notExists(path)) {
                Files.createFile(path);
                logger.info("Created new CSV file: " + path.toAbsolutePath());
            }

            List<String> lines = new ArrayList<>();
            lines.add("id\ttitle\tword_count\tmain_word_count\tmensch_count\tlong_words");

            for (BookAnalysis analysis : analyses) {
                logger.fine("Writing to CSV: " + analysis.getId() + " | " + analysis.getTitle());
                lines.add(analysis.getId() + "\t" +
                        analysis.getTitle() + "\t" +
                        analysis.getWordCount() + "\t" +
                        analysis.getMainWordCount() + "\t" +
                        analysis.getMenschCount() + "\t" +
                        String.join(", ", analysis.getLongWords()));
            }

            Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Results successfully written to: " + path.toAbsolutePath());

        } catch (IOException e) {
            logger.severe("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

