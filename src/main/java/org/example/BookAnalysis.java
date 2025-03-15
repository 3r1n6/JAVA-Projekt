package org.example;

import java.util.*;
import java.util.regex.*;

public class BookAnalysis {
    private String id, title, text;

    // Words that will not be counted
    private static final Set<String> STOP_WORDS = Set.of("und", "oder", "der", "die", "das", "ein", "eine");

    public BookAnalysis(String id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
    }

    //Find the total number of words (changed)
    public int getWordCount() {
        if (text == null || text.trim().isEmpty()) {
            return 0; // Fix: Make sure the null values are considered
        }
        return text.trim().split("\\s+").length;
    }


    //Find the total number of words not including the stop words (changed)
    public int getMainWordCount() {
        if (text == null || text.trim().isEmpty()) {
            return 0; // Ensure empty or whitespace-only text returns 0
        }

        return (int) Arrays.stream(text.trim().split("\\s+"))
                .map(word -> word.replaceAll("^[^a-zA-ZäöüÄÖÜß]+|[^a-zA-ZäöüÄÖÜß]+$", "")) // Remove other characters
                .map(String::toLowerCase)
                .filter(word -> !word.isEmpty())
                .filter(word -> !STOP_WORDS.contains(word)) // Fix: Exclude stop words correctly
                .count();

    }

    //Find the number of the word Mensch (changed)
    public int getMenschCount() {
        Matcher matcher = Pattern.compile("\\bmensch\\b", Pattern.CASE_INSENSITIVE).matcher(text); // Fix: Regex changed
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    //Find all the words longer than 18 characters
    public List<String> getLongWords() {
        List<String> longWords = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\b\\w{19,}\\b").matcher(text);
        while (matcher.find()) longWords.add(matcher.group());
        return longWords;
    }

    //Create a good looking format for the book's information
    @Override
    public String toString() {
        return "BookAnalysis{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", word_count=" + getWordCount() +
                ", main_word_count=" + getMainWordCount() +
                ", mensch_count=" + getMenschCount() +
                ", long_words=" + getLongWords() +
                '}';
    }
}
