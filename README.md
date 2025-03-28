﻿# JAVA-Projekt

## Code Coverage for BookAnalysis
- **Class Coverage:** 100% (1/1)
- **Method Coverage:** 100% (7/7)
- **Line Coverage:** 96.6% (28/29)


[![Java CI](https://github.com/3r1n6/JAVA-Projekt/actions/workflows/ci.yml/badge.svg)](https://github.com/3r1n6/JAVA-Projekt/actions/workflows/ci.yml)

# Book Analysis Project

## Overview

The **Book Analysis Project** is a Java-based program that fetches book data from a remote API, processes the data (word count, main word count, Mensch count, and long words), and then stores the results in a MySQL database and a CSV file. The program also sends the results as a POST request to an external API with **Basic Authentication**.

## Features

- **Fetches book data** from a remote API.
- **Analyzes books' text**:
    - Total word count
    - Main word count (excluding stop words)
    - Count of the word "Mensch"
    - Long words (more than 18 characters)
- **Stores the results**:
    - Saves results to a MySQL database.
    - Saves results to a CSV file.
- **Sends results via a POST request** to an external API with Basic Authentication.



## Book Analysis Project

 - This Book Analysis Project is a Java program designed to process and analyze book data fetched from an external API. The program performs several tasks such as analyzing word counts, counting the occurrences of the word "Mensch," identifying long words, and writing the results to both a MySQL database and a CSV file. Additionally, it sends the results via a POST request to an external API for further processing. Features of the Program
The program performs the following tasks:

* Fetching Data: It fetches book data from an external API using a GET request.

##  **Text Analysis**:

* Word Count: Calculates the total number of words in the book's text.
* Main Word Count: Excludes common stop words from the word count to give the count of significant words.
* Mensch Count: Counts the number of times the word "Mensch" appears in the text.
* Long Words: Identifies words longer than 18 characters in the book’s text.

##  Saving Results:

* Saves the results of the analysis to a MySQL database.
* Saves the results to a CSV file for easy viewing.
* **Sending Results**: Sends the results as a POST request to an external API with Basic Authentication, along with the UUID and the duration of the program execution.
* git clone https://github.com/3r1n6/JAVA-Projekt.git

* **Database Setup**: Ensure you have MySQL installed and running. Create a database and a table with the following schema to store the results:


      `CREATE DATABASE 2024_4ax_erindvora_ProjektSEW;
      USE 2024_4ax_erindvora_ProjektSEW;
      
      CREATE TABLE results (
      id INT PRIMARY KEY AUTO_INCREMENT,
      book_id VARCHAR(50),
      title VARCHAR(255),
      word_count INT,
      main_word_count INT,
      mensch_count INT,
      long_words TEXT
      );`
  - **Configuration**: In the Main class, update the following variables with your database connection information:


    `private static final String DB_URL = "jdbc:mysql://your-database-url:3306/your-database";
    private static final String DB_USER = "your-username";
    private static final String DB_PASSWORD = "your-password";`
 


 - Fetch book data from the provided API.
Process each book's text using multiple threads for faster execution.
Save the analysis results to a MySQL database and CSV file.
Send the results via a POST request to an external API.

##   **Logging**

 - The program uses Java Util Logging to log various important actions, such as:

 - Connecting to the API
 - Processing books
 - Saving results to the database
 - Sending POST requests
 - Logs are printed at different levels (INFO, FINE, SEVERE) and are helpful for debugging and tracking the program's progress.

## Javadoc Documentation

The program is documented using Javadoc for clarity. If you want to generate the Javadoc for the entire project, you can run the following Maven command:




POST Request API
Once the analysis is complete, the program sends the results to an API at https://htl-assistant.vercel.app/api/projects/sew5. You need to include the UUID received from the GET request and the duration of the program's execution. The results are sent using Basic Authentication.

The program sends the following JSON body in the POST request:


    `{
    "uuid": "your-uuid",
    "duration": "execution-time-in-milliseconds",
    "name": "Your Name",
    "url": "Your GitHub URL",
    "results": [
    {
    "id": 1,
    "title": "Book Title",
    "word_count": 23,
    "main_word_count": 13,
    "mensch_count": 2,
    "long_words": "Donaudampfschifffahrtskapitän, Vollholzhausbaumeister"
    }
    ]
    }
## License
This project is licensed under the MIT License. For more details, refer to the LICENSE file.
