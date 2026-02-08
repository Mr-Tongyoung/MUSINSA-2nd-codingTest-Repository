package com.example.courseRegistrationSystem.service;

import com.example.courseRegistrationSystem.dto.CourseResponse;
import com.example.courseRegistrationSystem.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(CourseResponse::from)
                .toList();
    }

    public List<CourseResponse> findByDepartmentId(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(CourseResponse::from)
                .toList();
    }
}
