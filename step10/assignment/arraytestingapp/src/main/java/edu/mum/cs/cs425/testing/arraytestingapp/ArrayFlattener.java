package edu.mum.cs.cs425.testing.arraytestingapp;

public class ArrayFlattener {

    public int[] flattenArray(int[][] input) {
        if (input == null) return null;
        int total = 0;
        for (var row : input) total += row.length;
        var result = new int[total];
        int idx = 0;
        for (var row : input)
            for (var val : row)
                result[idx++] = val;
        return result;
    }
}
