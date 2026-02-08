package com.example.courseRegistrationSystem.config;

import com.example.courseRegistrationSystem.entity.*;
import com.example.courseRegistrationSystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final ProfessorRepository professorRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    private final Random random = new Random(42);

    private static final String[] DEPARTMENT_NAMES = {
        "컴퓨터공학과", "전자공학과", "기계공학과", "화학공학과", "건축공학과",
        "경영학과", "경제학과", "심리학과", "국어국문학과", "영어영문학과",
        "수학과", "물리학과", "생명과학과", "법학과", "행정학과"
    };

    private static final String[] LAST_NAMES = {
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
        "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍"
    };

    private static final String[] FIRST_NAMES = {
        "민준", "서준", "예준", "도윤", "시우", "주원", "하준", "지호", "지후", "준서",
        "서연", "서윤", "지우", "서현", "민서", "하은", "하윤", "윤서", "지민", "채원",
        "수빈", "지원", "은서", "다은", "현우", "준혁", "승현", "태민", "건우", "성민"
    };

    private static final String[][] COURSE_NAMES_BY_DEPT = {
        // 컴퓨터공학과
        {"자료구조", "알고리즘", "운영체제", "컴퓨터네트워크", "데이터베이스", "소프트웨어공학", "인공지능", "컴퓨터구조", "프로그래밍언어론", "웹프로그래밍", "모바일프로그래밍", "정보보안", "클라우드컴퓨팅", "빅데이터분석", "기계학습"},
        // 전자공학과
        {"회로이론", "전자회로", "디지털논리회로", "신호및시스템", "통신공학", "반도체공학", "전자기학", "마이크로프로세서", "임베디드시스템", "제어공학"},
        // 기계공학과
        {"열역학", "유체역학", "고체역학", "동역학", "재료역학", "기계설계", "CAD/CAM", "자동차공학", "로봇공학", "생산공학"},
        // 화학공학과
        {"화공열역학", "반응공학", "이동현상", "공정제어", "화공양론", "고분자공학", "촉매공학", "분리공정", "환경공학개론", "나노소재공학"},
        // 건축공학과
        {"건축구조역학", "건축시공학", "건축환경공학", "건축재료학", "철근콘크리트", "건축설비", "건축법규", "도시계획론", "건축CAD", "건설관리학"},
        // 경영학과
        {"경영학원론", "마케팅원론", "재무관리", "인적자원관리", "경영전략", "회계원리", "생산운영관리", "국제경영", "소비자행동론", "경영정보시스템"},
        // 경제학과
        {"미시경제학", "거시경제학", "경제수학", "통계학", "계량경제학", "국제경제학", "화폐금융론", "산업조직론", "게임이론", "재정학"},
        // 심리학과
        {"심리학개론", "발달심리학", "사회심리학", "인지심리학", "이상심리학", "상담심리학", "심리통계", "실험심리학", "성격심리학", "산업심리학"},
        // 국어국문학과
        {"국어학개론", "고전문학사", "현대문학사", "국어음운론", "국어문법론", "한국문학비평", "고전소설강독", "현대소설강독", "시론", "국어의미론"},
        // 영어영문학과
        {"영어학개론", "영문학개론", "영어음성학", "영어통사론", "영미소설", "영미시", "영어회화", "번역연습", "영어작문", "영미문화"},
        // 수학과
        {"미적분학", "선형대수학", "해석학", "대수학", "확률론", "위상수학", "미분방정식", "수치해석", "이산수학", "복소해석학"},
        // 물리학과
        {"일반물리학", "역학", "전자기학", "양자역학", "열통계물리학", "광학", "현대물리학", "물리수학", "고체물리학", "핵물리학"},
        // 생명과학과
        {"일반생물학", "세포생물학", "유전학", "분자생물학", "생태학", "미생물학", "생화학", "면역학", "발생생물학", "신경과학"},
        // 법학과
        {"헌법", "민법총칙", "형법총론", "행정법", "상법", "민사소송법", "형사소송법", "국제법", "노동법", "환경법"},
        // 행정학과
        {"행정학원론", "정책학개론", "조직론", "인사행정론", "재무행정론", "지방자치론", "전자정부론", "행정법총론", "비교행정론", "도시행정론"}
    };

    private static final String[] DAYS = {"월", "화", "수", "목", "금"};

    private static final String[][] TIME_SLOTS = {
        {"09:00", "10:30"}, {"10:30", "12:00"}, {"13:00", "14:30"},
        {"14:30", "16:00"}, {"16:00", "17:30"}
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long start = System.currentTimeMillis();
        log.info("초기 데이터 생성 시작...");

        List<Department> departments = createDepartments();
        List<Professor> professors = createProfessors(departments);
        List<Course> courses = createCourses(departments, professors);
        createStudents(departments);

        long elapsed = System.currentTimeMillis() - start;
        log.info("초기 데이터 생성 완료 - 학과: {}, 교수: {}, 강좌: {}, 소요시간: {}ms",
                departments.size(), professors.size(), courses.size(), elapsed);
    }

    private List<Department> createDepartments() {
        List<Department> departments = new ArrayList<>();
        for (String name : DEPARTMENT_NAMES) {
            departments.add(Department.builder().name(name).build());
        }
        return departmentRepository.saveAll(departments);
    }

    private List<Professor> createProfessors(List<Department> departments) {
        List<Professor> professors = new ArrayList<>();
        for (Department dept : departments) {
            int count = 7 + random.nextInt(4); // 7~10명
            for (int i = 0; i < count; i++) {
                professors.add(Professor.builder()
                        .name(generateName())
                        .department(dept)
                        .build());
            }
        }
        return professorRepository.saveAll(professors);
    }

    private List<Course> createCourses(List<Department> departments, List<Professor> professors) {
        List<Course> courses = new ArrayList<>();

        Map<Long, List<Professor>> profByDept = new HashMap<>();
        for (Professor prof : professors) {
            profByDept.computeIfAbsent(prof.getDepartment().getId(), k -> new ArrayList<>()).add(prof);
        }

        for (int deptIdx = 0; deptIdx < departments.size(); deptIdx++) {
            Department dept = departments.get(deptIdx);
            List<Professor> deptProfs = profByDept.get(dept.getId());
            String[] courseNames = COURSE_NAMES_BY_DEPT[deptIdx];

            // 각 강좌를 여러 분반으로 생성하여 500개 이상 확보
            for (String courseName : courseNames) {
                int sections = 3 + random.nextInt(2); // 3~4 분반
                for (int sec = 1; sec <= sections; sec++) {
                    Professor prof = deptProfs.get(random.nextInt(deptProfs.size()));
                    int credits = generateCredits();
                    String schedule = generateSchedule();
                    int capacity = 30 + random.nextInt(21); // 30~50명

                    String fullName = sections > 1 ? courseName + " " + sec + "분반" : courseName;

                    courses.add(Course.builder()
                            .name(fullName)
                            .credits(credits)
                            .capacity(capacity)
                            .schedule(schedule)
                            .professor(prof)
                            .department(dept)
                            .build());
                }
            }
        }

        return courseRepository.saveAll(courses);
    }

    private void createStudents(List<Department> departments) {
        List<Student> batch = new ArrayList<>();
        int studentCount = 0;

        for (int i = 0; i < 10000; i++) {
            Department dept = departments.get(random.nextInt(departments.size()));
            int grade = 1 + random.nextInt(4); // 1~4학년
            int yearPrefix = 2026 - grade;
            String studentNumber = String.format("%d%05d", yearPrefix, i + 1);

            batch.add(Student.builder()
                    .name(generateName())
                    .studentNumber(studentNumber)
                    .grade(grade)
                    .department(dept)
                    .build());

            if (batch.size() >= 500) {
                studentRepository.saveAll(batch);
                studentCount += batch.size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            studentRepository.saveAll(batch);
            studentCount += batch.size();
        }

        log.info("학생 {}명 생성 완료", studentCount);
    }

    private String generateName() {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)]
             + FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    private int generateCredits() {
        int r = random.nextInt(10);
        if (r < 1) return 1;       // 10%
        if (r < 3) return 2;       // 20%
        if (r < 8) return 3;       // 50%
        return 4;                   // 20%
    }

    private String generateSchedule() {
        String day = DAYS[random.nextInt(DAYS.length)];
        String[] slot = TIME_SLOTS[random.nextInt(TIME_SLOTS.length)];
        return day + " " + slot[0] + "-" + slot[1];
    }
}
