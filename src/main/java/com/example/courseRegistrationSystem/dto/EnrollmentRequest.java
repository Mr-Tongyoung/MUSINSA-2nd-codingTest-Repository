package com.example.courseRegistrationSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "학생 ID는 필수입니다")
    @Schema(description = "학생 ID", example = "1")
    private Long studentId;

    @NotNull(message = "강좌 ID는 필수입니다")
    @Schema(description = "강좌 ID", example = "1")
    private Long courseId;
}
