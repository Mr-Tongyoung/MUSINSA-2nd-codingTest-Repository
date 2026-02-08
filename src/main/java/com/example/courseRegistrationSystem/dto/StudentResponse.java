package com.example.courseRegistrationSystem.dto;

import com.example.courseRegistrationSystem.entity.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String studentNumber;
    private int grade;
    private String departmentName;

    public static StudentResponse from(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .studentNumber(student.getStudentNumber())
                .grade(student.getGrade())
                .departmentName(student.getDepartment().getName())
                .build();
    }
}
