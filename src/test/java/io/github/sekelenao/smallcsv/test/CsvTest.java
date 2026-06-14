package io.github.sekelenao.smallcsv.test;

import io.github.sekelenao.smallcsv.api.Csv;
import io.github.sekelenao.smallcsv.api.Row;
import io.github.sekelenao.smallcsv.api.CsvConfiguration;
import io.github.sekelenao.smallcsv.api.exception.InvalidCsvValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

final class CsvTest {

    private static Csv csvTemplate() {
        return Csv.of(
                Row.of("\"Hello", "world", "!;"),
                Row.of("'Hello,", "\"second,\"", "world", "!'", "")
        );
    }

    private static Csv csvTemplate(int lineNumber) {
        return Csv.of(
                IntStream.range(0, lineNumber)
                        .mapToObj(i -> Row.of(String.valueOf(i)))
                        .toList()
        );
    }

    @Nested
    final class Constructors {

        @Test
        @DisplayName("Empty after default constructor")
        void byEmpty() {
            assertAll("Empty after default constructor",
                    () -> assertEquals(0, Csv.empty().size()),
                    () -> assertEquals("", Csv.empty().toString()),
                    () -> assertTrue(Csv.empty().isEmpty()),
                    () -> assertInstanceOf(RandomAccess.class, Csv.empty())
            );
        }

        @Test
        @DisplayName("VarArgs constructor")
        void byVarArgs() {
            var array = new Row[]{
                    Row.of("\"Hello", "world", "!;"),
                    Row.of("'Hello,", "\"second\"", "world", "!'", "")
            };
            var csv = Csv.of(array);
            assertAll("Simple operations",
                    () -> assertEquals(2, csv.size()),
                    () -> assertEquals("""
                                    \"""Hello";world;"!;"
                                    'Hello,;\"""second\""";world;!';
                                    """
                            , csv.toString()),
                    () -> assertEquals(0, Csv.of(new Row[]{}).size()),
                    () -> assertEquals("", Csv.of(new Row[]{}).toString()),
                    () -> assertEquals(1, Csv.of(Row.empty()).size()),
                    () -> assertEquals("\n", Csv.of(Row.empty()).toString())
            );
        }

        @Test
        @DisplayName("VarArgs constructor null assertions")
        void byVarArgsAssertions() {
            var emptyRow = Row.empty();
            var array1 = new Row[]{Row.empty(), null};
            assertAll("VarArgs constructor null assertions",
                    () -> assertThrows(NullPointerException.class, () -> Csv.of((Row) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csv.of(new Row[]{null})),
                    () -> assertThrows(NullPointerException.class, () -> Csv.of(array1)),
                    () -> assertThrows(NullPointerException.class, () -> Csv.of(emptyRow, null))
            );
        }

        @Test
        @DisplayName("Iterable constructor")
        void byIterable() {
            var rowList = new ArrayList<>(
                    List.of(
                            Row.of("One", "!"),
                            Row.of("Two", "!"),
                            Row.of("Three", "!")
                    )
            );
            var csv = Csv.of(rowList);
            assertAll("Simple operations",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals("""
                            One;!
                            Two;!
                            Three;!
                            """, csv.toString()),
                    () -> assertFalse(csv.isEmpty()),
                    () -> assertEquals(0, Csv.of(Collections.emptyList()).size()),
                    () -> assertEquals(1, Csv.of(Row.empty()).size()),
                    () -> assertEquals("", Csv.empty().toString()),
                    () -> assertEquals("\n", Csv.of(Row.empty()).toString()),
                    () -> assertEquals("\n\n", Csv.of(List.of(Row.empty(), Row.empty())).toString())
            );
        }

        @Test
        @DisplayName("Iterable constructor null assertions")
        void byIterableAssertions() {
            var wrongList = new ArrayList<Row>();
            wrongList.add(Row.empty());
            wrongList.add(null);
            assertAll("NullPointer assertions",
                    () -> assertThrows(NullPointerException.class, () -> Csv.of((List<Row>) null)),
                    () -> assertThrows(NullPointerException.class, () -> Csv.of(wrongList))
            );
        }

        @Test
        @DisplayName("Iterable (non-collection) constructor")
        void byNonCollectionIterable() {
            Iterable<Row> iterable = () -> List.of(Row.of("One", "!"), Row.of("Two", "!")).iterator();
            var csv = Csv.of(iterable);
            assertAll("Iterable (non-collection)",
                    () -> assertEquals(2, csv.size()),
                    () -> assertEquals("One;!\nTwo;!\n", csv.toString())
            );
        }

    }

