package edu.mum.cs.cs425.studentmgmt.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "transcripts")
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transcriptId;

    @Column(nullable = false)
    private String degreeTitle;
}
