package io.github.sekelenao.smallcsv.test;

import io.github.sekelenao.smallcsv.internal.Assertions;
import io.github.sekelenao.smallcsv.api.exception.InvalidCsvValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Assertions Test Suite")
class AssertionsTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Constructor is private and throws AssertionError")
        void privateConstructor() throws NoSuchMethodException {
            var constructor = Assertions.class.getDeclaredConstructor();
            assertThrows(IllegalAccessException.class, constructor::newInstance);
            constructor.setAccessible(true);
            assertThrows(InvocationTargetException.class, constructor::newInstance);
        }

    }

    @Nested
    @DisplayName("Positive")
    class Positive {

        @Test
        @DisplayName("Should throw IllegalArgumentException when value is negative")
        void shouldThrowExceptionWhenNegative() {
            assertThrows(IllegalArgumentException.class, () -> Assertions.positive(-1));
            assertThrows(IllegalArgumentException.class, () -> Assertions.positive(-100));
        }

        @Test
        @DisplayName("Should not throw when value is positive or zero")
        void shouldNotThrowWhenPositiveOrZero() {
            assertAll(
                () -> assertDoesNotThrow(() -> Assertions.positive(0)),
                () -> assertDoesNotThrow(() -> Assertions.positive(1)),
                () -> assertDoesNotThrow(() -> Assertions.positive(100))
            );
        }

    }

    @Nested
    @DisplayName("IsValidChar")
    class IsValidChar {

        @ParameterizedTest
        @ValueSource(chars = {'\n', '\r', '\b', '\f', '\0'})
        @DisplayName("Should throw InvalidCsvValueException for invalid characters")
        void shouldThrowExceptionForInvalidChars(char invalidChar) {
            assertThrows(InvalidCsvValueException.class, () -> Assertions.isValidChar(invalidChar));
        }

        @ParameterizedTest
        @ValueSource(chars = {'a', 'A', '1', ';', ',', ' ', '\t'})
        @DisplayName("Should not throw for valid characters")
        void shouldNotThrowForValidChars(char validChar) {
            assertDoesNotThrow(() -> Assertions.isValidChar(validChar));
        }

    }

    @Nested
    @DisplayName("ValidPosition")
    class ValidPosition {

        @Test
        @DisplayName("Should throw IndexOutOfBoundsException when position is out of bounds")
        void shouldThrowExceptionWhenOutOfBounds() {
            assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> Assertions.validPosition(-1, 5)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> Assertions.validPosition(6, 5))
            );
        }

        @Test
        @DisplayName("Should not throw when position is within bounds")
        void shouldNotThrowWhenWithinBounds() {
            assertAll(
                () -> assertDoesNotThrow(() -> Assertions.validPosition(0, 5)),
                () -> assertDoesNotThrow(() -> Assertions.validPosition(3, 5)),
                () -> assertDoesNotThrow(() -> Assertions.validPosition(5, 5)),
                () -> assertDoesNotThrow(() -> Assertions.validPosition(0, 0))
            );
        }

    }

    @Nested
    @DisplayName("RequireNonNulls")
    class RequireNonNulls {

        @Test
        @DisplayName("Should throw NullPointerException when any argument is null")
        void shouldThrowExceptionWhenAnyArgIsNull() {
            assertAll(
                () -> assertThrows(NullPointerException.class, () -> Assertions.requireNonNulls((Object) null)),
                () -> assertThrows(NullPointerException.class, () -> Assertions.requireNonNulls("ok", null)),
                () -> assertThrows(NullPointerException.class, () -> Assertions.requireNonNulls(null, "ok")),
                () -> assertThrows(NullPointerException.class, () -> Assertions.requireNonNulls("ok1", null, "ok2"))
            );
        }

        @Test
        @DisplayName("Should not throw when all arguments are non-null")
        void shouldNotThrowWhenAllArgsAreNonNull() {
            assertAll(
                () -> assertDoesNotThrow(() -> Assertions.requireNonNulls()),
                () -> assertDoesNotThrow(() -> Assertions.requireNonNulls("onlyOne")),
                () -> assertDoesNotThrow(() -> Assertions.requireNonNulls("one", "two", 3))
            );
        }

    }

    @Nested
    @DisplayName("ConcurrentModification")
    class ConcurrentModification {

        @Test
        @DisplayName("Should throw ConcurrentModificationException when version does not match expected version")
        void shouldThrowExceptionWhenVersionMismatch() {
            assertThrows(ConcurrentModificationException.class, () -> Assertions.concurrentModification(1, 2));
        }

        @Test
        @DisplayName("Should not throw when version matches expected version")
        void shouldNotThrowWhenVersionMatches() {
            assertDoesNotThrow(() -> Assertions.concurrentModification(5, 5));
        }

    }

}
