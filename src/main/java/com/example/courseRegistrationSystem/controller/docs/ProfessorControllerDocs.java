package com.example.courseRegistrationSystem.controller.docs;

import com.example.courseRegistrationSystem.dto.ProfessorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "교수", description = "교수 관련 API")
public interface ProfessorControllerDocs {

    @Operation(summary = "교수 목록 조회", description = "전체 교수 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<List<ProfessorResponse>> getProfessors();
}
