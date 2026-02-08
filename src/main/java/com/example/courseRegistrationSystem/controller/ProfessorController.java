package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.dto.ProfessorResponse;
import com.example.courseRegistrationSystem.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "교수", description = "교수 관련 API")
@RestController
@RequestMapping("/professors")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(summary = "교수 목록 조회", description = "전체 교수 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<ProfessorResponse>> getProfessors() {
        return ResponseEntity.ok(professorService.findAll());
    }
}
