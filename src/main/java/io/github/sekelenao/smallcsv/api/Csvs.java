package io.github.sekelenao.smallcsv.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.github.sekelenao.smallcsv.internal.Assertions;
import io.github.sekelenao.smallcsv.internal.CsvFormatter;

/**
 * Utility and factory class for CSV operations.
 * This class provides static factory methods to read CSV data into {@link Csv} instances,
 * and static methods to export Java records to CSV files.
 */
public final class Csvs {

    /**
     * Private constructor to prevent instantiation of the Csvs class.
     *
     * @throws AssertionError always thrown to indicate that instantiation is not allowed
     */
    private Csvs() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    /**
     * Reads the rows from the specified file using the given configuration and charset, and returns a Csv instance.
     *
     * @param path the path to the file
     * @param config the configuration to use for parsing
     * @param charset the charset to use for reading the file
     * @return a Csv instance containing the rows read from the file
     * @throws IOException if an I/O error occurs while reading the file
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(Path path, CsvConfiguration config, Charset charset) throws IOException {
        Assertions.requireNonNulls(path, config, charset);
        try (var reader = Files.newBufferedReader(path, charset)) {
            return from(reader, config);
        }
    }

    /**
     * Reads the rows from the specified input stream using the given configuration and charset, and returns a Csv instance.
     *
     * @param in the input stream to read from
     * @param config the configuration to use for parsing
     * @param charset the charset to use for reading the stream
     * @return a Csv instance containing the rows read from the stream
     * @throws IOException if an I/O error occurs while reading the stream
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(InputStream in, CsvConfiguration config, Charset charset) throws IOException {
        Assertions.requireNonNulls(in, config, charset);
        try (var reader = new BufferedReader(new InputStreamReader(in, charset))) {
            return from(reader, config);
        }
    }

    /**
     * Reads the rows from the specified input stream using the given configuration and the default charset, and returns a Csv instance.
     *
     * @param in the input stream to read from
     * @param config the configuration to use for parsing
     * @return a Csv instance containing the rows read from the stream
     * @throws IOException if an I/O error occurs while reading the stream
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(InputStream in, CsvConfiguration config) throws IOException {
        Assertions.requireNonNulls(in, config);
        return from(in, config, Charset.defaultCharset());
    }

    /**
     * Reads the rows from the specified input stream using the default configuration and the given charset, and returns a Csv instance.
     *
     * @param in the input stream to read from
     * @param charset the charset to use for reading the stream
     * @return a Csv instance containing the rows read from the stream
     * @throws IOException if an I/O error occurs while reading the stream
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(InputStream in, Charset charset) throws IOException {
        Assertions.requireNonNulls(in, charset);
        return from(in, CsvConfiguration.SEMICOLON, charset);
    }

    /**
     * Reads the rows from the specified input stream using the default configuration and the default charset, and returns a Csv instance.
     *
     * @param in the input stream to read from
     * @return a Csv instance containing the rows read from the stream
     * @throws IOException if an I/O error occurs while reading the stream
     * @throws NullPointerException if the specified stream is null
     */
    public static Csv from(InputStream in) throws IOException {
        return from(Objects.requireNonNull(in), CsvConfiguration.SEMICOLON);
    }

    /**
     * Reads the rows from the specified buffered reader using the given configuration, and returns a Csv instance.
     *
     * @param reader the buffered reader to read from
     * @param config the configuration to use for parsing
     * @return a Csv instance containing the rows read from the reader
     * @throws IOException if an I/O error occurs while reading the reader
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(BufferedReader reader, CsvConfiguration config) throws IOException {
        Assertions.requireNonNulls(reader, config);
        var formatter = new CsvFormatter(config);
        return formatter.split(reader);
    }

    /**
     * Reads the rows from the specified buffered reader using the default configuration, and returns a Csv instance.
     *
     * @param reader the buffered reader to read from
     * @return a Csv instance containing the rows read from the reader
     * @throws IOException if an I/O error occurs while reading the reader
     * @throws NullPointerException if the specified reader is null
     */
    public static Csv from(BufferedReader reader) throws IOException {
        return from(Objects.requireNonNull(reader), CsvConfiguration.SEMICOLON);
    }

