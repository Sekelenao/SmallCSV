package io.github.sekelenao.smallcsv.test;

import io.github.sekelenao.smallcsv.api.Csv;
import io.github.sekelenao.smallcsv.api.Row;
import io.github.sekelenao.smallcsv.api.CsvConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

final class RowTest {

    private static Row helloWorldRow() {
        var row = new Row("Hello", "world");
        row.add("!");
        return row;
    }

    @Nested
    final class Constructors {

        @Test
        @DisplayName("Empty after default constructor")
        void byEmpty() {
            assertEquals(0, new Row().size());
            assertEquals("", new Row().toString());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 100, 1000, 10000})
        @DisplayName("Constructor by size")
        void bySize(int size) {
            var row = new Row(size);
            assertEquals(size, row.size());
            assertEquals(";".repeat(Math.max(size - 1, 0)), row.toString());
        }

        @Test
        @DisplayName("Constructor by size assertions")
        void bySizeAssertions() {
            assertThrows(IllegalArgumentException.class, () -> new Row(-1));
        }

        @Test
        @DisplayName("VarArgs constructor")
        void byVarArgs() {
            var array = new String[]{"", "Hello", "world", "!", "", ""};
            var row = new Row(array);
            assertAll("Simple operations",
                    () -> assertEquals(4, new Row("", "Hello", "world", "!").size()),
                    () -> assertEquals(";Hello;world;!;;", row.toString()),
                    () -> assertEquals(0, new Row(new String[]{}).size()),
                    () -> assertEquals("", new Row(new String[]{}).toString()),
                    () -> assertEquals(1, new Row("").size())
            );
        }

        @Test
        @DisplayName("VarArgs constructor null assertions")
        void byVarArgsAssertions() {
            assertAll("VarArgs constructor null assertions",
                    () -> assertThrows(NullPointerException.class, () -> new Row((String) null)),
                    () -> assertThrows(NullPointerException.class, () -> new Row(new String[]{null})),
                    () -> assertThrows(NullPointerException.class, () -> new Row(new String[]{"wrong", null}))
            );
        }

        @Test
        @DisplayName("Collection constructor")
        void byCollection() {
            var helloWorldList = new ArrayList<>(List.of("", "Hello", "world", "!"));
            var row = new Row(helloWorldList);
            assertAll("Simple operations",
                    () -> assertEquals(4, row.size()),
                    () -> assertEquals(";Hello;world;!", row.toString()),
                    () -> assertEquals(0, new Row(Collections.emptyList()).size()),
                    () -> assertEquals(1, new Row(List.of("")).size()),
                    () -> assertEquals("", new Row(List.of("")).toString()),
                    () -> assertEquals(";", new Row(List.of("", "")).toString())
            );
            helloWorldList.add("test");
            assertEquals(";Hello;world;!", row.toString());
        }

