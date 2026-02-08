package com.example.courseRegistrationSystem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int enrolled;

    @Column(nullable = false)
    private String schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Builder
    public Course(String name, int credits, int capacity, String schedule,
                  Professor professor, Department department) {
        this.name = name;
        this.credits = credits;
        this.capacity = capacity;
        this.enrolled = 0;
        this.schedule = schedule;
        this.professor = professor;
        this.department = department;
    }

    public void increaseEnrolled() {
        this.enrolled++;
    }

    public void decreaseEnrolled() {
        if (this.enrolled > 0) {
            this.enrolled--;
        }
    }

    public boolean isFull() {
        return this.enrolled >= this.capacity;
    }
}