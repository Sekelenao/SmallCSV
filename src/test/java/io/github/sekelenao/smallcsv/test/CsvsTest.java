package io.github.sekelenao.smallcsv.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.io.TempDir;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import io.github.sekelenao.smallcsv.api.Csv;
import io.github.sekelenao.smallcsv.api.CsvColumn;
import io.github.sekelenao.smallcsv.api.CsvConfiguration;
import io.github.sekelenao.smallcsv.api.Csvs;
import io.github.sekelenao.smallcsv.api.Row;
import io.github.sekelenao.smallcsv.api.exception.CsvParsingException;

@DisplayName("Csvs Utility & Factory Tests")
class CsvsTest {

    @TempDir
    static Path tempDir;

    private static Path copyResourceToTemp(String resourceName) throws IOException {
        var target = tempDir.resolve(resourceName);
        try (var in = CsvsTest.class.getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    public record Animal(@CsvColumn String name, float ignored, @CsvColumn int legs) {
        public Animal {
            Objects.requireNonNull(name);
            if (legs < 0) {
                throw new IllegalArgumentException("legs cannot be negative");
            }
        }
    }

    public record Food(@CsvColumn String name, @CsvColumn String color, String secretRecipe) {
        public Food {
            Objects.requireNonNull(name);
            Objects.requireNonNull(color);
            Objects.requireNonNull(secretRecipe);
        }

        @Override
        public String toString() {
            return name + " !";
        }
    }

    public record BankAccount(
            @CsvColumn String bankName,
            @CsvColumn UUID uuid,
            @CsvColumn double balance,
            @CsvColumn Food favoriteFood
    ) {
        public BankAccount {
            Objects.requireNonNull(uuid);
            Objects.requireNonNull(bankName);
            if (balance < 0) {
                throw new IllegalArgumentException("balance cannot be negative");
            }
        }
    }

    public static final List<Animal> ANIMALS = List.of(
            new Animal("Dog", 0f, 4),
            new Animal("\"Cat\"\n", 0f, 4),
            new Animal("Spider;", 0f, 8)
    );

    public static final List<Food> FOODS = List.of(
            new Food("Soup", "Brown", "Contains Java coffee"),
            new Food("Burger", "Multicolor", "Contains iceberg salad"),
            new Food("Fish", "Blue", "Contains fish...")
    );

    private static final Iterable<BankAccount> BANK_ACCOUNT_ITERABLE = () -> new Iterator<>() {
        private int index;
        private static final Random RANDOM = new Random();

        @Override
        public boolean hasNext() {
            return index < 1_000_000;
        }

        @Override
        public BankAccount next() {
            if (!hasNext()) throw new NoSuchElementException();
            index++;
            return new BankAccount("OnlyBank", UUID.randomUUID(), RANDOM.nextDouble(), FOODS.get(RANDOM.nextInt(FOODS.size())));
        }
    };

    @Nested
    @DisplayName("From")
    class From {

        @Test
        @DisplayName("Should read CSV from text using default configuration")
        void fromText() {
            var text = "\"Hello\"\"\";world;!;\";\";";
            var csv1 = Csvs.from(Collections.singleton(text));
            var csv2 = Csvs.from(Collections.singleton(";\"Hello\";world"));
            assertAll("from text default",
                    () -> assertEquals(1, csv1.size()),
                    () -> assertEquals(1, csv2.size()),
                    () -> assertEquals(5, csv1.getFirst().size()),
                    () -> assertEquals("Hello\"", csv1.getFirst().get(0)),
                    () -> assertEquals("world", csv1.getFirst().get(1)),
                    () -> assertEquals("!", csv1.getFirst().get(2)),
                    () -> assertEquals(";", csv1.getFirst().get(3)),
                    () -> assertEquals("", csv1.getFirst().get(4)),
                    () -> assertEquals(text, csv1.getFirst().toString()),
                    () -> assertEquals(3, csv2.getFirst().size()),
                    () -> assertEquals("", csv2.getFirst().get(0)),
                    () -> assertEquals("Hello", csv2.getFirst().get(1))
            );
        }

        @Test
        @DisplayName("Should parse complex quote structures correctly")
        void fromTextComplex() {
            assertAll("From text complex",
                    () -> assertEquals("\n", Csvs.from(Collections.singleton("\"\"")).toString()),
                    () -> assertEquals("\"", Csvs.from(Collections.singleton("\"\"\"\"")).getFirst().getFirst()),
                    () -> assertEquals("\"\"\"\"", Csvs.from(Collections.singleton("\"\"\"\"")).getFirst().toString()),
                    () -> assertEquals("\"\"\"\"\n", Csvs.from(Collections.singleton("\"\"\"\"")).toString()),
                    () -> assertEquals(" ; ; ;\n", Csvs.from(Collections.singleton(" ; ; ;")).toString())
            );
        }

        @Test
        @DisplayName("Should read CSV from text using custom configuration")
        void fromTextWithConfig() {
            var defaultText = Collections.singleton("Hello;world;!");
            var customText = Collections.singleton("'Hello,',world,'!'''");
            var config = new CsvConfiguration(',', '\'');
            var defaultRow = Csvs.from(defaultText, config).getFirst();
            var customRow = Csvs.from(customText, config).getFirst();
            assertAll("from text custom config",
                    () -> assertEquals(1, defaultRow.size()),
                    () -> assertEquals(3, customRow.size()),
                    () -> assertEquals('"' + "Hello;world;!" + '"', defaultRow.toString()),
                    () -> assertEquals("Hello;world;!", defaultRow.get(0)),
                    () -> assertEquals("Hello,;world;!'", customRow.toString()),
                    () -> assertEquals("Hello,", customRow.get(0)),
                    () -> assertEquals("world", customRow.get(1)),
                    () -> assertEquals("!'", customRow.get(2))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when inputs are null")
        void fromTextNull() {
            var config = new CsvConfiguration(' ', '@');
            var singleton = Collections.singleton("null");
            var path = tempDir.resolve("temp.csv");
            assertAll("Null assertions",
                    () -> assertThrows(NullPointerException.class, () -> Csvs.from((Iterable<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.from((List<String>) null, config)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.from(singleton, null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.from(path, config, null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.from((InputStream) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.from((BufferedReader) null))
            );
        }

        @Test
        @DisplayName("Should throw CsvParsingException for invalid CSV patterns")
        void fromTextParsingAssertions() {
            var config = new CsvConfiguration(',', '\'');
            assertAll("From text default",
                    () -> {
                        var singleton = Collections.singleton("Hello;\"world\"\";!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton));
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello;\"world\"!\";!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton));
                    },
                    () -> {
                        var singleton = Collections.singleton("\"Hello;world!;!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton));
                    },
                    () -> {
                        var singleton = Collections.singleton("'Hello;world!;!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("'Hello'';world!;!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello';'world!;!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello'';world!;!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("''';world!;!");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello;world!;!;'");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("'Hello,',world,'!''");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("Hello,world,!''");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton, config));
                    },
                    () -> {
                        var singleton = Collections.singleton("\"");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton));
                    },
                    () -> {
                        var singleton = Collections.singleton("\"\"\"");
                        assertThrows(CsvParsingException.class, () -> Csvs.from(singleton));
                    }
            );
        }

        @Test
        @DisplayName("Should read CSV from a file path")
        void fromFile() throws IOException {
            var pathSemicolon = copyResourceToTemp("template.csv");
            var pathComma = copyResourceToTemp("template_comma.csv");
            var pathUselessQuotes = copyResourceToTemp("template_useless_quotes.csv");
            var csvSemicolon = Csvs.from(pathSemicolon, CsvConfiguration.SEMICOLON);
            var csvComma = Csvs.from(pathComma, CsvConfiguration.COMMA);
            var csvUselessQuotes = Csvs.from(pathUselessQuotes, StandardCharsets.UTF_8);
            assertAll("From a file",
                    () -> assertEquals(6, csvSemicolon.size()),
                    () -> assertEquals(6, csvComma.size()),
                    () -> assertEquals(Files.readString(pathSemicolon).replace("\r", ""), csvSemicolon.toString()),
                    () -> assertEquals(Files.readString(pathComma).replace("\r", ""), csvComma.configure(CsvConfiguration.COMMA).toString()),
                    () -> assertEquals(csvSemicolon, csvComma),
                    () -> assertEquals(csvSemicolon.configure(new CsvConfiguration('@', '/')), csvComma),
                    () -> assertEquals(csvSemicolon, csvUselessQuotes)
            );
        }

        @Test
        @DisplayName("Should read CSV from an InputStream")
        void fromInputStream() throws IOException {
            var pathSemicolon = copyResourceToTemp("template.csv");
            try (var in = Files.newInputStream(pathSemicolon)) {
                var csv = Csvs.from(in, CsvConfiguration.SEMICOLON);
                assertEquals(6, csv.size());
                assertEquals(Files.readString(pathSemicolon).replace("\r", ""), csv.toString());
            }
        }

        @Test
        @DisplayName("Should read CSV from a BufferedReader")
        void fromBufferedReader() throws IOException {
            var pathSemicolon = copyResourceToTemp("template.csv");
            try (var reader = Files.newBufferedReader(pathSemicolon, StandardCharsets.UTF_8)) {
                var csv = Csvs.from(reader, CsvConfiguration.SEMICOLON);
                assertEquals(6, csv.size());
                assertEquals(Files.readString(pathSemicolon).replace("\r", ""), csv.toString());
            }
        }

        @Test
        @DisplayName("Read overloads coverage")
        void fromOverloads() throws IOException {
            var path = copyResourceToTemp("template.csv");

            // test from(Path)
            var csv1 = Csvs.from(path);
            assertEquals(6, csv1.size());

            // test from(InputStream)
            try (var in = Files.newInputStream(path)) {
                var csv2 = Csvs.from(in);
                assertEquals(6, csv2.size());
            }

            // test from(InputStream, Charset)
            try (var in = Files.newInputStream(path)) {
                var csv3 = Csvs.from(in, StandardCharsets.UTF_8);
                assertEquals(6, csv3.size());
            }

            // test from(BufferedReader)
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                var csv4 = Csvs.from(reader);
                assertEquals(6, csv4.size());
            }
        }

    }

    @Nested
    @DisplayName("Export")
    class Export {

        @Test
        @DisplayName("Should export CSV rows to a file path")
        void exportCsv() throws IOException {
            var path = tempDir.resolve("temp.csv");
            var csv = Csv.of(
                    Row.of("", "world", "\njump\n", "cool"),
                    Row.of("yeah", " ok", "")
            );
            Csvs.export(path, csv);
            assertEquals(Files.readString(path).replace("\r", ""), csv.toString());
        }

        @Test
        @DisplayName("Should export CSV rows to an OutputStream")
        void exportCsvToStream() throws IOException {
            var csv = Csv.of(
                    Row.of("", "world", "\njump\n", "cool"),
                    Row.of("yeah", " ok", "")
            );
            try (var out = new ByteArrayOutputStream()) {
                Csvs.export(out, csv);
                assertEquals(csv.toString(), out.toString(StandardCharsets.UTF_8).replace("\r", ""));
            }
            try (var out = new ByteArrayOutputStream()) {
                Csvs.export(out, csv, StandardCharsets.UTF_8);
                assertEquals(csv.toString(), out.toString(StandardCharsets.UTF_8).replace("\r", ""));
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when inputs are null on export")
        void exportAssertions() {
            var path = tempDir.resolve("temp.csv");
            var csv = Csv.of(
                    Row.of("", "world", "\njump\n", "cool"),
                    Row.of("yeah", " ok", "")
            );
            assertAll("Export assertions",
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(null, csv, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(path, (Csv) null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(path, csv, (OpenOption) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(path, csv, StandardCharsets.UTF_8, (OpenOption) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(path, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE, null))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when inputs are null on Stream export")
        void exportStreamAssertions() {
            var csv = Csv.of(
                    Row.of("", "world", "\njump\n", "cool"),
                    Row.of("yeah", " ok", "")
            );
            assertAll("Export stream assertions",
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export((OutputStream) null, csv)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(new ByteArrayOutputStream(), (Csv) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(new ByteArrayOutputStream(), csv, null))
            );
        }

    }

    @Nested
    @DisplayName("Record Support")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RecordSupport {

        private Path producedPath() {
            return tempDir.resolve("produced.csv");
        }

        @Test
        @DisplayName("Export with annotation and default methods")
        void exportWithAnnotation() throws IOException {
            var producedPath = producedPath();
            Csvs.export(producedPath, ANIMALS, StandardOpenOption.CREATE);
            var csv = Csvs.from(producedPath);
            assertAll("With annotation",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(ANIMALS.get(0).name, csv.get(0).getFirst()),
                    () -> assertEquals(ANIMALS.get(1).name, csv.get(1).getFirst()),
                    () -> assertEquals(ANIMALS.get(2).name, csv.get(2).getFirst()),
                    () -> assertEquals(ANIMALS.get(ANIMALS.size() - 1).legs, Integer.parseInt(csv.getLast().getLast()))
            );
            Files.deleteIfExists(producedPath);
            Csvs.export(producedPath, ANIMALS, CsvConfiguration.COMMA, StandardOpenOption.CREATE);
            var csv2 = Csvs.from(producedPath, CsvConfiguration.COMMA);
            assertAll("With annotation and config",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(producedPath);
        }

        @Test
        @DisplayName("Export with annotation and override toString")
        void exportWithAnnotationOverride() throws IOException {
            var producedPath = producedPath();
            Csvs.export(producedPath, FOODS, CsvConfiguration.SEMICOLON, StandardOpenOption.CREATE);
            var csv = Csvs.from(producedPath);
            assertAll("With annotation",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals(2, csv.getFirst().size()),
                    () -> assertEquals(FOODS.get(0).name, csv.get(0).getFirst()),
                    () -> assertEquals(FOODS.get(1).name, csv.get(1).getFirst()),
                    () -> assertEquals(FOODS.get(2).name, csv.get(2).getFirst()),
                    () -> assertEquals(FOODS.get(FOODS.size() - 1).color, csv.getLast().getLast())
            );
            Files.deleteIfExists(producedPath);
            Csvs.export(producedPath, FOODS, CsvConfiguration.COMMA, StandardOpenOption.CREATE);
            var csv2 = Csvs.from(producedPath, CsvConfiguration.COMMA);
            assertAll("With annotation and config",
                    () -> assertEquals(3, csv2.size()),
                    () -> assertEquals(2, csv2.getFirst().size())
            );
            Files.deleteIfExists(producedPath);
        }

        @Test
        @DisplayName("Export a lot")
        @Timeout(3)
        @Order(1)
        void exportALot() {
            var producedPath = producedPath();
            assertDoesNotThrow(() -> Csvs.export(producedPath, BANK_ACCOUNT_ITERABLE, CsvConfiguration.SEMICOLON, StandardOpenOption.CREATE));
        }

        @Test
        @DisplayName("Import a lot")
        @Timeout(3)
        @Order(2)
        void importALot() throws IOException {
            var producedPath = producedPath();
            var csv = Csvs.from(producedPath);
            assertEquals(1_000_000, csv.size());
            Files.deleteIfExists(producedPath);
        }

        @Test
        @DisplayName("Export records to an OutputStream")
        void exportRecordsToStream() throws IOException {
            try (var out = new ByteArrayOutputStream()) {
                Csvs.export(out, ANIMALS);
                try (var in = new java.io.ByteArrayInputStream(out.toByteArray())) {
                    var csv = Csvs.from(in);
                    assertAll("Stream export animals",
                            () -> assertEquals(3, csv.size()),
                            () -> assertEquals(2, csv.getFirst().size()),
                            () -> assertEquals(ANIMALS.get(0).name, csv.get(0).getFirst())
                    );
                }
            }
            try (var out = new ByteArrayOutputStream()) {
                Csvs.export(out, FOODS, CsvConfiguration.SEMICOLON, java.nio.charset.StandardCharsets.UTF_8);
                try (var in = new java.io.ByteArrayInputStream(out.toByteArray())) {
                    var csv = Csvs.from(in);
                    assertAll("Stream export foods",
                            () -> assertEquals(3, csv.size()),
                            () -> assertEquals(2, csv.getFirst().size()),
                            () -> assertEquals(FOODS.get(0).name, csv.get(0).getFirst())
                    );
                }
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when inputs are null on Stream export")
        void exportStreamAssertions() {
            assertAll("Stream export null assertions",
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export((OutputStream) null, ANIMALS)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(new ByteArrayOutputStream(), (Iterable<? extends Record>) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(new ByteArrayOutputStream(), ANIMALS, null, java.nio.charset.StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class, () -> Csvs.export(new ByteArrayOutputStream(), ANIMALS, CsvConfiguration.SEMICOLON, null))
            );
        }

        public record FaultyRecord(@CsvColumn String value) {
            @Override
            public String value() {
                throw new RuntimeException("faulty");
            }
        }

        public record ErrorRecord(@CsvColumn String value) {
            @Override
            public String value() {
                throw new OutOfMemoryError("error");
            }
        }

        public record SneakyRecord(@CsvColumn String value) {
            @Override
            public String value() {
                sneakyThrow(new IOException("checked"));
                return null;
            }

            @SuppressWarnings("unchecked")
            private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
                throw (T) t;
            }
        }

        private static class PrivateContainer {
            private record HiddenRecord(@CsvColumn String value) {}
        }

        @Test
        @DisplayName("Reflection exception coverage")
        void testReflectionExceptions() {
            var faulty = new FaultyRecord("a");
            var error = new ErrorRecord("b");
            var sneaky = new SneakyRecord("c");
            var hidden = new PrivateContainer.HiddenRecord("d");

            assertAll("Reflection exceptions",
                    () -> assertThrows(RuntimeException.class, () -> Csvs.export(new ByteArrayOutputStream(), List.of(faulty))),
                    () -> assertThrows(OutOfMemoryError.class, () -> Csvs.export(new ByteArrayOutputStream(), List.of(error))),
                    () -> assertThrows(java.lang.reflect.UndeclaredThrowableException.class, () -> Csvs.export(new ByteArrayOutputStream(), List.of(sneaky))),
                    () -> assertThrows(IllegalAccessError.class, () -> Csvs.export(new ByteArrayOutputStream(), List.of(hidden)))
            );
        }

    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Constructor is private and throw")
        void privateConstructor() throws NoSuchMethodException {
            var constructor = Csvs.class.getDeclaredConstructor();
            assertThrows(IllegalAccessException.class, constructor::newInstance);
            constructor.setAccessible(true);
            assertThrows(InvocationTargetException.class, constructor::newInstance);
        }

    }

}
