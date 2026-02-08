package com.example.courseRegistrationSystem.dto;

import com.example.courseRegistrationSystem.entity.Enrollment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    private int credits;
    private String schedule;

    public static EnrollmentResponse from(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getName())
                .credits(enrollment.getCourse().getCredits())
                .schedule(enrollment.getCourse().getSchedule())
                .build();
    }
}
