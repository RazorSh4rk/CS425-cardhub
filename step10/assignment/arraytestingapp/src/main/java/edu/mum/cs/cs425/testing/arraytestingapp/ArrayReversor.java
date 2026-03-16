package edu.mum.cs.cs425.testing.arraytestingapp;

public class ArrayReversor {

    private final ArrayFlattenerService arrayFlattenerService;

    public ArrayReversor(ArrayFlattenerService arrayFlattenerService) {
        this.arrayFlattenerService = arrayFlattenerService;
    }

    public int[] reverseArray(int[][] input) {
        var flattened = arrayFlattenerService.flattenArray(input);
        if (flattened == null) return null;
        var result = new int[flattened.length];
        for (int i = 0; i < flattened.length; i++)
            result[i] = flattened[flattened.length - 1 - i];
        return result;
    }
}
