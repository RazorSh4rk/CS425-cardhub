package edu.mum.cs.cs425.testing.arraytestingapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArrayReversorTest {

    @Mock
    private ArrayFlattenerService arrayFlattenerService;

    @InjectMocks
    private ArrayReversor arrayReversor;

    @Test
    void testReverseArray_validInput() {
        int[][] input = {{1, 3}, {0}, {4, 5, 9}};
        when(arrayFlattenerService.flattenArray(input)).thenReturn(new int[]{1, 3, 0, 4, 5, 9});

        var result = arrayReversor.reverseArray(input);

        assertArrayEquals(new int[]{9, 5, 4, 0, 3, 1}, result);
        verify(arrayFlattenerService).flattenArray(input);
    }

    @Test
    void testReverseArray_nullInput() {
        when(arrayFlattenerService.flattenArray(null)).thenReturn(null);

        var result = arrayReversor.reverseArray(null);

        assertNull(result);
        verify(arrayFlattenerService).flattenArray(null);
    }
}
