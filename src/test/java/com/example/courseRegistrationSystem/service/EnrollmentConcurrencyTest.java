package com.example.courseRegistrationSystem.service;

import com.example.courseRegistrationSystem.entity.*;
import com.example.courseRegistrationSystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnrollmentConcurrencyTest {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    private Department department;
    private Professor professor;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        professorRepository.deleteAll();
        departmentRepository.deleteAll();

        department = departmentRepository.save(Department.builder().name("테스트학과").build());
        professor = professorRepository.save(Professor.builder().name("테스트교수").department(department).build());
    }

    @Test
    @DisplayName("정원 1명인 강좌에 100명이 동시 신청하면 정확히 1명만 성공한다")
    void concurrentEnrollment_capacity1_only1Succeeds() throws InterruptedException {
        // given
        Course course = courseRepository.save(Course.builder()
                .name("동시성테스트강좌")
                .credits(3)
                .capacity(1)
                .schedule("월 09:00-10:30")
                .professor(professor)
                .department(department)
                .build());

        int threadCount = 100;
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            students.add(studentRepository.save(Student.builder()
                    .name("학생" + i)
                    .studentNumber("TEST" + String.format("%05d", i))
                    .grade(1)
                    .department(department)
                    .build()));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long studentId = students.get(i).getId();
            final Long courseId = course.getId();
            executor.submit(() -> {
                try {
                    enrollmentService.enroll(studentId, courseId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getEnrolled()).isEqualTo(1);
        assertThat(enrollmentRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("정원 30명인 강좌에 100명이 동시 신청하면 정확히 30명만 성공한다")
    void concurrentEnrollment_capacity30_only30Succeed() throws InterruptedException {
        // given
        int capacity = 30;
        Course course = courseRepository.save(Course.builder()
                .name("정원30강좌")
                .credits(2)
                .capacity(capacity)
                .schedule("화 10:30-12:00")
                .professor(professor)
                .department(department)
                .build());

        int threadCount = 100;
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            students.add(studentRepository.save(Student.builder()
                    .name("학생" + i)
                    .studentNumber("CAP30" + String.format("%05d", i))
                    .grade(2)
                    .department(department)
                    .build()));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long studentId = students.get(i).getId();
            final Long courseId = course.getId();
            executor.submit(() -> {
                try {
                    enrollmentService.enroll(studentId, courseId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 정원 초과 실패 예상
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(capacity);

        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getEnrolled()).isEqualTo(capacity);
    }

    @Test
    @DisplayName("동일 학생이 같은 강좌를 동시에 중복 신청하면 1번만 성공한다")
    void concurrentDuplicateEnrollment_sameStudent_only1Succeeds() throws InterruptedException {
        // given
        Course course = courseRepository.save(Course.builder()
                .name("중복테스트강좌")
                .credits(3)
                .capacity(50)
                .schedule("수 13:00-14:30")
                .professor(professor)
                .department(department)
                .build());

        Student student = studentRepository.save(Student.builder()
                .name("중복학생")
                .studentNumber("DUP00001")
                .grade(1)
                .department(department)
                .build());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    enrollmentService.enroll(student.getId(), course.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 중복 신청 실패 예상
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);

        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getEnrolled()).isEqualTo(1);
    }

    @Test
    @DisplayName("수강신청과 수강취소가 동시에 발생해도 enrolled 카운트가 정확하다")
    void concurrentEnrollAndCancel_enrolledCountConsistent() throws InterruptedException {
        // given
        Course course = courseRepository.save(Course.builder()
                .name("신청취소동시강좌")
                .credits(3)
                .capacity(50)
                .schedule("목 14:30-16:00")
                .professor(professor)
                .department(department)
                .build());

        // 먼저 10명 수강신청 (순차)
        List<Long> enrollmentIds = Collections.synchronizedList(new ArrayList<>());
        List<Student> preStudents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Student s = studentRepository.save(Student.builder()
                    .name("기존학생" + i)
                    .studentNumber("PRE" + String.format("%05d", i))
                    .grade(1)
                    .department(department)
                    .build());
            preStudents.add(s);
            var resp = enrollmentService.enroll(s.getId(), course.getId());
            enrollmentIds.add(resp.getId());
        }

        // 추가 신청할 학생 20명
        List<Student> newStudents = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            newStudents.add(studentRepository.save(Student.builder()
                    .name("신규학생" + i)
                    .studentNumber("NEW" + String.format("%05d", i))
                    .grade(2)
                    .department(department)
                    .build()));
        }

        ExecutorService executor = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(30);
        AtomicInteger enrollSuccess = new AtomicInteger(0);
        AtomicInteger cancelSuccess = new AtomicInteger(0);

        // when: 10명 취소 + 20명 신청 동시 실행
        for (int i = 0; i < 10; i++) {
            final Long enrollmentId = enrollmentIds.get(i);
            executor.submit(() -> {
                try {
                    enrollmentService.cancel(enrollmentId);
                    cancelSuccess.incrementAndGet();
                } catch (Exception e) {
                    // ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 0; i < 20; i++) {
            final Long studentId = newStudents.get(i).getId();
            final Long courseId = course.getId();
            executor.submit(() -> {
                try {
                    enrollmentService.enroll(studentId, courseId);
                    enrollSuccess.incrementAndGet();
                } catch (Exception e) {
                    // ignore
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then: enrolled = 기존10 - 취소성공 + 신규성공
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        int expectedEnrolled = 10 - cancelSuccess.get() + enrollSuccess.get();
        long actualEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(course.getId()))
                .count();

        assertThat(updatedCourse.getEnrolled()).isEqualTo(expectedEnrolled);
        assertThat(actualEnrollments).isEqualTo(expectedEnrolled);
    }
}
