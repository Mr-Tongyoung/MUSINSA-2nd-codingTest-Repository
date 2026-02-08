package com.example.courseRegistrationSystem.service;

import com.example.courseRegistrationSystem.dto.EnrollmentResponse;
import com.example.courseRegistrationSystem.dto.TimetableResponse;
import com.example.courseRegistrationSystem.entity.Course;
import com.example.courseRegistrationSystem.entity.Enrollment;
import com.example.courseRegistrationSystem.entity.Student;
import com.example.courseRegistrationSystem.exception.BusinessException;
import com.example.courseRegistrationSystem.repository.CourseRepository;
import com.example.courseRegistrationSystem.repository.EnrollmentRepository;
import com.example.courseRegistrationSystem.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final int MAX_CREDITS = 18;

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse enroll(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND", "해당 학생을 찾을 수 없습니다"));

        // 비관적 락으로 Course 행 잠금
        Course course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "해당 강좌를 찾을 수 없습니다"));

        // 1. 중복 신청 검증
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "ALREADY_ENROLLED", "이미 수강신청한 강좌입니다");
        }

        // 2. 정원 초과 검증
        if (course.isFull()) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "CAPACITY_EXCEEDED", "해당 강좌의 정원이 초과되었습니다");
        }

        // 3. 학점 초과 검증
        int currentCredits = enrollmentRepository.sumCreditsByStudentId(studentId);
        if (currentCredits + course.getCredits() > MAX_CREDITS) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "CREDIT_LIMIT_EXCEEDED",
                    "최대 수강 학점(18학점)을 초과합니다");
        }

        // 4. 시간 충돌 검증
        List<Enrollment> existingEnrollments = enrollmentRepository.findByStudentId(studentId);
        for (Enrollment existing : existingEnrollments) {
            if (hasScheduleConflict(existing.getCourse().getSchedule(), course.getSchedule())) {
                throw new BusinessException(
                        HttpStatus.CONFLICT, "SCHEDULE_CONFLICT",
                        "기존 수강 강좌와 시간이 충돌합니다");
            }
        }

        // 수강신청 처리
        course.increaseEnrolled();
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();
        enrollmentRepository.save(enrollment);

        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void cancel(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "해당 수강 기록을 찾을 수 없습니다"));

        // 취소 시에도 비관적 락으로 Course 잠금
        Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "해당 강좌를 찾을 수 없습니다"));

        course.decreaseEnrolled();
        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public TimetableResponse getTimetable(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND", "해당 학생을 찾을 수 없습니다"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        List<TimetableResponse.TimetableCourse> courses = enrollments.stream()
                .map(e -> TimetableResponse.TimetableCourse.builder()
                        .enrollmentId(e.getId())
                        .courseId(e.getCourse().getId())
                        .courseName(e.getCourse().getName())
                        .professorName(e.getCourse().getProfessor().getName())
                        .credits(e.getCourse().getCredits())
                        .schedule(e.getCourse().getSchedule())
                        .build())
                .toList();

        int totalCredits = courses.stream()
                .mapToInt(TimetableResponse.TimetableCourse::getCredits)
                .sum();

        return TimetableResponse.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .semester("2026-1")
                .totalCredits(totalCredits)
                .courses(courses)
                .build();
    }

    /**
     * 두 시간표 문자열의 충돌 여부를 판단한다.
     * 형식: "월 09:00-10:30"
     * 같은 요일이고 시간 구간이 겹치면 충돌
     */
    private boolean hasScheduleConflict(String schedule1, String schedule2) {
        String[] parts1 = schedule1.split(" ");
        String[] parts2 = schedule2.split(" ");

        String day1 = parts1[0];
        String day2 = parts2[0];

        if (!day1.equals(day2)) {
            return false;
        }

        String[] time1 = parts1[1].split("-");
        String[] time2 = parts2[1].split("-");

        int start1 = toMinutes(time1[0]);
        int end1 = toMinutes(time1[1]);
        int start2 = toMinutes(time2[0]);
        int end2 = toMinutes(time2[1]);

        // 경계는 허용: start1 < end2 && start2 < end1
        return start1 < end2 && start2 < end1;
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
