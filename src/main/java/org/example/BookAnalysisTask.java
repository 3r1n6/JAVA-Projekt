package org.example;

import java.util.concurrent.Callable;

public class BookAnalysisTask implements Callable<BookAnalysis> {
    private final String id;
    private final String title;
    private final String text;

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