    @Nested
    final class Configuration {

        static Stream<Character> escapeCharsProvider() {
            return Stream.of('\n', '\0', '\r', '\b', '\f');
        }

        @Test
        @DisplayName("Configuration is updated")
        void configUpdated() {
            var csv = csvTemplate();
            assertAll("Configuration is updated",
                    () -> assertEquals("""
                            \"""Hello";world;"!;"
                            'Hello,;\"""second,\""";world;!';
                            """, csv.toString()),
                    () -> assertEquals(CsvConfiguration.SEMICOLON, csv.configuration()),
                    () -> {
                        csv.configure(new CsvConfiguration(',', '\''));
                        assertEquals("""
                                "Hello,world,!;
                                '''Hello,','"second,"',world,'!''',
                                """, csv.toString());
                    },
                    () -> assertEquals(new CsvConfiguration(',', '\''), csv.configuration())
            );
        }

        @ParameterizedTest
        @MethodSource("escapeCharsProvider")
        @DisplayName("Configuration assertions")
        void configAssertions(char wrongChar) {
            assertAll("Configuration assertions",
                    () -> assertThrows(InvalidCsvValueException.class, () -> new CsvConfiguration(wrongChar, '"')),
                    () -> assertThrows(InvalidCsvValueException.class, () -> new CsvConfiguration(';', wrongChar)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new CsvConfiguration(';', ';'))
            );
        }
    }

    @Nested
    @DisplayName("Inserts and add")
    final class InsertAndAdd {

        @Test
        @DisplayName("add")
        void add() {
            var csv = Csv.empty();
            csv.addLast(Row.of("Hello", "world", "!"));
            assertAll("add",
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("Hello;world;!\n", csv.toString()),
                    () -> assertEquals(Row.of("Hello", "world", "!"), csv.get(0)),
                    () -> {
                        csv.addLast(Row.of("I", "love", "Java", ""));
                        assertEquals("""
                                        Hello;world;!
                                        I;love;Java;
                                        """
                                , csv.toString());
                    },
                    () -> assertEquals(Row.of("I", "love", "Java", ""), csv.get(1))
            );
        }

        @Test
        @DisplayName("add null assertions")
        void addNullAssertions() {
            var emptyCsv = Csv.empty();
            assertThrows(NullPointerException.class, () -> emptyCsv.addLast(null));
        }

        @Test
        @DisplayName("add first")
        void addFirst() {
            var csv = Csv.empty();
            csv.addFirst(Row.of("Hello", "world", "!"));
            assertAll("addFirst",
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("Hello;world;!\n", csv.toString()),
                    () -> assertEquals(Row.of("Hello", "world", "!"), csv.get(0)),
                    () -> {
                        csv.addFirst(Row.of("I", "love", "Java", ""));
                        assertEquals("""
                                        I;love;Java;
                                        Hello;world;!
                                        """
                                , csv.toString());
                    },
                    () -> assertEquals(Row.of("Hello", "world", "!"), csv.get(1))
            );
        }

        @Test
        @DisplayName("addFirst null assertions")
        void addFirstNullAssertions() {
            var emptyCsv = Csv.empty();
            assertThrows(NullPointerException.class, () -> emptyCsv.addFirst(null));
        }

        @Test
        @DisplayName("Insert")
        void insert() {
            var csv = Csv.empty();
            csv.insert(0, Row.of("Hello"));
            csv.insert(0, Row.of("world"));
            csv.insert(1, Row.of("!"));
            assertAll("Insert",
                    () -> assertEquals(3, csv.size()),
                    () -> assertEquals("""
                                    world
                                    !
                                    Hello
                                    """
                            , csv.toString()),
                    () -> assertEquals(Row.of("world"), csv.get(0)),
                    () -> assertEquals(Row.of("!"), csv.get(1)),
                    () -> assertEquals(Row.of("Hello"), csv.get(2))
            );
        }

        @Test
        @DisplayName("Insert null assertions")
        void insertNullAssertions() {
            var emptyCsv = Csv.empty();
            assertThrows(NullPointerException.class, () -> emptyCsv.insert(0, null));
        }

