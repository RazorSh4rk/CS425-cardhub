package edu.mum.cs.cs425.eregistrar.controller;

import edu.mum.cs.cs425.eregistrar.model.Student;
import edu.mum.cs.cs425.eregistrar.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;

    @GetMapping
    public List<Student> list(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search);
        }
        return studentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable Long id) {
        return studentRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public Student create(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student student) {
        student.setStudentId(id);
        return studentRepository.save(student);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        studentRepository.deleteById(id);
    }
}
