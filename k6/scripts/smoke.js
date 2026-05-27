import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://shortener:8080';

function vuIp() {
  return `10.0.${Math.floor(__VU / 256)}.${__VU % 256}`;
}

function headers() {
  return { 'Content-Type': 'application/json', 'X-Forwarded-For': vuIp() };
}

export default function () {
  const shortenRes = http.post(
    `${BASE_URL}/api/v1/data/shorten`,
    JSON.stringify({ originalUrl: 'https://example.com/smoke-test' }),
    { headers: headers() }
  );

  check(shortenRes, {
    'shorten status 200': (r) => r.status === 200,
    'shortCode present': (r) => JSON.parse(r.body).shortCode !== undefined,
  });

  const shortCode = JSON.parse(shortenRes.body).shortCode;

  const redirectRes = http.get(`${BASE_URL}/api/v1/${shortCode}`, {
    headers: headers(),
    redirects: 0,
  });

  check(redirectRes, {
    'redirect status 302': (r) => r.status === 302,
  });

  sleep(1);
}
