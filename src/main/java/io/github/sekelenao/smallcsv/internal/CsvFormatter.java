package io.github.sekelenao.smallcsv.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

import io.github.sekelenao.smallcsv.api.Csv;
import io.github.sekelenao.smallcsv.api.CsvConfiguration;
import io.github.sekelenao.smallcsv.api.Row;
import io.github.sekelenao.smallcsv.api.exception.CsvParsingException;

public final class CsvFormatter {

    private static final class CsvBuffer {

        private final Csv csv;

        private Row row;

        private StringBuilder cell;

        private CsvBuffer() {
            this.csv = Csv.empty();
            this.row = Row.empty();
            this.cell = new StringBuilder();
        }

        private void appendToCell(char c) {
            cell.append(c);
        }

        private void pushCell() {
            row.add(cell.toString());
            cell = new StringBuilder();
        }

        private void pushRow(){
            csv.addLast(row);
            row = Row.empty();
        }

        private boolean notEmpty() {
            return !cell.isEmpty();
        }

    }

    private enum QuoteState {ENCOUNTERED, IN, OUT}

    private final char quote;
    private final char delimiter;
    private QuoteState quoteState = QuoteState.OUT;

    public CsvFormatter(CsvConfiguration configuration) {
        Objects.requireNonNull(configuration);
        this.quote = configuration.quote();
        this.delimiter = configuration.delimiter();
    }

    private void treatDelimiter(CsvBuffer buffer) {
        switch (quoteState) {
            case OUT -> buffer.pushCell();
            case IN -> buffer.appendToCell(delimiter);
            case ENCOUNTERED -> {
                buffer.pushCell();
                quoteState = QuoteState.OUT;
            }
        }
    }

    private void treatQuote(CsvBuffer buffer, String text) {
        switch (quoteState) {
            case OUT -> {
                if (buffer.notEmpty()) throw new CsvParsingException(text);
                quoteState = QuoteState.IN;
            }
            case IN -> quoteState = QuoteState.ENCOUNTERED;
            case ENCOUNTERED -> {
                buffer.appendToCell(quote);
                quoteState = QuoteState.IN;
            }
        }
    }

    private void treatChar(CsvBuffer buffer, char c, String text){
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(text);
        switch (quoteState){
            case OUT -> {
                Assertions.isValidChar(c);
                buffer.appendToCell(c);
            }
            case IN -> buffer.appendToCell(c);
            case ENCOUNTERED -> throw new CsvParsingException(text);
        }
    }

    public Csv split(Iterable<String> lines){
        Objects.requireNonNull(lines);
        quoteState = QuoteState.OUT;
        var buffer = new CsvBuffer();
        for(var line : lines){
            var chars = line.toCharArray();
            for (char c : chars) {
                if (c == quote) treatQuote(buffer, line);
                else if (c == delimiter) treatDelimiter(buffer);
                else treatChar(buffer, c, line);
            }
            if(quoteState != QuoteState.IN){
                buffer.pushCell();
                buffer.pushRow();
                quoteState = QuoteState.OUT;
            } else {
                buffer.appendToCell('\n');
            }
        }
        if (quoteState == QuoteState.IN)
            throw new CsvParsingException(buffer.row.toString());
        return buffer.csv;
    }

    public Csv split(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);
        quoteState = QuoteState.OUT;
        var buffer = new CsvBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            var chars = line.toCharArray();
            for (char c : chars) {
                if (c == quote) treatQuote(buffer, line);
                else if (c == delimiter) treatDelimiter(buffer);
                else treatChar(buffer, c, line);
            }
            if (quoteState != QuoteState.IN) {
                buffer.pushCell();
                buffer.pushRow();
                quoteState = QuoteState.OUT;
            } else {
                buffer.appendToCell('\n');
            }
        }
        if (quoteState == QuoteState.IN)
            throw new CsvParsingException(buffer.row.toString());
        return buffer.csv;
    }

    public static boolean isEscapedChar(char character) {
        return switch (character) {
            case '\n', '\r', '\b', '\f', '\0'-> true;
            default -> false;
        };
    }

    private String formatString(String value) {
        Objects.requireNonNull(value);
        var needQuotes = false;
        var formatted = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c == quote) {
                needQuotes = true;
                formatted.append(quote).append(quote);
                continue;
            } else if (c == delimiter || isEscapedChar(c)) {
                needQuotes = true;
            }
            formatted.append(c);
        }
        if(needQuotes) return quote + formatted.toString() + quote;
        return formatted.toString();
    }

    public String toCsvString(Iterable<String> values) {
        var csvString = new StringBuilder();
        var joiner = "";
        for (var value : values) {
            csvString.append(joiner).append(formatString(value));
            joiner = String.valueOf(delimiter);
        }
        return csvString.toString();
    }

}
