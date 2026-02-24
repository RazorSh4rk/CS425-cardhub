package edu.mum.cs.cs425.studentmgmt.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "classrooms")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long classroomId;

    @Column(nullable = false)
    private String buildingName;

    @Column(nullable = false)
    private String roomNumber;
}
