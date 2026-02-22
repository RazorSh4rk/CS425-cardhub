package edu.mum.cs.cs425.demos.studentrecordsmgmtapp;

import edu.mum.cs.cs425.demos.studentrecordsmgmtapp.model.Student;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MyStudentRecordsMgmtApp {

    public static void main(String[] args) {
        var students = new Student[]{
            new Student(110001, "Dave",   LocalDate.of(1951, 11, 18)),
            new Student(110002, "Anna",   LocalDate.of(1990, 12,  7)),
            new Student(110003, "Erica",  LocalDate.of(1974,  1, 31)),
            new Student(110004, "Carlos", LocalDate.of(2009,  8, 22)),
            new Student(110005, "Bob",    LocalDate.of(1990,  3,  5))
        };

        System.out.println("=== All Students (Ascending by Name) ===");
        printListOfStudents(students);

        System.out.println("\n=== Platinum Alumni (Admitted >= 30 years ago, Descending by Date) ===");
        getListOfPlatinumAlumniStudents(students).forEach(System.out::println);

        System.out.println("\n=== Hello World ===");
        printHelloWorld(new int[]{5, 7, 35, 3, 14, 25, 49, 70});

        System.out.println("\n=== Second Biggest ===");
        System.out.println(findSecondBiggest(new int[]{1, 2, 3, 4, 5}));
        System.out.println(findSecondBiggest(new int[]{19, 9, 11, 0, 12}));
    }

    static void printListOfStudents(Student[] students) {
        Arrays.stream(students)
            .sorted(Comparator.comparing(Student::getName))
            .forEach(System.out::println);
    }

    static List<Student> getListOfPlatinumAlumniStudents(Student[] students) {
        var cutoff = LocalDate.now().minusYears(30);
        return Arrays.stream(students)
            .filter(s -> !s.getDateOfAdmission().isAfter(cutoff))
            .sorted(Comparator.comparing(Student::getDateOfAdmission).reversed())
            .collect(Collectors.toList());
    }

    static void printHelloWorld(int[] arr) {
        for (var n : arr) {
            if (n % 5 == 0 && n % 7 == 0) 
                System.out.println("HelloWorld");
            else if (n % 5 == 0)
                System.out.println("Hello");
            else if (n % 7 == 0)
                System.out.println("World");
        }
    }

    static int findSecondBiggest(int[] arr) {
        var biggest = Integer.MIN_VALUE;
        var second  = Integer.MIN_VALUE;
        for (var n : arr) {
            if(n > biggest) { second = biggest; biggest = n; }
            else if (n > second)  { second = n; }
        }
        return second;
    }
}
