package com.example.courseRegistrationSystem.repository;

import com.example.courseRegistrationSystem.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.student.id = :studentId AND e.course.baseName = :baseName")
    boolean existsByStudentIdAndCourseBaseName(@Param("studentId") Long studentId, @Param("baseName") String baseName);

    @Query("SELECT COALESCE(SUM(e.course.credits), 0) FROM Enrollment e WHERE e.student.id = :studentId")
    int sumCreditsByStudentId(@Param("studentId") Long studentId);
}
