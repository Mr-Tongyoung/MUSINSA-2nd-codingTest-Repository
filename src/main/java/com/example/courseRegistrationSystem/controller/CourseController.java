package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.controller.docs.CourseControllerDocs;
import com.example.courseRegistrationSystem.dto.CourseResponse;
import com.example.courseRegistrationSystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController implements CourseControllerDocs {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses(
            @RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(courseService.findByDepartmentId(departmentId));
        }
        return ResponseEntity.ok(courseService.findAll());
    }
}
