package com.example.courseRegistrationSystem.dto;

import com.example.courseRegistrationSystem.entity.Course;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseResponse {
    private Long id;
    private String name;
    private int credits;
    private int capacity;
    private int enrolled;
    private String schedule;
    private String professorName;
    private String departmentName;

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .credits(course.getCredits())
                .capacity(course.getCapacity())
                .enrolled(course.getEnrolled())
                .schedule(course.getSchedule())
                .professorName(course.getProfessor().getName())
                .departmentName(course.getDepartment().getName())
                .build();
    }
}
