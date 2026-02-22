import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('custom_error_rate');
const enrollLatency = new Trend('enroll_latency', true);
const courseListLatency = new Trend('course_list_latency', true);
const timetableLatency = new Trend('timetable_latency', true);

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '10s', target: 500 },    // warmup
    { duration: '10s', target: 10000 },   // spike ramp-up
    { duration: '30s', target: 10000 },   // peak sustained
    { duration: '10s', target: 500 },     // ramp-down
    { duration: '5s', target: 0 },        // cool-down
  ],
  thresholds: {
    custom_error_rate: ['rate<0.3'],
  },
};

const MAX_STUDENTS = 10000;
const MAX_COURSES = 500;
const MAX_DEPARTMENTS = 10;

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

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
  check(res, { 'enroll: not 5xx': (r) => r.status < 500 });
  errorRate.add(res.status >= 500);
}

function listCourses() {
  const params = { tags: { type: 'courses' } };
  let url = `${BASE_URL}/courses`;
  if (Math.random() > 0.5) {
    url += `?departmentId=${randomInt(1, MAX_DEPARTMENTS)}`;
  }
  const res = http.get(url, params);
  courseListLatency.add(res.timings.duration);
  check(res, { 'courses: not 5xx': (r) => r.status < 500 });
  errorRate.add(res.status >= 500);
}

function getTimetable() {
  const studentId = randomInt(1, MAX_STUDENTS);
  const params = { tags: { type: 'timetable' } };
  const res = http.get(`${BASE_URL}/students/${studentId}/timetable`, params);
  timetableLatency.add(res.timings.duration);
  check(res, { 'timetable: not 5xx': (r) => r.status < 500 });
  errorRate.add(res.status >= 500);
}

export default function () {
  const rand = Math.random();
  if (rand < 0.5) {
    enrollCourse();
  } else if (rand < 0.8) {
    listCourses();
  } else {
    getTimetable();
  }
  sleep(0.1);
}
