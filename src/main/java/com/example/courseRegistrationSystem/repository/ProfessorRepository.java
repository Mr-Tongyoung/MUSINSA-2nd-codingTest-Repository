package com.example.courseRegistrationSystem.repository;

import com.example.courseRegistrationSystem.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
