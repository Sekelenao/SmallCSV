package io.github.sekelenao.smallcsv.api;

import io.github.sekelenao.smallcsv.internal.Assertions;
import io.github.sekelenao.smallcsv.internal.CsvFormatter;


import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a CSV (Comma-Separated Values) file consisting of multiple rows.
 * This class provides methods to manipulate and access rows within the CSV.
 *
 * <p>Instances of this class use a default configuration {@link CsvConfiguration#SEMICOLON SEMICOLON} with a semicolon as
 * the delimiter and double quotes for quoting fields. The configuration can be customized as needed.
 *
 * <p> Like individual rows, which implement {@code RandomAccess}, this class guarantees constant time access for random retrieval of rows.
 *
 * <p>Null values are not permitted in instances of this class, ensuring consistency in data processing.
 */
public class Csv implements Iterable<Row>, RandomAccess {

    /**
     * The internal list of rows in this CSV.
     * Each element in the list represents a single row.
     */
    private final ArrayList<Row> rows;

    /**
     * The configuration used for formatting this CSV data.
     */
    private CsvConfiguration config = CsvConfiguration.SEMICOLON;

    /**
     * Constructs an Csv instance initialized with the specified rows and the default configuration
     * {@link CsvConfiguration#SEMICOLON SEMICOLON} with a semicolon as the delimiter and double quotes for quoting fields.
     *
     * <p>The provided array of rows is copied, so subsequent changes to the array do not affect this Csv instance.
     *
     * @param rows the array of rows to initialize the CSV
     * @throws NullPointerException if the specified array or any of its elements is null
     */
    private Csv(Row... rows) {
        Objects.requireNonNull(rows);
        this.rows = new ArrayList<>(rows.length);
        for (var row : rows) {
            Objects.requireNonNull(row);
            this.rows.add(row);
        }
    }

    /**
     * Constructs a new Csv object with the specified collection of rows.
     *
     * @param rows the collection of Row objects to be included in the CSV.
     *             Must not be null.
     * @throws NullPointerException if the provided rows collection is null.
     */
    private Csv(Collection<Row> rows) {
        Objects.requireNonNull(rows);
        this.rows = new ArrayList<>(rows.size());
        for (var row : rows) {
            Objects.requireNonNull(row);
            this.rows.add(row);
        }
    }

    /**
     * Creates an empty Csv instance with the default configuration {@link CsvConfiguration#SEMICOLON SEMICOLON} with
     * a semicolon as the delimiter and double quotes for quoting fields.
     *
     * @return a new empty Csv instance
     */
    public static Csv empty() {
        return new Csv();
    }

    /**
     * Creates a Csv instance initialized with the specified rows and the default configuration
     * {@link CsvConfiguration#SEMICOLON SEMICOLON} with a semicolon as the delimiter and double quotes for quoting fields.
     *
     * <p>The provided array of rows is copied, so subsequent changes to the array do not affect this Csv instance.
     *
     * @param rows the array of rows to initialize the CSV
     * @return a new Csv instance containing the specified rows
     * @throws NullPointerException if the specified array or any of its elements is null
     */
    public static Csv of(Row... rows) {
        return new Csv(rows);
    }

    /**
     * Creates a Csv instance initialized with the specified collection of rows and the default configuration
     * {@link CsvConfiguration#SEMICOLON SEMICOLON} with a semicolon as the delimiter and double quotes for quoting fields.
     *
     * @param rows the collection of Row objects to be included in the CSV.
     *             Must not be null.
     * @return a new Csv instance containing the specified rows
     * @throws NullPointerException if the provided rows collection is null or contains null elements
     */
    public static Csv of(Collection<Row> rows) {
        return new Csv(rows);
    }

    /**
     * Creates a Csv instance initialized with the specified iterable of rows and the default configuration
     * {@link CsvConfiguration#SEMICOLON SEMICOLON} with a semicolon as the delimiter and double quotes for quoting fields.
     *
     * <p>The provided iterable of rows is copied, so later changes to the iterable do not affect this Csv instance.
     *
     * @param rows the iterable of rows to initialize the CSV
     * @return a new Csv instance containing the specified rows
     * @throws NullPointerException if the specified iterable or any of its elements is null
     */
    public static Csv of(Iterable<Row> rows) {
        Objects.requireNonNull(rows);
        var list = new ArrayList<Row>();
        for (var row : rows) {
            Objects.requireNonNull(row);
            list.add(row);
        }
        return new Csv(list);
    }

    /**
     * Sets the configuration for this Csv instance.
     *
     * @param configuration the new configuration to be applied
     * @return this Csv instance with the updated configuration
     * @throws NullPointerException if the specified configuration is null
     */
    public Csv configure(CsvConfiguration configuration) {
        this.config = Objects.requireNonNull(configuration);
        return this;
    }

    /**
     * Returns the current configuration of this Csv instance.
     *
     * @return the current configuration
     */
    public CsvConfiguration configuration() {
        return config;
    }

    /**
     * Returns the number of rows in this Csv instance.
     *
     * @return the number of rows
     */
    public int size() {
        return rows.size();
    }

    /**
     * Returns {@code true} if this Csv instance contains no rows.
     *
     * @return {@code true} if this Csv contains no rows, {@code false} otherwise
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    /**
     * Adds a single row to the end of this Csv instance.
     *
     * @param row the row to be added
     * @throws NullPointerException if the specified row is null
     */
    public void addLast(Row row) {
        Objects.requireNonNull(row);
        rows.addLast(row);
    }

    /**
     * Adds a single row to the beginning of this Csv instance.
     *
     * @param row the row to be added
     * @throws NullPointerException if the specified row is null
     */
    public void addFirst(Row row){
        Objects.requireNonNull(row);
        rows.addFirst(row);
    }

    /**
     * Adds all specified rows to this Csv instance.
     * The provided array of rows is copied, so later changes to the array do not affect this Csv instance.
     *
     * @param rows the array of rows to be added
     * @throws NullPointerException if the specified array or any of its elements is null
     */
    public void addAll(Row... rows) {
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            this.rows.add(row);
        }
    }

    /**
     * Adds all rows from the specified iterable to this Csv instance.
     * The provided iterable of rows is copied, so subsequent changes to the iterable do not affect this Csv instance.
     *
     * @param rows the iterable of rows to be added
     * @throws NullPointerException if the specified iterable or any of its elements is null
     */
    public void addAll(Iterable<Row> rows) {
        Objects.requireNonNull(rows);
        for (var row : rows) {
            Objects.requireNonNull(row);
            this.rows.add(row);
        }
    }

    /**
     * Inserts a single row at the specified position in this Csv instance.
     *
     * <p><strong>Note:</strong> This method does not provide constant-time access (O(1)) because it requires
     * shifting elements in the underlying array.
     *
     * @param position the position at which the row is to be inserted
     * @param row      the row to be inserted
     * @throws IllegalArgumentException if the position is invalid
     * @throws NullPointerException     if the specified row is null
     */
    public void insert(int position, Row row) {
        Assertions.validPosition(position, rows.size());
        Objects.requireNonNull(row);
        rows.add(position, row);
    }

    /**
     * Inserts all specified rows at the specified position in this Csv instance.
     * The provided array of rows is copied, so subsequent changes to the array do not affect this Csv instance.
     *
     * <p><strong>Note:</strong> This method does not provide constant-time access (O(1)) because it requires
     * shifting elements in the underlying array.
     *
     * @param position the position at which the rows are to be inserted
     * @param rows     the array of rows to be inserted
     * @throws IllegalArgumentException if the position is invalid
     * @throws NullPointerException     if the specified array or any of its elements is null
     */
    public void insertAll(int position, Row... rows) {
        Assertions.validPosition(position, this.rows.size());
        Objects.requireNonNull(rows);
        var lstItr = this.rows.listIterator(position);
        for (var row : rows) {
            Objects.requireNonNull(row);
            lstItr.add(row);
        }
    }

    /**
     * Inserts all rows from the specified iterable at the specified position in this Csv instance.
     * The provided iterable of rows is copied, so subsequent changes to the iterable do not affect this Csv instance.
     *
     * <p><strong>Note:</strong> This method does not provide constant-time access (O(1)) because it requires
     * shifting elements in the underlying array.
     *
     * @param position the position at which the rows are to be inserted
     * @param rows     the iterable of rows to be inserted
     * @throws IllegalArgumentException if the position is invalid
     * @throws NullPointerException     if the specified iterable or any of its elements is null
     */
    public void insertAll(int position, Iterable<Row> rows) {
        Assertions.validPosition(position, this.rows.size());
        Objects.requireNonNull(rows);
        var lstItr = this.rows.listIterator(position);
        for (var row : rows) {
            Objects.requireNonNull(row);
            lstItr.add(row);
        }
    }

    /**
     * Replaces the row at the specified position in this Csv instance with the specified row.
     *
     * @param index the index of the row to replace
     * @param row   the row to be stored at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws NullPointerException      if the specified row is null
     */
    public void set(int index, Row row) {
        Objects.checkIndex(index, rows.size());
        Objects.requireNonNull(row);
        rows.set(index, row);
    }

    /**
     * Returns the row at the specified position in this Csv instance.
     *
     * @param index the index of the row to return
     * @return the row at the specified position in this Csv instance
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Row get(int index) {
        Objects.checkIndex(index, rows.size());
        return rows.get(index);
    }

    /**
     * Returns the first row in this Csv instance.
     *
     * @return the first row in this Csv instance
     * @throws NoSuchElementException if this Csv instance is empty
     */
    public Row getFirst() {
        if (rows.isEmpty()) throw new NoSuchElementException();
        return rows.getFirst();
    }

    /**
     * Returns the last row in this Csv instance.
     *
     * @return the last row in this Csv instance
     * @throws NoSuchElementException if this Csv instance is empty
     */
    public Row getLast() {
        if (rows.isEmpty()) throw new NoSuchElementException();
        return rows.getLast();
    }

    /**
     * Removes the row at the specified position in this Csv instance.
     *
     * <p><strong>Note:</strong> This method does not provide constant-time access (O(1)) because it requires
     * shifting elements in the underlying array.
     *
     * @param index the index of the row to be removed
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public void remove(int index) {
        Objects.checkIndex(index, rows.size());
        rows.remove(index);
    }

    /**
     * Removes the first row from this Csv instance.
     *
     * @throws NoSuchElementException if this Csv instance is empty
     */
    public void removeFirst() {
        if (rows.isEmpty()) throw new NoSuchElementException();
        rows.removeFirst();
    }

    /**
     * Removes the last row from this Csv instance.
     *
     * @throws NoSuchElementException if this Csv instance is empty
     */
    public void removeLast() {
        if (rows.isEmpty()) throw new NoSuchElementException();
        rows.removeLast();
    }

    /**
     * Removes all rows from this Csv instance that satisfy the given predicate.
     *
     * @param filter the predicate used to filter rows
     * @return {@code true} if any rows were removed as a result of this call, {@code false} otherwise
     * @throws NullPointerException if the specified predicate is null
     */
    public boolean removeIf(Predicate<? super Row> filter) {
        return rows.removeIf(Objects.requireNonNull(filter));
    }

    /**
     * Returns {@code true} if this Csv instance contains the specified element.
     * More formally, returns {@code true} if and only if this Csv instance contains
     * at least one element {@code e} such that {@code Objects.equals(o, e)}.
     *
     * @param object the object to check for containment in this Csv instance
     * @return {@code true} if this Csv instance contains the specified object, {@code false} otherwise
     */
    public boolean contains(Object object) {
        return object != null && rows.stream().anyMatch(object::equals);
    }

    /**
     * Returns a list iterator over the rows in this Csv instance, starting at the specified position in the list.
     * The specified index indicates the first row that would be returned by an initial call to {@code next}.
     * An initial call to {@code previous} would return the row with the specified index minus one.
     *
     * @param index the index of the first row to be returned by the list iterator
     * @return a list iterator over the rows in this Csv instance, starting at the specified position in the list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public ListIterator<Row> listIterator(int index) {
        Objects.checkIndex(index, rows.size());
        return new ListIterator<>() {

            private final ListIterator<Row> lstItr = rows.listIterator(index);

            @Override
            public boolean hasNext() {
                return lstItr.hasNext();
            }

            @Override
            public Row next() {
                return lstItr.next();
            }

            @Override
            public boolean hasPrevious() {
                return lstItr.hasPrevious();
            }

            @Override
            public Row previous() {
                return lstItr.previous();
            }

            @Override
            public int nextIndex() {
                return lstItr.nextIndex();
            }

            @Override
            public int previousIndex() {
                return lstItr.previousIndex();
            }

            @Override
            public void remove() {
                lstItr.remove();
            }

            @Override
            public void set(Row row) {
                Objects.requireNonNull(row);
                lstItr.set(row);
            }

            @Override
            public void add(Row row) {
                Objects.requireNonNull(row);
                lstItr.add(row);
            }

            @Override
            public void forEachRemaining(Consumer<? super Row> action) {
                Objects.requireNonNull(action);
                lstItr.forEachRemaining(action);
            }

        };
    }

    /**
     * Returns a list iterator over the rows in this Csv instance, starting at the beginning of the list.
     *
     * @return a list iterator over the rows in this Csv instance, starting at the beginning of the list
     */
    public ListIterator<Row> listIterator() {
        return new ListIterator<>() {

            private final ListIterator<Row> lstItr = rows.listIterator();

            @Override
            public boolean hasNext() {
                return lstItr.hasNext();
            }

            @Override
            public Row next() {
                return lstItr.next();
            }

            @Override
            public boolean hasPrevious() {
                return lstItr.hasPrevious();
            }

            @Override
            public Row previous() {
                return lstItr.previous();
            }

            @Override
            public int nextIndex() {
                return lstItr.nextIndex();
            }

            @Override
            public int previousIndex() {
                return lstItr.previousIndex();
            }

            @Override
            public void remove() {
                lstItr.remove();
            }

            @Override
            public void set(Row row) {
                Objects.requireNonNull(row);
                lstItr.set(row);
            }

            @Override
            public void add(Row row) {
                Objects.requireNonNull(row);
                lstItr.add(row);
            }

            @Override
            public void forEachRemaining(Consumer<? super Row> action) {
                Objects.requireNonNull(action);
                lstItr.forEachRemaining(action);
            }

        };
    }

    /**
     * Returns an iterator over the rows in this Csv instance, starting at the beginning of the list.
     *
     * @return an iterator over the rows in this Csv instance, starting at the beginning of the list
     */
    @Override
    public Iterator<Row> iterator() {
        return listIterator();
    }

    /**
     * Performs the given action for each row in this Csv instance until all rows have been processed or the action
     * throws an exception.
     *
     * @param action the action to be performed for each row
     * @throws NullPointerException if the specified action is null
     */
    @Override
    public void forEach(Consumer<? super Row> action) {
        Objects.requireNonNull(action);
        rows.forEach(action);
    }

    /**
     * Returns a {@code Spliterator} over the rows in this Csv instance.
     *
     * <p><strong>Note:</strong> The {@code Spliterator} provided by this method is {@link Spliterator#NONNULL},
     * {@link Spliterator#SIZED}, and {@link Spliterator#ORDERED}.
     *
     * @return a {@code Spliterator} over the rows in this Csv instance
     */
    @Override
    public Spliterator<Row> spliterator() {
        return Spliterators.spliterator(rows, Spliterator.NONNULL | Spliterator.SIZED | Spliterator.ORDERED);
    }

    /**
     * Returns a sequential {@code Stream} over the rows in this Csv instance.
     * The Stream traverses the elements of the row in the order they were added.
     *
     * @return a sequential {@code Stream} over the rows in this Csv instance
     */
    public Stream<Row> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Applies the given function to each row in this Csv instance, replacing the row with the result of the function.
     *
     * @param mapper the function to apply to each row
     * @throws NullPointerException if the specified mapper is null, or if the mapper returns null for any row
     */
    public void map(Function<? super Row, Row> mapper) {
        Objects.requireNonNull(mapper);
        var lstItr = rows.listIterator();
        while (lstItr.hasNext()) {
            var mappedValue = mapper.apply(lstItr.next());
            Objects.requireNonNull(mappedValue);
            lstItr.set(mappedValue);
        }
    }

    /**
     * Returns a {@code Collector} that accumulates input elements into a new Csv instance.
     *
     * @return a {@code Collector} that accumulates input elements into a new Csv instance
     */
    public static Collector<Row, ?, Csv> collector() {
        return Collector.of(
                Csv::new, Csv::addAll,
                (csv1, csv2) -> {
                    csv1.addAll(csv2);
                    return csv1;
                },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }


    /**
     * Indicates whether some other object is "equal to" this Csv instance.
     *
     * <p>This method returns {@code true} if the specified object is also a Csv instance,
     * both instances have the same number of rows, and all corresponding pairs of rows are
     * equal. In other words, two Csv instances are defined to be equal if they contain
     * the same rows in the same order.
     *
     * @param other the reference object with which to compare
     * @return {@code true} if this Csv instance is equal to the specified object, {@code false} otherwise
     * @throws NullPointerException if the specified object is null
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Csv otherCsv
                && otherCsv.rows.size() == rows.size()
                && otherCsv.rows.equals(rows);
    }

    /**
     * Returns the hash code value for this Csv instance.
     *
     * <p>This method computes a hash code based on the number of rows and the hash codes of all rows.
     *
     * @return the hash code value for this Csv instance
     */
    @Override
    public int hashCode() {
        int hash = rows.size();
        for (var row : rows) {
            hash ^= row.hashCode();
        }
        return hash;
    }

    /**
     * Returns a string representation of this Csv instance.
     *
     * <p>This method returns a string containing the CSV representation of all rows in this Csv instance,
     * separated by newlines. The CSV formatting is performed using the current configuration of this Csv instance.
     *
     * @return a string representation of this Csv instance
     */
    @Override
    public String toString() {
        var formatter = new CsvFormatter(config);
        var builder = new StringBuilder();
        for (var row : rows) {
            builder.append(formatter.toCsvString(row)).append("\n");
        }
        return builder.toString();
    }

}