    /**
     * Reads the rows from the specified file using the given configuration and the default charset, and returns a Csv instance.
     *
     * @param path the path to the file
     * @param config the configuration to use for parsing
     * @return a Csv instance containing the rows read from the file
     * @throws IOException if an I/O error occurs while reading the file
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(Path path, CsvConfiguration config) throws IOException {
        Assertions.requireNonNulls(path, config);
        return from(path, config, Charset.defaultCharset());
    }

    /**
     * Reads the rows from the specified file using the default configuration and the given charset, and returns a Csv instance.
     *
     * @param path the path to the file
     * @param charset the charset to use for reading the file
     * @return a Csv instance containing the rows read from the file
     * @throws IOException if an I/O error occurs while reading the file
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(Path path, Charset charset) throws IOException {
        Assertions.requireNonNulls(path, charset);
        return from(path, CsvConfiguration.SEMICOLON, charset);
    }

    /**
     * Reads the rows from the specified file using the default configuration and the default charset, and returns a Csv instance.
     *
     * @param path the path to the file
     * @return a Csv instance containing the rows read from the file
     * @throws IOException if an I/O error occurs while reading the file
     * @throws NullPointerException if the specified path is null
     */
    public static Csv from(Path path) throws IOException {
        return from(Objects.requireNonNull(path), CsvConfiguration.SEMICOLON);
    }

    /**
     * Parses the text provided by the given iterable using the specified configuration, and returns a Csv instance.
     *
     * @param text the iterable providing the text to parse
     * @param config the configuration to use for parsing
     * @return a Csv instance containing the parsed rows
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static Csv from(Iterable<String> text, CsvConfiguration config) {
        Assertions.requireNonNulls(text, config);
        var formatter = new CsvFormatter(config);
        return formatter.split(text);
    }

    /**
     * Parses the text provided by the given iterable using the default configuration, and returns a Csv instance.
     *
     * @param text the iterable providing the text to parse
     * @return a Csv instance containing the parsed rows
     * @throws NullPointerException if the specified text is null
     */
    public static Csv from(Iterable<String> text) {
        return from(Objects.requireNonNull(text), CsvConfiguration.SEMICOLON);
    }