        @Test
        @DisplayName("Collection constructor null assertions")
        void byCollectionAssertions() {
            var wrongList = new ArrayList<String>();
            wrongList.add("wrong");
            wrongList.add(null);
            assertAll("NullPointer assertions",
                    () -> assertThrows(NullPointerException.class, () -> new Row((List<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> new Row(wrongList))
            );
        }

        @Test
        @DisplayName("Iterable constructor")
        void byIterable() {
            var otherRow = new Row(List.of("", "Hello", "world", "!"));
            var row = new Row(otherRow);
            assertAll("Simple operations",
                    () -> assertEquals(4, row.size()),
                    () -> assertEquals(";Hello;world;!", row.toString()),
                    () -> assertEquals(0, new Row(new Row()).size()),
                    () -> assertEquals(1, new Row(new Row("")).size()),
                    () -> assertEquals("", new Row(new Row("")).toString()),
                    () -> assertEquals(";", new Row(new Row("", "")).toString()),
                    () -> assertEquals(";", new Row(new Row("", ""), 2).toString()),
                    () -> assertEquals(";", new Row(new Row("", ""), 1).toString()),
                    () -> assertEquals(";", new Row(new Row("", ""), 100).toString())
            );
            otherRow.add("test");
            assertEquals(";Hello;world;!", row.toString());
        }

        @Test
        @DisplayName("Iterable constructor assertions")
        void byIterableAssertions() {
            var nullIterable = new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return Arrays.asList(new String[]{"", null}).iterator();
                }
            };
            assertAll("NullPointer assertions",
                    () -> assertThrows(NullPointerException.class, () -> new Row((Iterable<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> new Row(nullIterable)),
                    () -> assertThrows(NullPointerException.class, () -> new Row(nullIterable, 0)),
                    () -> assertThrows(NullPointerException.class, () -> new Row(null, 0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Row(nullIterable, -1))
            );
        }

    }

    @Nested
    @DisplayName("Empty tests")
    final class IsEmptyOrBlank {

        @Test
        @DisplayName("Is empty test")
        void isEmptyOrBlank() {
            var row = new Row();
            assertAll("Empty tests",
                    () -> assertTrue(row.isEmpty()),
                    () -> {
                        row.addAll("     ", "\n", "\t");
                        assertFalse(row.isEmpty());
                    }
            );
        }

    }

    @Nested
    @DisplayName("Inserts and add")
    final class InsertAndAdd {


        @Test
        @DisplayName("add")
        void add() {
            var row = new Row();
            row.add("Hello");
            row.add("world");
            row.add("!");
            assertAll("add",
                    () -> assertEquals(3, row.size()),
                    () -> assertEquals("Hello;world;!", row.toString()),
                    () -> assertEquals("Hello", row.get(0)),
                    () -> assertEquals("world", row.get(1)),
                    () -> assertEquals("!", row.get(2))
            );
        }

        @Test
        @DisplayName("add null assertions")
        void addNullAssertions() {
            var emptyRow = new Row();
            assertThrows(NullPointerException.class, () -> emptyRow.add(null));
        }

        @Test
        @DisplayName("Add all null assertions")
        void addAllNullAssertions() {
            var emptyRow = new Row();
            var lst = Collections.singleton((String) null);
            assertAll("Add all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll((Iterable<String>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll((String[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyRow.addAll(lst))
            );
        }

    }

    @Nested
    @DisplayName("Set")
    final class SetTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Set all indices working")
        void setAllIndices(int index) {
            var row = Csv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Set all indices",
                    () -> {
                        row.set(index, "replaced");
                        assertEquals("replaced", row.get(index));
                    },
                    () -> assertEquals("replaced", row.get(index))
            );
        }

        @Test
        @DisplayName("Set indices assertions")
        void setAllIndicesAssertions() {
            var emptyRow = new Row();
            var row = Csv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.set(-1, "wrong")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.set(8, "wrong")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyRow.set(0, "wrong"))
            );
        }

        @Test
        @DisplayName("Set null assertions")
        void setNullAssertions() {
            var row = new Row("One");
            assertThrows(NullPointerException.class, () -> row.set(0, null));
        }

    }

    @Nested
    final class Fill {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 8, 100, 1000})
        @DisplayName("Fill basic test")
        void fill(int size) {
            var row = new Row();
            row.fill(size);
            assertAll("Fill",
                    () -> assertEquals(size, row.size()),
                    () -> assertTrue(row.stream().allMatch(String::isEmpty)),
                    () -> {
                        row.fill(size);
                        assertEquals(size * 2, row.size());
                    }
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -2, -100})
        @DisplayName("Fill assertions")
        void fillAssertions(int size) {
            var emptyRow = new Row();
            assertAll("Fill assertions",
                    () -> assertThrows(IllegalArgumentException.class, () -> emptyRow.fill(size))
            );
        }

    }

    @Nested
    final class Get {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Get all indices working")
        void getAllIndices(int index) {
            var row = Csv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertEquals(String.valueOf(index), row.get(index));
        }

        @Test
        @DisplayName("Get indices assertions")
        void getAllIndicesAssertions() {
            var emptyRow = new Row();
            var row = Csv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> row.get(8)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyRow.get(0))
            );
        }

        @Test
        @DisplayName("Get first and last")
        void getFirstAndLast() {
            var row = Csv.from(Collections.singleton("0;1;2;3;4;5;6;7")).getFirst();
            assertAll("Get first and last",
                    () -> assertEquals("0", row.getFirst()),
                    () -> assertEquals("7", row.getLast())
            );
        }

        @Test
        @DisplayName("Get indices assertions")
        void getFirstAndLastAssertions() {
            var emptyRow = new Row();
            assertAll("Get first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, emptyRow::getFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyRow::getLast)
            );
        }

    }

    @Nested
    final class Contains {

        private record Citation(String value) {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof String str && str.equals(value);
            }
        }

        private static final Row ROW = Csv.from(Collections.singleton("1;3;5;7;9")).getFirst();

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9})
        @DisplayName("Contains basic tests")
        void contains(int value) {
            assertAll("Contains basic tests",
                    () -> assertEquals((value & 1) == 1, ROW.contains(String.valueOf(value))),
                    () -> assertFalse(ROW.contains(value)),
                    () -> assertFalse(ROW.contains(null))
            );
        }

        @Test
        @DisplayName("Contains with custom equals")
        void containsWithCustomEquals() {
            var row = new Row("Hey", "a quote is a quote");
            assertAll("Contains with custom equals",
                    () -> assertTrue(row.contains(new Citation("a quote is a quote"))),
                    () -> assertTrue(row.contains("a quote is a quote")),
                    () -> assertFalse(row.contains("Yes")),
                    () -> assertFalse(row.contains(new Citation("Yes")))
            );
        }

    }

    @Nested
    final class ForEach {

        @Test
        @DisplayName("ForEach basic test")
        void forEach() {
            var row = helloWorldRow();
            var lst = new ArrayList<String>();
            row.forEach((Object value) -> lst.add((String) value));
            assertEquals(String.join("", row), String.join("", lst));
        }

        @Test
        @DisplayName("ForEach null assertions")
        void forEachNullAssertions() {
            var emptyRow = new Row();
            assertThrows(NullPointerException.class, () -> emptyRow.forEach(null));
        }

    }

    @Nested
    @DisplayName("Iterable")
    final class IterableTest {

        @Test
        @DisplayName("Empty iterator")
        void emptyIterator() {
            var row = new Row();
            var it = row.iterator();
            assertAll(
                () -> assertFalse(it.hasNext()),
                () -> assertThrows(NoSuchElementException.class, it::next)
            );
        }

        @Test
        @DisplayName("For each loop is working")
        void iterableFor() {
            var lst = new ArrayList<String>();
            var row = helloWorldRow();
            for (String string : row) lst.add(string);
            assertAll("For each working",
                    () -> assertEquals(row.size(), lst.size()),
                    () -> assertEquals(String.join("", row), String.join("", lst))
            );
        }

        @Test
        @DisplayName("Iterator is working")
        void iterator() {
            var row = helloWorldRow();
            var it = row.iterator();
            var emptyIt = new Row().iterator();
            assertAll("Iterator is working",
                    () -> assertTrue(it.hasNext()),
                    () -> assertTrue(it.hasNext()),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            assertEquals(row.get(i), it.next());
                        }
                    },
                    () -> assertFalse(it::hasNext),
                    () -> assertThrows(NoSuchElementException.class, it::next),
                    () -> assertFalse(emptyIt.hasNext())
            );
        }

        @Test
        @DisplayName("Iterator remove is working")
        void iteratorRemove() {
            var row = helloWorldRow();
            var it = row.iterator();
            assertAll("Iterator remove",
                    () -> assertThrows(UnsupportedOperationException.class, it::remove)
            );
        }

        @Test
        @DisplayName("Iterator for each remaining is working")
        void iterableForEachRemaining() {
            var lst = new ArrayList<String>();
            var row = helloWorldRow();
            var it = row.iterator();
            it.next();
            it.forEachRemaining(lst::add);
            assertAll("For each remaining working",
                    () -> assertEquals(2, lst.size()),
                    () -> assertEquals("world!", String.join("", lst))
            );
        }

        @Test
        @DisplayName("Iterator concurrent modifications")
        void iteratorConcurrentModifications() {
            var row = helloWorldRow();
            var it = row.iterator();
            row.add("");
            assertThrows(ConcurrentModificationException.class, it::next);
        }

    }

    @Nested
    @DisplayName("Stream")
    final class StreamTest {

        @Test
        @DisplayName("Stream basic tests")
        void stream() {
            var lst = new ArrayList<String>();
            var emptyRow = new Row();
            var row = helloWorldRow();
            row.stream().filter(s -> s.length() > 1).forEach(lst::add);
            var newRow = row.stream().parallel().filter(s -> s.length() > 1).collect(Row.collector());
            assertAll("Stream basics",
                    () -> assertEquals("Hello world", String.join(" ", lst)),
                    () -> assertEquals(2, lst.size()),
                    () -> assertDoesNotThrow(emptyRow::stream),
                    () -> assertEquals(2, newRow.size())
            );
        }

    }

    @Nested
    @DisplayName("Map")
    final class MapTest {

        @Test
        @DisplayName("Map basic tests")
        void map() {
            var row = helloWorldRow();
            row.map(String::toUpperCase);
            assertAll("Map basics",
                    () -> assertEquals("HELLO;WORLD;!", row.toString()),
                    () -> assertEquals(3, row.size()),
                    () -> assertDoesNotThrow(() -> row.map((Object s) -> "")),
                    () -> assertEquals(0, row.stream().mapToInt(String::length).sum())
            );
        }

        @Test
        @DisplayName("Map null assertions")
        void mapAssertions() {
            var helloWorldRow = helloWorldRow();
            assertAll("Map null assertions",
                    () -> assertThrows(NullPointerException.class, () -> helloWorldRow.map(null)),
                    () -> assertThrows(NullPointerException.class, () -> helloWorldRow.map(s -> null))
            );
        }

    }

    @Nested
    @DisplayName("Collector")
    final class CollectorTest {

        @Test
        @DisplayName("Collector is working")
        void collector() {
            var row = new Row("x", "y", "z", "x", "z", "y");
            var row2 = row.stream()
                    .filter(s -> !s.equals("z"))
                    .collect(Row.collector());
            var row3 = row.stream().collect(Row.collector());
            assertAll("Collector",
                    () -> assertEquals(new Row("x", "y", "x", "y"), row2),
                    () -> assertEquals(row, row3)
            );
        }

        @Test
        @DisplayName("Parallel collector")
        void ParallelCollector() {
            var result = IntStream.range(0, 1000).parallel()
                    .mapToObj(String::valueOf)
                    .collect(Row.collector());
            assertTrue(IntStream.range(0, 1000).allMatch(i -> result.get(i).equals(String.valueOf(i))));
        }

    }

    @Nested
    final class Equals {

        @Test
        @DisplayName("Equals basic tests")
        void equals() {
            var row = helloWorldRow();
            var row2 = helloWorldRow();
            var row3 = helloWorldRow();
            row3.set(0, "false");
            assertAll("Equals basic tests",
                    () -> assertNotSame(row, row2),
                    () -> assertEquals(row, row2),
                    () -> assertNotEquals(row, row3),
                    () -> {
                        row.set(0, "hello");
                        assertNotEquals(row, row2);
                    },
                    () -> assertNotEquals(null, row)
            );
        }

    }

    @Nested
    final class HashCode {

        @Test
        @DisplayName("HashCode basic tests")
        void hashcode() {
            var row = helloWorldRow();
            var row2 = helloWorldRow();
            assertAll("Equals basic tests",
                    () -> assertEquals(row.hashCode(), row2.hashCode()),
                    () -> {
                        row.set(0, "hello");
                        assertNotEquals(row.hashCode(), row2.hashCode());
                    }
            );
        }

    }

    @Nested
    class ToString {

        @Test
        @DisplayName("Escape chars toString")
        void toStringEscapeChars() {
            assertAll("Escape chars assertions",
                    () -> assertEquals("\\n;", new Row("\\n", "").toString()),
                    () -> assertEquals("\t;tab", new Row("\t", "tab").toString())
            );
        }

        @Test
        @DisplayName("toString basic tests")
        void toStringBasicTests() {
            var row = new Row("Hello\"", "world", "!", ";", "");
            var rowAsString = row.toString();
            assertAll("toString basic tests",
                    () -> assertEquals("\"Hello\"\"\";world;!;\";\";", rowAsString),
                    () -> assertEquals(row, Csv.from(Collections.singleton("\"Hello\"\"\";world;!;\";\";")).getFirst()),
                    () -> assertEquals(row.toString(), rowAsString),
                    () -> assertEquals("\"\"\"\"", Csv.from(Collections.singleton("\"\"\"\"")).getFirst().toString())
            );
        }

        @Test
        @DisplayName("toString with custom config")
        void toStringWithConfig() {
            var row = new Row("Hello\"", "world", "!", ";", "");
            var otherRow = new Row("Hello'", "world", "!", ",", "'");
            assertAll("toString custom config",
                    () -> assertEquals("\"Hello\"\"\";world;!;\";\";", row.toString()),
                    () -> assertEquals("\"Hello\"\"\",world,!,;,", row.toString(CsvConfiguration.COMMA)),
                    () -> assertEquals("Hello\",world,!,;,", row.toString(new CsvConfiguration(',', '\''))),
                    () -> assertEquals("'Hello''',world,!,',',''''", otherRow.toString(new CsvConfiguration(',', '\'')))
            );
        }

        @Test
        @DisplayName("toString with newlines")
        void toStringWithNewlines() {
            var row = new Row("hey", "cool\ncool");
            assertEquals("""
                    hey;"cool
                    cool\"""", row.toString());
        }

    }
}