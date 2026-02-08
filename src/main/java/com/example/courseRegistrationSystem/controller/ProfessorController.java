package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.dto.ProfessorResponse;
import com.example.courseRegistrationSystem.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/professors")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @GetMapping
    public ResponseEntity<List<ProfessorResponse>> getProfessors() {
        return ResponseEntity.ok(professorService.findAll());
    }
}
