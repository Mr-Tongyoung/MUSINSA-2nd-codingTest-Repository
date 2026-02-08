package com.example.courseRegistrationSystem.repository;

import com.example.courseRegistrationSystem.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
