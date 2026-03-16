package edu.mum.cs.cs425.testing.arraytestingapp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayFlattenerTest {

    private final ArrayFlattener arrayFlattener = new ArrayFlattener();

    @Test
    void testFlattenArray_validInput() {
        int[][] input = {{1, 3}, {0}, {4, 5, 9}};
        var result = arrayFlattener.flattenArray(input);
        assertArrayEquals(new int[]{1, 3, 0, 4, 5, 9}, result);
    }

    @Test
    void testFlattenArray_nullInput() {
        assertNull(arrayFlattener.flattenArray(null));
    }
}
