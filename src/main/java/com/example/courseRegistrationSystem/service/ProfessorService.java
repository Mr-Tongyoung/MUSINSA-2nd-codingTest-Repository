package com.example.courseRegistrationSystem.service;

import com.example.courseRegistrationSystem.dto.ProfessorResponse;
import com.example.courseRegistrationSystem.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public List<ProfessorResponse> findAll() {
        return professorRepository.findAll().stream()
                .map(ProfessorResponse::from)
                .toList();
    }
}
