package io.github.sekelenao.smallcsv.api;

import io.github.sekelenao.smallcsv.internal.Assertions;

/**
 * Configuration class for CSV files.
 * This class defines the delimiter and quote characters used in CSV syntax.
 */
public record CsvConfiguration(char delimiter, char quote) {

    /**
     * Predefined configuration using a semicolon as delimiter and double quotes.
     * This is the default configuration.
     */
    public static final CsvConfiguration SEMICOLON = new CsvConfiguration(';', '"');

    /**
     * Predefined configuration using a comma as delimiter and double quotes.
     */
    public static final CsvConfiguration COMMA = new CsvConfiguration(',', '"');

    /**
     * Constructs a new CSV configuration with the specified delimiter and quote characters.
     *
     * @param delimiter the character used to separate values
     * @param quote the character used to quote values
     *
     * @throws IllegalArgumentException if the delimiter and quote characters are the same
     */
    public CsvConfiguration {
        Assertions.isValidChar(quote);
        Assertions.isValidChar(delimiter);
        if (delimiter == quote) {
            throw new IllegalArgumentException("Delimiter should be different than quotes");
        }
    }

}
