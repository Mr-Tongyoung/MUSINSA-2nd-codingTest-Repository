package com.example.courseRegistrationSystem.service;

import com.example.courseRegistrationSystem.dto.EnrollmentResponse;
import com.example.courseRegistrationSystem.dto.TimetableResponse;
import com.example.courseRegistrationSystem.entity.*;
import com.example.courseRegistrationSystem.exception.BusinessException;
import com.example.courseRegistrationSystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class EnrollmentServiceTest {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    private Department department;
    private Professor professor;
    private Student student;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        professorRepository.deleteAll();
        departmentRepository.deleteAll();

        department = departmentRepository.save(Department.builder().name("컴퓨터공학과").build());
        professor = professorRepository.save(Professor.builder().name("김교수").department(department).build());
        student = studentRepository.save(Student.builder()
                .name("이학생")
                .studentNumber("2023001")
                .grade(2)
                .department(department)
                .build());
    }

    @Test
    @DisplayName("수강신청 성공")
    void enroll_success() {
        Course course = saveCourse("자료구조", 3, 30, "월 09:00-10:30");

        EnrollmentResponse response = enrollmentService.enroll(student.getId(), course.getId());

        assertThat(response.getCourseName()).isEqualTo("자료구조");
        assertThat(response.getCredits()).isEqualTo(3);
        Course updated = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updated.getEnrolled()).isEqualTo(1);
    }

    @Test
    @DisplayName("정원이 가득 찬 강좌에 수강신청하면 CAPACITY_EXCEEDED")
    void enroll_capacityExceeded() {
        Course course = saveCourse("알고리즘", 3, 1, "월 13:00-14:30");

        Student other = studentRepository.save(Student.builder()
                .name("다른학생").studentNumber("2023999").grade(1).department(department).build());
        enrollmentService.enroll(other.getId(), course.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("CAPACITY_EXCEEDED"));
    }

    @Test
    @DisplayName("동일 강좌 중복 수강신청하면 ALREADY_ENROLLED")
    void enroll_alreadyEnrolled() {
        Course course = saveCourse("운영체제", 3, 30, "화 09:00-10:30");
        enrollmentService.enroll(student.getId(), course.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("ALREADY_ENROLLED"));
    }

    @Test
    @DisplayName("18학점 초과 수강신청하면 CREDIT_LIMIT_EXCEEDED")
    void enroll_creditLimitExceeded() {
        // 4학점 * 4 = 16학점 먼저 등록
        saveCourseAndEnroll("과목A", 4, "월 09:00-10:30");
        saveCourseAndEnroll("과목B", 4, "화 09:00-10:30");
        saveCourseAndEnroll("과목C", 4, "수 09:00-10:30");
        saveCourseAndEnroll("과목D", 4, "목 09:00-10:30");

        // 3학점 추가 시도 → 19학점 → 초과
        Course overCourse = saveCourse("과목E", 3, 30, "금 09:00-10:30");

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), overCourse.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("CREDIT_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("시간이 겹치는 강좌를 수강신청하면 SCHEDULE_CONFLICT")
    void enroll_scheduleConflict() {
        saveCourseAndEnroll("기존강좌", 3, "월 09:00-10:30");

        // 겹치는 시간대
        Course conflict = saveCourse("충돌강좌", 3, 30, "월 10:00-11:30");

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), conflict.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("SCHEDULE_CONFLICT"));
    }

    @Test
    @DisplayName("경계 시간(10:30 끝 → 10:30 시작)은 충돌하지 않는다")
    void enroll_boundaryTimeNoConflict() {
        saveCourseAndEnroll("앞강좌", 3, "월 09:00-10:30");

        Course next = saveCourse("뒷강좌", 3, 30, "월 10:30-12:00");

        EnrollmentResponse response = enrollmentService.enroll(student.getId(), next.getId());
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("수강취소 성공 시 enrolled 감소")
    void cancel_success() {
        Course course = saveCourse("취소강좌", 3, 30, "수 13:00-14:30");
        EnrollmentResponse resp = enrollmentService.enroll(student.getId(), course.getId());

        enrollmentService.cancel(resp.getId());

        Course updated = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updated.getEnrolled()).isEqualTo(0);
        assertThat(enrollmentRepository.findById(resp.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 수강 기록 취소 시 ENROLLMENT_NOT_FOUND")
    void cancel_notFound() {
        assertThatThrownBy(() -> enrollmentService.cancel(99999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("ENROLLMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("같은 과목의 다른 분반을 수강신청하면 SAME_COURSE_ENROLLED")
    void enroll_sameCourseEnrolled() {
        Course section1 = saveCourseWithBaseName("자료구조 1분반", "자료구조", 3, 30, "월 09:00-10:30");
        Course section2 = saveCourseWithBaseName("자료구조 2분반", "자료구조", 3, 30, "화 09:00-10:30");

        enrollmentService.enroll(student.getId(), section1.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), section2.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("SAME_COURSE_ENROLLED"));
    }

    @Test
    @DisplayName("시간표 조회 시 수강 중인 강좌와 총 학점을 반환한다")
    void getTimetable_success() {
        saveCourseAndEnroll("자료구조", 3, "월 09:00-10:30");
        saveCourseAndEnroll("알고리즘", 3, "화 09:00-10:30");

        TimetableResponse timetable = enrollmentService.getTimetable(student.getId());

        assertThat(timetable.getStudentName()).isEqualTo("이학생");
        assertThat(timetable.getTotalCredits()).isEqualTo(6);
        assertThat(timetable.getCourses()).hasSize(2);
    }

    private Course saveCourse(String name, int credits, int capacity, String schedule) {
        return courseRepository.save(Course.builder()
                .name(name)
                .credits(credits)
                .capacity(capacity)
                .schedule(schedule)
                .professor(professor)
                .department(department)
                .build());
    }

    private Course saveCourseWithBaseName(String name, String baseName, int credits, int capacity, String schedule) {
        return courseRepository.save(Course.builder()
                .name(name)
                .baseName(baseName)
                .credits(credits)
                .capacity(capacity)
                .schedule(schedule)
                .professor(professor)
                .department(department)
                .build());
    }

    private void saveCourseAndEnroll(String name, int credits, String schedule) {
        Course course = saveCourse(name, credits, 30, schedule);
        enrollmentService.enroll(student.getId(), course.getId());
    }
}
