package io.github.sekelenao.smallcsv.internal;

import io.github.sekelenao.smallcsv.api.exception.InvalidCsvValueException;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Objects;

public final class Assertions {

    private Assertions() {
        throw new AssertionError("You cannot instantiate this class.");
    }

    public static void positive(int integer) {
        if (integer < 0) throw new IllegalArgumentException("Must be positive");
    }

    public static void isValidChar(int character) {
        switch (character) {
            case '\n' -> throw new InvalidCsvValueException("\\n");
            case '\r' -> throw new InvalidCsvValueException("\\r");
            case '\b' -> throw new InvalidCsvValueException("\\b");
            case '\f' -> throw new InvalidCsvValueException("\\f");
            case '\0' -> throw new InvalidCsvValueException("\\0");
            default -> {/*pass*/}
        }
    }

    public static void validPosition(int position, int size) {
        if (position > size || position < 0)
            throw new IndexOutOfBoundsException("Position " + position + " out of bounds for length " + size);
    }

    public static void requireNonNulls(Object... objects) {
        Arrays.stream(objects).forEach(Objects::requireNonNull);
    }

    public static void concurrentModification(int version, int expectedVersion){
        if(version != expectedVersion){
            throw new ConcurrentModificationException();
        }
    }

}
