package com.example.courseRegistrationSystem.service;

import com.example.courseRegistrationSystem.dto.CourseResponse;
import com.example.courseRegistrationSystem.exception.BusinessException;
import com.example.courseRegistrationSystem.repository.CourseRepository;
import com.example.courseRegistrationSystem.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(CourseResponse::from)
                .toList();
    }

    public List<CourseResponse> findByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND, "DEPARTMENT_NOT_FOUND", "해당 학과를 찾을 수 없습니다");
        }
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(CourseResponse::from)
                .toList();
    }
}
