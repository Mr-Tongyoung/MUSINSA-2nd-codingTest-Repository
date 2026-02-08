package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.controller.docs.StudentControllerDocs;
import com.example.courseRegistrationSystem.dto.StudentResponse;
import com.example.courseRegistrationSystem.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController implements StudentControllerDocs {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getStudents() {
        return ResponseEntity.ok(studentService.findAll());
    }
}
