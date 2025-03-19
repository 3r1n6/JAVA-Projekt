import org.example.BookAnalysis;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class BookAnalysisTest {

    @Test
    void testGetWordCount() {
        BookAnalysis book = new BookAnalysis("1", "Test", "Das ist ein Test.");
        assertEquals(4, book.getWordCount()); // Positive Test

        BookAnalysis emptyBook = new BookAnalysis("2", "Empty", "");
        assertEquals(0, emptyBook.getWordCount()); // Negative Test (empty text)

        BookAnalysis whitespaceOnly = new BookAnalysis("3", "Whitespace", "     ");
        assertEquals(0, whitespaceOnly.getWordCount()); // Edge Case: Only whitespace
    }

    @Test
    void testGetMainWordCount() {
        BookAnalysis book = new BookAnalysis("1", "Test", "Das ist ein Buch und ein Mensch");
        assertEquals(3, book.getMainWordCount()); // Positive Test

        BookAnalysis stopWordsOnly = new BookAnalysis("2", "StopWords", "und oder der die das ein eine");
        assertEquals(0, stopWordsOnly.getMainWordCount()); // Negative Test (only stop words)

    }

    @Test
    void testGetMenschCount() {
        BookAnalysis book = new BookAnalysis("1", "Test", "Mensch und mensch sind wichtig");
        assertEquals(2, book.getMenschCount()); // Positive Test

        BookAnalysis noMensch = new BookAnalysis("2", "No Mensch", "Das ist ein Buch");
        assertEquals(0, noMensch.getMenschCount()); // Negative Test (no 'mensch' occurrences)

        BookAnalysis edgeCase = new BookAnalysis("3", "Edge Case", "unmenschlich und menschenleer");
        assertEquals(0, edgeCase.getMenschCount()); // Should not count partial words
    }

    @Test
    void testGetLongWords() {
        BookAnalysis book = new BookAnalysis("1", "Test", "Lebensversicherungsgesellschaft ist ein sehr langes Wort.");
        List<String> expected = List.of("Lebensversicherungsgesellschaft");
        assertEquals(expected, book.getLongWords()); // Positive Test

        BookAnalysis noLongWords = new BookAnalysis("2", "Short Words", "Das ist kurz.");
        assertTrue(noLongWords.getLongWords().isEmpty()); // Negative Test (no long words)

        BookAnalysis multipleLongWords = new BookAnalysis("3", "Multiple", "Donaudampfschifffahrtskapitän Vollholzhausbaumeister");
        List<String> expectedMultiple = List.of("Donaudampfschifffahrtskapitän", "Vollholzhausbaumeister");
        assertEquals(expectedMultiple, multipleLongWords.getLongWords()); // Test with multiple long words
    }
    @Test
    void testToString() {
        BookAnalysis book = new BookAnalysis("1", "Test Book", "Das ist ein Buch und ein Mensch.");
        String expectedOutput = "BookAnalysis{" +
                "id='1', title='Test Book', word_count=" + book.getWordCount() +
                ", main_word_count=" + book.getMainWordCount() +
                ", mensch_count=" + book.getMenschCount() +
                ", long_words=" + book.getLongWords() +
                '}';

        assertEquals(expectedOutput, book.toString()); // Test toString output
    }
}