    /**
     * Invokes the specified accessor method on the given record instance in a secure manner.
     *
     * @param instance the record instance on which to invoke the accessor method
     * @param accessor the accessor method to invoke
     * @return the result of invoking the accessor method on the record instance
     * @throws IllegalAccessError if illegal access occurs while invoking the accessor method
     * @throws RuntimeException if the invocation target exception contains a runtime exception
     * @throws Error if the invocation target exception contains an error
     * @throws UndeclaredThrowableException if an undeclared throwable exception occurs during the invocation
     */
    private static Object secureInvoke(Record instance, Method accessor) {
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError();
        } catch (InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns a function that converts the specified record component to a string representation.
     *
     * @param component the record component for which to create the conversion function
     * @return a function that converts the specified record component to a string
     */
    private static Function<Record, String> componentToString(RecordComponent component){
        return instance -> String.valueOf(secureInvoke(instance, component.getAccessor()));
    }

    /**
     * Class-value that maintains a cache of conversion functions for record components annotated with CsvColumn.
     */
    private static final ClassValue<List<Function<Record, String>>> CACHE = new ClassValue<>() {

        @Override
        protected List<Function<Record, String>> computeValue(Class<?> type) {
            Objects.requireNonNull(type);
            return Arrays.stream(type.getRecordComponents())
                    .filter(rc -> rc.isAnnotationPresent(CsvColumn.class))
                    .map(Csvs::componentToString)
                    .toList();
        }

    };

    /**
     * Exports the provided records to a CSV file at the specified path using the given configuration.
     *
     * @param path the path to the CSV file to export the records to
     * @param records the iterable of records to export
     * @param config the CSV configuration to use for formatting the exported data
     * @param options the open options specifying how the file is opened
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void export(Path path, Iterable<? extends Record> records, CsvConfiguration config, OpenOption... options) throws IOException {
        Assertions.requireNonNulls(path, records, config, options);
        var formatter = new CsvFormatter(config);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)) {
            for (var rcd : records) {
                var values = CACHE.get(rcd.getClass()).stream()
                        .map(f -> f.apply(rcd))
                        .toList();
                writer.write(formatter.toCsvString(values));
                writer.newLine();
            }
        }
    }

    /**
     * Exports the provided records to a CSV file at the specified path using the default configuration.
     *
     * @param path the path to the CSV file to export the records to
     * @param records the iterable of records to export
     * @param options the open options specifying how the file is opened
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void export(Path path, Iterable<? extends Record> records, OpenOption... options) throws IOException {
        Assertions.requireNonNulls(path, records, options);
        export(path, records, CsvConfiguration.SEMICOLON, options);
    }

    /**
     * Exports the rows of the specified Csv instance to a file at the given path using the given charset and open options.
     *
     * @param path the path to the file
     * @param csv the Csv instance to export
     * @param charset the charset to use for writing the file
     * @param openOptions the options specifying how the file is opened
     * @throws IOException if an I/O error occurs while writing the file
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static void export(Path path, Csv csv, Charset charset, OpenOption... openOptions) throws IOException {
        Assertions.requireNonNulls(path, csv, charset, openOptions);
        var formatter = new CsvFormatter(csv.configuration());
        try (var writer = Files.newBufferedWriter(path, charset, openOptions)) {
            for (var row : csv) {
                writer.write(formatter.toCsvString(row));
                writer.newLine();
            }
        }
    }

    /**
     * Exports the rows of the specified Csv instance to a file at the given path using the default charset and the given open options.
     *
     * @param path the path to the file
     * @param csv the Csv instance to export
     * @param openOptions the options specifying how the file is opened
     * @throws IOException if an I/O error occurs while writing the file
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static void export(Path path, Csv csv, OpenOption... openOptions) throws IOException {
        Assertions.requireNonNulls(path, csv, openOptions);
        export(path, csv, Charset.defaultCharset(), openOptions);
    }

    /**
     * Exports the rows of the specified Csv instance to the given output stream using the given charset.
     *
     * @param out the output stream to write to
     * @param csv the Csv instance to export
     * @param charset the charset to use for writing
     * @throws IOException if an I/O error occurs while writing
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static void export(OutputStream out, Csv csv, Charset charset) throws IOException {
        Assertions.requireNonNulls(out, csv, charset);
        var formatter = new CsvFormatter(csv.configuration());
        try (var writer = new BufferedWriter(new OutputStreamWriter(out, charset))) {
            for (var row : csv) {
                writer.write(formatter.toCsvString(row));
                writer.newLine();
            }
        }
    }

    /**
     * Exports the rows of the specified Csv instance to the given output stream using the default charset.
     *
     * @param out the output stream to write to
     * @param csv the Csv instance to export
     * @throws IOException if an I/O error occurs while writing
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static void export(OutputStream out, Csv csv) throws IOException {
        Assertions.requireNonNulls(out, csv);
        export(out, csv, Charset.defaultCharset());
    }

    /**
     * Exports the provided records to the given output stream using the specified configuration and charset.
     *
     * @param out the output stream to write to
     * @param records the iterable of records to export
     * @param config the CSV configuration to use for formatting
     * @param charset the charset to use for writing
     * @throws IOException if an I/O error occurs while writing
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static void export(OutputStream out, Iterable<? extends Record> records, CsvConfiguration config, Charset charset) throws IOException {
        Assertions.requireNonNulls(out, records, config, charset);
        var formatter = new CsvFormatter(config);
        try (var writer = new BufferedWriter(new OutputStreamWriter(out, charset))) {
            for (var rcd : records) {
                var values = CACHE.get(rcd.getClass()).stream()
                        .map(f -> f.apply(rcd))
                        .toList();
                writer.write(formatter.toCsvString(values));
                writer.newLine();
            }
        }
    }

    /**
     * Exports the provided records to the given output stream using the default configuration and default charset.
     *
     * @param out the output stream to write to
     * @param records the iterable of records to export
     * @throws IOException if an I/O error occurs while writing
     * @throws NullPointerException if any of the specified arguments is null
     */
    public static void export(OutputStream out, Iterable<? extends Record> records) throws IOException {
        Assertions.requireNonNulls(out, records);
        export(out, records, CsvConfiguration.SEMICOLON, StandardCharsets.UTF_8);
    }

}
