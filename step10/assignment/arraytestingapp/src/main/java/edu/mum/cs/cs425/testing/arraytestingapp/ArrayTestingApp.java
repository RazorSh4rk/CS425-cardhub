package edu.mum.cs.cs425.testing.arraytestingapp;

import java.util.Arrays;

public class ArrayTestingApp {

    public static void main(String[] args) {
        int[][] input = {{1, 3}, {0}, {4, 5, 9}};

        var flattener = new ArrayFlattener();
        var flattened = flattener.flattenArray(input);
        System.out.println("Input:    " + Arrays.deepToString(input));
        System.out.println("Flattened: " + Arrays.toString(flattened));

        System.out.println("\nRun tests with: mvn test");
    }
}
