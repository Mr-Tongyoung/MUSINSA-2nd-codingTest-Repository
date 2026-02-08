package com.example.courseRegistrationSystem.dto;

import com.example.courseRegistrationSystem.entity.Professor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfessorResponse {
    private Long id;
    private String name;
    private String departmentName;

    public static ProfessorResponse from(Professor professor) {
        return ProfessorResponse.builder()
                .id(professor.getId())
                .name(professor.getName())
                .departmentName(professor.getDepartment().getName())
                .build();
    }
}
