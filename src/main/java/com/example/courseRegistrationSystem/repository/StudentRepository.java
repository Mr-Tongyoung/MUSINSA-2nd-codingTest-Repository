package com.example.courseRegistrationSystem.repository;

import com.example.courseRegistrationSystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
