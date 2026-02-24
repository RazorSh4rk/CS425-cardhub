package edu.mum.cs.cs425.studentmgmt;

import edu.mum.cs.cs425.studentmgmt.model.Classroom;
import edu.mum.cs.cs425.studentmgmt.model.Student;
import edu.mum.cs.cs425.studentmgmt.model.Transcript;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.time.LocalDate;

@SpringBootApplication
public class StudentMgmtApp implements CommandLineRunner {

    private final StudentRepository studentRepository;

    public StudentMgmtApp(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(StudentMgmtApp.class, args);
    }

    @Override
    public void run(String... args) {
        // Task 1: save student
        var s1 = new Student();
        s1.setStudentNumber("000-61-0001");
        s1.setFirstName("Anna");
        s1.setMiddleName("Lynn");
        s1.setLastName("Smith");
        s1.setCgpa(3.45);
        s1.setDateOfEnrollment(LocalDate.of(2019, 5, 24));
        saveStudent(s1);

        // Task 2: save student with transcript and classroom
        var transcript = new Transcript();
        transcript.setDegreeTitle("BS Computer Science");

        var classroom = new Classroom();
        classroom.setBuildingName("McLaughlin building");
        classroom.setRoomNumber("M105");

        var s2 = new Student();
        s2.setStudentNumber("000-61-0002");
        s2.setFirstName("Bob");
        s2.setLastName("Jones");
        s2.setDateOfEnrollment(LocalDate.of(2020, 1, 15));
        s2.setTranscript(transcript);
        s2.setClassroom(classroom);
        saveStudent(s2);
    }

    public void saveStudent(Student student) {
        var saved = studentRepository.save(student);
        System.out.println("Student saved: " + saved);
    }
}
