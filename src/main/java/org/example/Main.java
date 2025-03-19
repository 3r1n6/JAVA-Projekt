package org.example;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Main {

    // Logger setup
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    // MySQL Database credentials (use your own)
    private static final String DB_URL = "jdbc:mysql://htl-projekt.com:33060/2024_4ax_erindvora_ProjektSEW";  // Your database
    private static final String DB_USER = "erindvora";  // Your MySQL username
    private static final String DB_PASSWORD = "!Insy_2023$";  // Your MySQL password

    public static void main(String[] args) {
        // Set logging level to FINE to capture all logs
        logger.setLevel(Level.FINE);

        // Log the start of the execution
        logger.info("Program started...");
        List<BookAnalysis> analyses = new ArrayList<>();
        long startTime = System.currentTimeMillis();  // Start measuring time

        try {
            logger.info("Connecting to the API...");
            // The connection to the link
            URL url = new URL("https://htl-assistant.vercel.app/api/projects/sew5");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the JSON Data from the link
            JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(connection.getInputStream()));
            System.out.println(jsonElement.toString());
            logger.fine("Received JSON: " + jsonElement.toString());

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
                logger.info("Processed Book: " + title);
            }

            // Wait for all futures to complete and collect the results
            for (Future<BookAnalysis> future : futures) {
                try {
                    BookAnalysis analysis = future.get(); // Blocking call, waits for the result
                    analyses.add(analysis);
                    logger.fine("Book analysis completed: " + analysis);  // Log detailed analysis
                } catch (InterruptedException | ExecutionException e) {
                    logger.severe("Error while processing book: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Shutdown the executor
            executor.shutdown();

            // After collecting all book analyses, write the results to the CSV file and database
            writeResultsToCSV(analyses);
            saveResultsToDatabase(analyses);

            // Get the UUID and execution duration (for POST request)
            String uuid = "2149b423-487a-4f56-bc91-a1495c0d0f08";  // Replace with actual UUID
            long endTime = System.currentTimeMillis();  // End measuring time
            long duration = endTime - startTime;  // Calculate the duration in milliseconds
            logger.info("Total execution time: " + duration + " milliseconds");

            // Send results as a POST request
            sendResultsToAPI(uuid, duration, analyses);

        } catch (Exception e) {
            logger.severe("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to save book analysis results to MySQL database
    private static void saveResultsToDatabase(List<BookAnalysis> analyses) {
        // SQL query to insert records into the results table
        String sqlInsert = "INSERT INTO results (book_id, title, word_count, main_word_count, mensch_count, long_words) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

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

                int rowsAffected = pstmt.executeUpdate();  // Execute the insert query
                if (rowsAffected > 0) {
                    logger.info("Inserted Book ID: " + analysis.getId() + " into the database.");
                } else {
                    logger.warning("Failed to insert Book ID: " + analysis.getId());
                }
            }

            logger.info("Results successfully saved to MySQL database!");

        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Method to write results to CSV
    private static void writeResultsToCSV(List<BookAnalysis> analyses) {
        Path path = Paths.get("results.csv");

        try {
            // Ensure the file exists before writing
            if (Files.notExists(path)) {
                Files.createFile(path);
                logger.info("Created new CSV file: " + path.toAbsolutePath());
            }

            // Prepare CSV header and data
            List<String> lines = new ArrayList<>();
            lines.add("id\ttitle\tword_count\tmain_word_count\tmensch_count\tlong_words");  // Header

            for (BookAnalysis analysis : analyses) {
                logger.fine("Writing to CSV: " + analysis.getId() + " | " + analysis.getTitle());

                lines.add(analysis.getId() + "\t" +
                        analysis.getTitle() + "\t" +
                        analysis.getWordCount() + "\t" +
                        analysis.getMainWordCount() + "\t" +
                        analysis.getMenschCount() + "\t" +
                        String.join(", ", analysis.getLongWords()));  // Convert list to string
            }

            // Write to file using Java NIO
            Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Results successfully written to: " + path.toAbsolutePath());

        } catch (IOException e) {
            logger.severe("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to send results as a POST request to the API with Basic Authentication
    private static void sendResultsToAPI(String uuid, long duration, List<BookAnalysis> analyses) {
        try {
            // Prepare the POST request URL
            URL url = new URL("https://htl-assistant.vercel.app/api/projects/sew5");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // Basic Authentication setup: "student:supersecret"
            String auth = "Basic " + new String(Base64.getEncoder().encode("student:supersecret".getBytes()));
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);

            // Create JSON body
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("uuid", uuid);
            resultObject.addProperty("duration", duration);
            resultObject.addProperty("name", "Erind Vora");  // Replace with actual name
            resultObject.addProperty("url", "https://github.com/3r1n6/JAVA-Projekt");  // Replace with your GitHub project URL

            // Prepare the results array
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

            // Add results to the body
            resultObject.add("results", resultsArray);

            // Send the POST request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = resultObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Log the response code
            int responseCode = connection.getResponseCode();
            logger.info("POST request sent. Response code: " + responseCode);

            // Read the response body
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                logger.warning("Failed to read the response body. Possible issue: " + e.getMessage());
                // Read the error stream if available
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        logger.severe("Error from API: " + errorLine);
                    }
                }
            }

            // Log the response body (whether successful or error)
            if (response.length() > 0) {
                logger.info("Response from POST request: " + response.toString());
            }

            // Check if the response code indicates success (200 or 201)
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                logger.info("POST request sent successfully!");
            } else {
                logger.warning("Failed to send POST request. Response code: " + responseCode);
            }

        } catch (IOException e) {
            logger.severe("Error sending POST request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
