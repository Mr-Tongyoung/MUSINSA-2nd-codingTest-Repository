import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Smoke test: lightweight config verification
export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  // Health check
  const health = http.get(`${BASE_URL}/health`);
  check(health, {
    'health: status 200': (r) => r.status === 200,
  });

  // Course list
  const courses = http.get(`${BASE_URL}/courses`);
  check(courses, {
    'courses: status 200': (r) => r.status === 200,
  });

  // Course list by department
  const deptCourses = http.get(`${BASE_URL}/courses?departmentId=1`);
  check(deptCourses, {
    'courses by dept: status 200': (r) => r.status === 200,
  });

  // Student timetable
  const timetable = http.get(`${BASE_URL}/students/1/timetable`);
  check(timetable, {
    'timetable: status 200': (r) => r.status === 200,
  });

  // Enrollment (expect 201 or 409)
  const payload = JSON.stringify({
    studentId: Math.floor(Math.random() * 100) + 1,
    courseId: Math.floor(Math.random() * 50) + 1,
  });
  const enroll = http.post(`${BASE_URL}/enrollments`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(enroll, {
    'enroll: status 201 or 409': (r) => r.status === 201 || r.status === 409,
  });

  sleep(1);
}
