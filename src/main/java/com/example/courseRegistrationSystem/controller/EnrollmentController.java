package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.controller.docs.EnrollmentControllerDocs;
import com.example.courseRegistrationSystem.dto.EnrollmentRequest;
import com.example.courseRegistrationSystem.dto.EnrollmentResponse;
import com.example.courseRegistrationSystem.dto.TimetableResponse;
import com.example.courseRegistrationSystem.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EnrollmentController implements EnrollmentControllerDocs {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(
                request.getStudentId(), request.getCourseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable Long enrollmentId) {
        enrollmentService.cancel(enrollmentId);
        return ResponseEntity.ok(Map.of("message", "수강취소가 완료되었습니다"));
    }

    @GetMapping("/students/{studentId}/timetable")
    public ResponseEntity<TimetableResponse> getTimetable(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getTimetable(studentId));
    }
}
