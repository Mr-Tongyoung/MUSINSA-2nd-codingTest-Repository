import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('custom_error_rate');
const enrollLatency = new Trend('enroll_latency', true);
const courseListLatency = new Trend('course_list_latency', true);
const timetableLatency = new Trend('timetable_latency', true);

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Spike pattern: warmup → ramp-up → peak → ramp-down
export const options = {
  stages: [
    { duration: '10s', target: 20 },   // warmup
    { duration: '5s', target: 200 },    // spike ramp-up
    { duration: '30s', target: 200 },   // peak sustained
    { duration: '10s', target: 50 },    // ramp-down
    { duration: '5s', target: 0 },      // cool-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],            // p95 < 3s
    custom_error_rate: ['rate<0.1'],              // 5xx error rate < 10%
    'http_req_duration{type:enroll}': ['p(99)<5000'],
  },
};

// Pre-computed ranges for random selection
const MAX_STUDENTS = 10000;
const MAX_COURSES = 500;
const MAX_DEPARTMENTS = 10;

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// Scenario: 수강신청 (POST /enrollments) — 50% traffic
function enrollCourse() {
  const payload = JSON.stringify({
    studentId: randomInt(1, MAX_STUDENTS),
    courseId: randomInt(1, MAX_COURSES),
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { type: 'enroll' },
  };

  const res = http.post(`${BASE_URL}/enrollments`, payload, params);
  enrollLatency.add(res.timings.duration);

  // 409 (business rule violation) is expected, not an error
  const isOk = res.status === 201 || res.status === 409;
  check(res, {
    'enroll: status is 201 or 409': () => isOk,
  });
  errorRate.add(res.status >= 500);
}

// Scenario: 강좌 조회 (GET /courses) — 30% traffic
function listCourses() {
  const params = { tags: { type: 'courses' } };
  let url = `${BASE_URL}/courses`;

  // 50% chance to filter by department
  if (Math.random() > 0.5) {
    url += `?departmentId=${randomInt(1, MAX_DEPARTMENTS)}`;
  }

  const res = http.get(url, params);
  courseListLatency.add(res.timings.duration);

  check(res, {
    'courses: status is 200': (r) => r.status === 200,
  });
  errorRate.add(res.status >= 500);
}

// Scenario: 시간표 조회 (GET /students/{id}/timetable) — 20% traffic
function getTimetable() {
  const studentId = randomInt(1, MAX_STUDENTS);
  const params = { tags: { type: 'timetable' } };

  const res = http.get(`${BASE_URL}/students/${studentId}/timetable`, params);
  timetableLatency.add(res.timings.duration);

  check(res, {
    'timetable: status is 200': (r) => r.status === 200,
  });
  errorRate.add(res.status >= 500);
}

export default function () {
  const rand = Math.random();

  if (rand < 0.5) {
    enrollCourse();        // 50%
  } else if (rand < 0.8) {
    listCourses();         // 30%
  } else {
    getTimetable();        // 20%
  }

  sleep(0.1); // 100ms think time
}