        @Test
        @DisplayName("Insert indices assertions")
        void insertIndicesAssertions() {
            var csv = Csv.empty();
            var row = Row.of("out");
            assertAll("Insert indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(-1, row)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(1, row))
            );
            csv.insertAll(csv.size(), Row.of("Hello", "world", "!"));
            assertAll("Insert indices assertions 2",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(-1, row)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insert(4, row))
            );
        }

        @Test
        @DisplayName("Insert all at end")
        void insertAllAtEnd() {
            var csv = csvTemplate();
            csv.insertAll(csv.size(), Row.of("(", "and Meta-verse", ")"), Row.of("Java"));
            csv.insertAll(csv.size(), List.of(Row.of("yes"), Row.of("ok")));
            assertAll("Insert all at end",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    ""\"Hello";world;"!;"
                                    'Hello,;""\"second,""\";world;!';
                                    (;and Meta-verse;)
                                    Java
                                    yes
                                    ok
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all at start")
        void insertAllAtStart() {
            var csv = csvTemplate();
            csv.insertAll(0, Row.of("(", "and Meta-verse", ")"), Row.empty());
            csv.insertAll(0, List.of(Row.empty(), Row.empty()));
            assertAll("Insert all at start",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    
                                    
                                    (;and Meta-verse;)
                                    
                                    ""\"Hello";world;"!;"
                                    'Hello,;""\"second,""\";world;!';
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all")
        void insertAll() {
            var csv = csvTemplate();
            csv.insertAll(1, List.of(Row.of("(", "and Meta-verse", ")"), Row.empty()));
            csv.insertAll(1, Row.of("1"), Row.of("2"));
            assertAll("Insert all",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    ""\"Hello";world;"!;"
                                    1
                                    2
                                    (;and Meta-verse;)
                                    
                                    'Hello,;""\"second,""\";world;!';
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all null assertions")
        void insertAllNullAssertions() {
            var emptyCsv = Csv.empty();
            var helloCsv = csvTemplate();
            var lst = new ArrayList<Row>();
            lst.add(Row.empty());
            lst.add(null);
            assertAll("Insert all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, (Iterable<Row>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, (Row[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.insertAll(0, lst)),
                    () -> assertThrows(NullPointerException.class, () -> helloCsv.insertAll(1, lst)),
                    () -> assertThrows(NullPointerException.class, () -> helloCsv.insertAll(1, new Row[]{null}))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 3, 5, 100})
        @DisplayName("Insert all position assertions")
        void insertAllPositionAssertions(int index) {
            var csv = csvTemplate();
            var lst = new ArrayList<Row>();
            var row = Row.empty();
            assertAll("Insert all position assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insertAll(index, lst)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.insertAll(index, row))
            );
        }

        @Test
        @DisplayName("Add all at end")
        void addAll() {
            var csv = csvTemplate();
            csv.addAll(Row.of("(", "and Meta-verse", ")"), Row.of("Java"));
            csv.addAll(List.of(Row.of("yes"), Row.of("ok")));
            assertAll("Add all",
                    () -> assertEquals(6, csv.size()),
                    () -> assertEquals("""
                                    ""\"Hello";world;"!;"
                                    'Hello,;""\"second,""\";world;!';
                                    (;and Meta-verse;)
                                    Java
                                    yes
                                    ok
                                    """
                            , csv.toString())
            );
        }

        @Test
        @DisplayName("Insert all null assertions")
        void addAllNullAssertions() {
            var emptyCsv = Csv.empty();
            var lst = Collections.singleton((Row) null);
            assertAll("Add all null",
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll((Iterable<Row>) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll((Row[]) null)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll(lst)),
                    () -> assertThrows(NullPointerException.class, () -> emptyCsv.addAll(Row.empty(), null))
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
            var csv = csvTemplate(8);
            assertAll("Set all indices",
                    () -> {
                        csv.set(index, Row.of("replaced"));
                        assertEquals("replaced", csv.get(index).getFirst());
                    }
            );
        }

        @Test
        @DisplayName("Set indices assertions")
        void setAllIndicesAssertions() {
            var csv = csvTemplate(8);
            var emptyCsv = Csv.empty();
            var emptyRow = Row.empty();
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.set(-1, emptyRow)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.set(8, emptyRow)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyCsv.set(0, emptyRow))
            );
        }

        @Test
        @DisplayName("Set null assertions")
        void setNullAssertions() {
            var csv = Csv.of(Row.of("Alone"));
            assertThrows(NullPointerException.class, () -> csv.set(0, null));
        }

    }

    @Nested
    final class Get {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Get all indices working")
        void getAllIndices(int index) {
            var csv = csvTemplate(8);
            assertEquals(String.valueOf(index), csv.get(index).getFirst());
        }

        @Test
        @DisplayName("Get indices assertions")
        void getAllIndicesAssertions() {
            var emptyCsv = Csv.empty();
            var csv = csvTemplate(8);
            assertAll("Set indices assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.get(8)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> emptyCsv.get(0))
            );
        }

        @Test
        @DisplayName("Get first and last")
        void getFirstAndLast() {
            var row = csvTemplate(8);
            assertAll("Get first and last",
                    () -> assertEquals("0", row.getFirst().getFirst()),
                    () -> assertEquals("7", row.getLast().getLast())
            );
        }

        @Test
        @DisplayName("Get indices assertions")
        void getFirstAndLastAssertions() {
            var emptyCsv = Csv.empty();
            assertAll("Get first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::getFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::getLast)
            );
        }

    }

    @Nested
    final class Remove {

        @ParameterizedTest
        @ValueSource(ints = {0, 1})
        @DisplayName("Remove basic tests")
        void remove(int index) {
            var csv = csvTemplate(2);
            csv.remove(index);
            assertAll("Remove basic tests",
                    () -> assertEquals(1, csv.size()),
                    () -> assertDoesNotThrow(() -> csv.remove(0)),
                    () -> assertEquals(0, csv.size())
            );
        }

        @Test
        @DisplayName("Remove all values")
        void removeAll() {
            var csv = csvTemplate();
            var initialSize = csv.size();
            for (int i = 0; i < initialSize; i++) csv.remove(0);
            assertAll("Remove all",
                    () -> assertTrue(csv.isEmpty()),
                    () -> assertEquals(0, csv.size()),
                    () -> assertEquals("", csv.toString())
            );
        }

        @Test
        @DisplayName("Remove assertions")
        void removeAssertions() {
            var csv = csvTemplate();
            csv.remove(0);
            assertAll("Remove assertions",
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.remove(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.remove(2)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> csv.remove(1))
            );
        }

        @Test
        @DisplayName("Remove first and last")
        void removeFirstAndLast() {
            int count = 0;
            var csv = csvTemplate(3);
            while (!csv.isEmpty()) {
                csv.removeFirst();
                count++;
                assertEquals(3 - count, csv.size());
            }
            assertEquals(3, count);
            count = 0;
            var csv2 = csvTemplate(3);
            while (!csv2.isEmpty()) {
                csv2.removeLast();
                count++;
                assertEquals(3 - count, csv2.size());
            }
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Remove first and last assertions")
        void removeFirstAndLastAssertions() {
            var csv = csvTemplate(3);
            var emptyCsv = Csv.empty();
            csv.removeFirst();
            csv.removeLast();
            csv.removeFirst();
            assertAll("Remove first and last assertions",
                    () -> assertThrows(NoSuchElementException.class, csv::removeFirst),
                    () -> assertThrows(NoSuchElementException.class, csv::removeLast),
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::removeFirst),
                    () -> assertThrows(NoSuchElementException.class, emptyCsv::removeLast)
            );
        }

        @Test
        @DisplayName("RemoveIf basic tests")
        void removeIf() {
            var csv = csvTemplate(8);
            int expectedSize = csv.size() / 2;
            assertAll("RemoveIf basic tests",
                    () -> assertTrue(csv.removeIf(r -> (Integer.parseInt(r.getFirst()) & 1) == 0)),
                    () -> assertTrue(csv.stream().allMatch(r -> (Integer.parseInt(r.getFirst()) & 1) == 1)),
                    () -> assertEquals(expectedSize, csv.size()),
                    () -> assertFalse(() -> csv.removeIf((Object value) -> false)),
                    () -> assertEquals(expectedSize, csv.size()),
                    () -> {
                        csv.removeIf(value -> true);
                        assertEquals(0, csv.size());
                    }
            );
        }

        @Test
        @DisplayName("RemoveIf null assertions")
        void removeIfNullAssertions() {
            var emptyCsv = Csv.empty();
            assertThrows(NullPointerException.class, () -> emptyCsv.removeIf(null));
        }

    }

    @Nested
    final class Contains {

        @Test
        @DisplayName("Contains basic tests")
        void contains() {
            var csv = csvTemplate(8);
            assertAll("Contains basic tests",
                    () -> assertTrue(csv.contains(Row.of("5"))),
                    () -> assertTrue(csv.contains(Row.of("0"))),
                    () -> assertFalse(csv.contains(Row.of("8"))),
                    () -> assertFalse(csv.contains(Row.of("-1"))),
                    () -> assertFalse(csv.contains(new Object())),
                    () -> assertFalse(csv.contains(null))
            );
        }

    }

    @Nested
    final class ForEach {

        @Test
        @DisplayName("ForEach basic test")
        void forEach() {
            var csv = csvTemplate();
            var lst = new ArrayList<Row>();
            csv.forEach((Object value) -> lst.add((Row) value));
            assertTrue(IntStream.range(0, csv.size()).allMatch(i -> csv.get(i).equals(lst.get(i))));
        }

        @Test
        @DisplayName("ForEach null assertions")
        void forEachNullAssertions() {
            var emptyCsv = Csv.empty();
            assertThrows(NullPointerException.class, () -> emptyCsv.forEach(null));
        }

    }

    @Nested
    @DisplayName("Iterable")
    final class IterableTest {

        @Test
        @DisplayName("Empty iterator")
        void emptyIterator() {
            var csv = Csv.empty();
            assertAll("Empty iterator",
                    () -> assertFalse(csv.iterator().hasNext()),
                    () -> assertThrows(NoSuchElementException.class, csv.iterator()::next)
            );
        }

        @Test
        @DisplayName("For each loop is working")
        void iterableFor() {
            var lst = new ArrayList<Row>();
            var csv = csvTemplate();
            for (var row : csv) lst.add(row);
            assertAll("For each working",
                    () -> assertEquals(csv.size(), lst.size()),
                    () -> assertTrue(IntStream.range(0, csv.size()).allMatch(i -> csv.get(i).equals(lst.get(i))))
            );
        }

        @Test
        @DisplayName("Iterator is working")
        void iterator() {
            var csv = csvTemplate();
            var it = csv.iterator();
            var emptyIt = Row.empty().iterator();
            assertAll("Iterator is working",
                    () -> assertTrue(it.hasNext()),
                    () -> assertTrue(it.hasNext()),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            assertEquals(csv.get(i), it.next());
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
            var csv = csvTemplate();
            var it = csv.iterator();
            assertAll("Iterator remove",
                    () -> assertThrows(IllegalStateException.class, it::remove),
                    () -> {
                        for (int i = 0; it.hasNext(); i++) {
                            if (i == 1) {
                                it.remove();
                                assertThrows(IllegalStateException.class, it::remove);
                            }
                            it.next();
                        }
                    },
                    () -> assertEquals(1, csv.size()),
                    () -> assertEquals("'Hello,;\"\"\"second,\"\"\";world;!';\n", csv.toString())
            );
        }

        @Test
        @DisplayName("Iterator for each remaining is working")
        void iterableForEachRemaining() {
            var lst = new ArrayList<Row>();
            var csv = csvTemplate();
            var it = csv.iterator();
            it.next();
            it.forEachRemaining(lst::add);
            assertAll("For each remaining working",
                    () -> assertEquals(1, lst.size()),
                    () -> assertEquals(Row.of("'Hello,", "\"second,\"", "world", "!'", ""), lst.get(0))
            );
        }

        @Test
        @DisplayName("Iterator concurrent modifications")
        void iteratorConcurrentModifications() {
            var csv = csvTemplate();
            var it = csv.iterator();
            csv.remove(0);
            assertThrows(ConcurrentModificationException.class, it::next);
        }

        @Test
        @DisplayName("ListIterator is working")
        void listIterator(){
            var csv = csvTemplate();
            var expected = 0;
            var lstItr = csv.listIterator(0);
            while (lstItr.hasNext()){
                assertEquals(expected++, lstItr.nextIndex());
                lstItr.next();
            }
            expected--;
            while (lstItr.hasPrevious()){
                assertEquals(expected--, lstItr.previousIndex());
                lstItr.previous();
            }
            lstItr.add(Row.of("add"));
            assertEquals(3, csv.size());
        }

        @Test
        @DisplayName("ListIterator assertions")
        void listIteratorAssertions(){
            var csv = csvTemplate();
            var lstItr = csv.listIterator();
            var emptyRow = Row.empty();
            assertAll(
                    () -> assertThrows(IllegalStateException.class, lstItr::remove),
                    () -> assertThrows(IllegalStateException.class, () -> lstItr.set(emptyRow)),
                    () -> assertThrows(NullPointerException.class, () -> lstItr.set(null)),
                    () -> assertThrows(NullPointerException.class, () -> lstItr.forEachRemaining(null)),
                    () -> assertThrows(NullPointerException.class, () -> lstItr.add(null))
            );
        }

    }

    @Nested
    @DisplayName("Stream")
    final class StreamTest {

        @Test
        @DisplayName("Stream basic tests")
        void stream() {
            var lst = new ArrayList<Row>();
            var emptyCsv = Csv.empty();
            var csv = csvTemplate();
            csv.addLast(Row.empty());
            csv.stream().filter(Row::isEmpty).forEach(lst::add);
            csv.remove(csv.size() - 1);
            assertAll("Stream basics",
                    () -> assertEquals(2, csv.size()),
                    () -> assertEquals(1, lst.size()),
                    () -> assertDoesNotThrow(emptyCsv::stream)
            );
        }

    }

    @Nested
    @DisplayName("Map")
    final class MapTest {

        @Test
        @DisplayName("Map basic tests")
        void map() {
            var csv = csvTemplate();
            csv.map(r -> {
                r.map(String::toUpperCase);
                return r;
            });
            assertAll("Map basics",
                    () -> assertEquals("""
                                    ""\"HELLO";WORLD;"!;"
                                    'HELLO,;""\"SECOND,""\";WORLD;!';
                                    """
                            , csv.toString()),
                    () -> assertEquals(2, csv.size()),
                    () -> assertDoesNotThrow(() -> csv.map((Object s) -> Row.empty())),
                    () -> assertEquals(0, csv.stream().mapToInt(Row::size).sum())
            );
        }

        @Test
        @DisplayName("Map null assertions")
        void mapAssertions() {
            var csv = csvTemplate();
            assertAll("Map null assertions",
                    () -> assertThrows(NullPointerException.class, () -> csv.map(null)),
                    () -> assertThrows(NullPointerException.class, () -> csv.map(r -> null))
            );
        }

    }

    @Nested
    @DisplayName("Collector")
    final class CollectorTest {

        @Test
        @DisplayName("Collector is working")
        void collector() {
            var csv = Csv.of(
                    Row.of("x"),
                    Row.of("y"),
                    Row.of("z"),
                    Row.of("x"),
                    Row.of("z"),
                    Row.of("y")
            );
            var csv2 = csv.stream()
                    .filter(s -> !s.contains("z"))
                    .collect(Csv.collector());
            var csv3 = csv.stream().collect(Csv.collector());
            assertAll("Collector",
                    () -> assertEquals(Csv.of(
                            Row.of("x"),
                            Row.of("y"),
                            Row.of("x"),
                            Row.of("y")
                    ), csv2),
                    () -> assertEquals(csv, csv3)
            );
        }

        @Test
        @DisplayName("Parallel collector")
        void ParallelCollector() {
            var result = IntStream.range(0, 1000).parallel()
                    .mapToObj(i -> Row.of(String.valueOf(i)))
                    .collect(Csv.collector());
            assertTrue(IntStream.range(0, 1000).allMatch(i -> result.get(i).equals(Row.of(String.valueOf(i)))));
        }

    }

    @Nested
    final class Equals {

        @Test
        @DisplayName("Equals basic tests")
        void equals() {
            var csv1 = csvTemplate();
            var csv2 = csvTemplate();
            assertAll("Equals basic tests",
                    () -> assertNotSame(csv1, csv2),
                    () -> assertEquals(csv1, csv2),
                    () -> {
                        csv1.set(0, Row.empty());
                        assertNotEquals(csv1, csv2);
                    },
                    () -> assertNotEquals(null, csv1)
            );
        }

    }

    @Nested
    final class HashCode {

        @Test
        @DisplayName("HashCode basic tests")
        void hashcode() {
            var csv1 = csvTemplate();
            var csv2 = csvTemplate();
            var csv3 = Csv.empty();
            assertAll("Equals basic tests",
                    () -> assertEquals(csv1.hashCode(), csv2.hashCode()),
                    () -> {
                        csv1.set(0, Row.empty());
                        assertNotEquals(csv1.hashCode(), csv2.hashCode());
                    },
                    () -> assertNotEquals(csv1.hashCode(), csv3.hashCode()),
                    () -> assertNotEquals(csv2.hashCode(), csv3.hashCode())
            );
        }

    }

    @Nested
    @DisplayName("ListIterator")
    final class ListIteratorTests {

        @Test
        @DisplayName("ListIterator with index")
        void testListIteratorWithIndex() {
            var csv = csvTemplate(); // has 2 rows
            var iterator = csv.listIterator(1);

            // test basic navigation
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasPrevious());
            assertEquals(1, iterator.nextIndex());
            assertEquals(0, iterator.previousIndex());

            var nextRow = iterator.next();
            assertEquals(csvTemplate().get(1), nextRow);

            var previousRow = iterator.previous();
            assertEquals(csvTemplate().get(1), previousRow);

            var prevRow2 = iterator.previous();
            assertEquals(csvTemplate().get(0), prevRow2);

            // test set
            var newRow = Row.of("new");
            iterator.set(newRow);
            assertEquals(newRow, csv.get(0));

            // test remove
            iterator.next(); // now at index 1
            iterator.remove();
            assertEquals(1, csv.size());
            assertEquals(csvTemplate().get(1), csv.get(0));

            // test add
            var addedRow = Row.of("added");
            iterator.add(addedRow);
            assertEquals(2, csv.size());
            assertEquals(addedRow, csv.get(0));
            assertEquals(csvTemplate().get(1), csv.get(1));

            // test forEachRemaining
            iterator = csv.listIterator(0);
            var collected = new ArrayList<Row>();
            iterator.forEachRemaining(collected::add);
            assertEquals(2, collected.size());
            assertEquals(addedRow, collected.get(0));
            assertEquals(csvTemplate().get(1), collected.get(1));

            // test assertions
            assertThrows(NullPointerException.class, () -> csv.listIterator(0).set(null));
            assertThrows(NullPointerException.class, () -> csv.listIterator(0).add(null));
            assertThrows(NullPointerException.class, () -> csv.listIterator(0).forEachRemaining(null));
        }

        @Test
        @DisplayName("ListIterator default")
        void testListIteratorDefault() {
            var csv = csvTemplate(); // has 2 rows
            var iterator = csv.listIterator();

            // test basic navigation
            assertTrue(iterator.hasNext());
            assertFalse(iterator.hasPrevious());
            assertEquals(0, iterator.nextIndex());
            assertEquals(-1, iterator.previousIndex());

            var nextRow = iterator.next();
            assertEquals(csvTemplate().get(0), nextRow);

            assertTrue(iterator.hasPrevious());
            assertEquals(nextRow, iterator.previous());
            iterator.next();

            // test set
            var newRow = Row.of("new");
            iterator.set(newRow);
            assertEquals(newRow, csv.get(0));

            // test remove
            iterator.remove();
            assertEquals(1, csv.size());
            assertEquals(csvTemplate().get(1), csv.get(0));

            // test add
            var addedRow = Row.of("added");
            iterator.add(addedRow);
            assertEquals(2, csv.size());
            assertEquals(addedRow, csv.get(0));
            assertEquals(csvTemplate().get(1), csv.get(1));

            // test forEachRemaining
            iterator = csv.listIterator();
            var collected = new ArrayList<Row>();
            iterator.forEachRemaining(collected::add);
            assertEquals(2, collected.size());
            assertEquals(addedRow, collected.get(0));
            assertEquals(csvTemplate().get(1), collected.get(1));

            // test assertions
            assertThrows(NullPointerException.class, () -> csv.listIterator().set(null));
            assertThrows(NullPointerException.class, () -> csv.listIterator().add(null));
            assertThrows(NullPointerException.class, () -> csv.listIterator().forEachRemaining(null));
        }
    }

}
