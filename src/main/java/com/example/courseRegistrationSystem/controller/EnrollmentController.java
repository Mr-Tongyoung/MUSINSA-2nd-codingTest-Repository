package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.dto.EnrollmentRequest;
import com.example.courseRegistrationSystem.dto.EnrollmentResponse;
import com.example.courseRegistrationSystem.dto.ErrorResponse;
import com.example.courseRegistrationSystem.dto.TimetableResponse;
import com.example.courseRegistrationSystem.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "수강신청", description = "수강신청/취소 및 시간표 조회 API")
@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "수강신청", description = "학생이 강좌에 수강신청합니다. 정원 초과, 학점 초과, 시간 충돌, 중복 신청 시 실패합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "수강신청 성공"),
            @ApiResponse(responseCode = "404", description = "학생 또는 강좌를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "정원 초과 / 학점 초과 / 시간 충돌 / 중복 신청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(
                request.getStudentId(), request.getCourseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "수강취소", description = "수강신청을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수강취소 성공"),
            @ApiResponse(responseCode = "404", description = "수강 기록을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<Map<String, String>> cancel(
            @Parameter(description = "수강신청 ID") @PathVariable Long enrollmentId) {
        enrollmentService.cancel(enrollmentId);
        return ResponseEntity.ok(Map.of("message", "수강취소가 완료되었습니다"));
    }

    @Operation(summary = "내 시간표 조회", description = "해당 학생의 이번 학기 시간표를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/students/{studentId}/timetable")
    public ResponseEntity<TimetableResponse> getTimetable(
            @Parameter(description = "학생 ID") @PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getTimetable(studentId));
    }
}
