package com.example.courseRegistrationSystem.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TimetableResponse {
    private Long studentId;
    private String studentName;
    private String semester;
    private int totalCredits;
    private List<TimetableCourse> courses;

    @Getter
    @Builder
    public static class TimetableCourse {
        private Long enrollmentId;
        private Long courseId;
        private String courseName;
        private String professorName;
        private int credits;
        private String schedule;
    }
}
