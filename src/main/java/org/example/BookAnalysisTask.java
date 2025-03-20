package org.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * The BookAnalysisTask class is responsible for performing the analysis of a single book.
 * It implements the {@link Callable} interface, which allows it to be executed in parallel threads
 * within an {@link ExecutorService}.
 *
 * This task is designed to create a {@link BookAnalysis} object containing the analysis results
 * of the provided book's ID, title, and text.
 *
 * @author Erind Vora
 * @version 1.4.0
 *
 */
public class BookAnalysisTask implements Callable<BookAnalysis> {
    private final String id;
    private final String title;
    private final String text;

    /**
     * Constructor to initialize the BookAnalysisTask with the book's ID, title, and text.
     *
     * @param id    The ID of the book
     * @param title The title of the book
     * @param text  The text content of the book
     */
    public BookAnalysisTask(String id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
    }

    @Override
    public BookAnalysis call() {
        return new BookAnalysis(id, title, text);
    }
}
