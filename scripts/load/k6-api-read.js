import crypto from "k6/crypto";
import http from "k6/http";
import { check, sleep } from "k6";

const publicBaseUrl = (__ENV.SANDWICH_PUBLIC_BASE_URL || "http://127.0.0.1:18080").replace(/\/$/, "");
const adminBaseUrl = (__ENV.SANDWICH_ADMIN_BASE_URL || `${publicBaseUrl}/admin-api`).replace(/\/$/, "");
const frontBaseUrl = (__ENV.SANDWICH_FRONT_BASE_URL || `${publicBaseUrl}/front-api`).replace(/\/$/, "");
const openBaseUrl = (__ENV.SANDWICH_OPEN_BASE_URL || `${publicBaseUrl}/open-api`).replace(/\/$/, "");
const openContextPath = __ENV.SANDWICH_OPEN_CONTEXT_PATH || "/open-api";
const adminToken = __ENV.SANDWICH_SMOKE_ADMIN_TOKEN || __ENV.SANDWICH_SMOKE_ACCESS_TOKEN || "";
const tokenHeader = __ENV.SANDWICH_SMOKE_TOKEN_HEADER || "Access-Token";
const openApiKey = __ENV.SANDWICH_SMOKE_OPEN_API_KEY || "";
const openApiSecret = __ENV.SANDWICH_SMOKE_OPEN_API_SECRET || "";
const thinkTimeSeconds = Number(__ENV.SANDWICH_LOAD_THINK_TIME_SECONDS || "1");

const parseStages = (value) => {
    const raw = value || "30s:5,2m:20,30s:0";
    return raw.split(",").map((item) => {
        const [duration, target] = item.split(":");
        return {
            duration: duration.trim(),
            target: Number(target.trim())
        };
    });
};

export const options = {
    scenarios: {
        api_read: {
            executor: "ramping-vus",
            stages: parseStages(__ENV.SANDWICH_LOAD_STAGES),
            gracefulRampDown: "30s"
        }
    },
    thresholds: {
        http_req_failed: [__ENV.SANDWICH_LOAD_FAILED_THRESHOLD || "rate<0.01"],
        http_req_duration: [
            __ENV.SANDWICH_LOAD_P95_THRESHOLD || "p(95)<1000",
            __ENV.SANDWICH_LOAD_P99_THRESHOLD || "p(99)<2000"
        ]
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"]
};

const jsonHeaders = (extra = {}) => ({
    Accept: "application/json",
    "Content-Type": "application/json",
    ...extra
});

const endpointTag = (name) => ({ tags: { endpoint: name } });

const postJson = (name, baseUrl, path, body = "", headers = {}) => {
    return http.post(`${baseUrl}${path}`, body, {
        headers: jsonHeaders(headers),
        ...endpointTag(name)
    });
};

const expectStatus = (name, response, statuses) => {
    check(response, {
        [`${name} status in ${statuses.join("/")}`]: (res) => statuses.includes(res.status),
        [`${name} has body`]: (res) => res.body !== null && res.body !== undefined && res.body.length > 0
    });
};

const openApiHeaders = (method, path, body) => {
    const timestamp = String(Date.now());
    const nonce = `k6-${__VU}-${__ITER}-${timestamp}`;
    const contentSha256 = crypto.sha256(body, "hex");
    const canonicalRequest = [method, `${openContextPath}${path}`, "", timestamp, nonce, contentSha256].join("\n");
    const signature = crypto.hmac("sha256", openApiSecret, canonicalRequest, "hex");
    return {
        "X-Sandwish-Api-Key": openApiKey,
        "X-Sandwish-Timestamp": timestamp,
        "X-Sandwish-Nonce": nonce,
        "X-Sandwish-Content-SHA256": contentSha256,
        "X-Sandwish-Signature": signature
    };
};

export default function () {
    expectStatus(
        "admin auth pre-auth-session",
        postJson("admin_auth_pre_auth_session", adminBaseUrl, "/api/auth/session/pre-auth-session"),
        [200]
    );

    expectStatus(
        "front auth pre-auth-session",
        postJson("front_auth_pre_auth_session", frontBaseUrl, "/api/auth/session/pre-auth-session"),
        [200]
    );

    expectStatus(
        "front auth check-login",
        postJson("front_auth_check_login", frontBaseUrl, "/api/auth/session/check-login"),
        [200]
    );

    expectStatus(
        "open unsigned submission page",
        postJson("open_unsigned_submission_page", openBaseUrl, "/api/submission/submission/page", '{"pageNo":1,"pageSize":1}'),
        [401]
    );

    if (adminToken) {
        const authHeader = { [tokenHeader]: adminToken };
        expectStatus(
            "admin current user info",
            postJson("admin_current_user_info", adminBaseUrl, "/api/sys/current-user/info", "", authHeader),
            [200]
        );
        expectStatus(
            "admin current user menus",
            postJson("admin_current_user_menus", adminBaseUrl, "/api/sys/current-user/menus", "", authHeader),
            [200]
        );
        expectStatus(
            "admin current user permissions",
            postJson("admin_current_user_permissions", adminBaseUrl, "/api/sys/current-user/perms", "", authHeader),
            [200]
        );
        expectStatus(
            "admin dictionary page",
            postJson("admin_dictionary_page", adminBaseUrl, "/api/sys/dict/page", '{"pageNo":1,"pageSize":10}', authHeader),
            [200]
        );
        expectStatus(
            "admin storage object tree",
            postJson("admin_storage_object_tree", adminBaseUrl, "/api/storage/object/tree", "", authHeader),
            [200]
        );
    }

    if (openApiKey && openApiSecret) {
        const body = '{"pageNo":1,"pageSize":10}';
        expectStatus(
            "open signed submission page",
            postJson(
                "open_signed_submission_page",
                openBaseUrl,
                "/api/submission/submission/page",
                body,
                openApiHeaders("POST", "/api/submission/submission/page", body)
            ),
            [200]
        );
    }

    sleep(thinkTimeSeconds);
}

const metricValue = (data, metric, field) => {
    const target = data.metrics[metric];
    if (!target || !target.values) {
        return "";
    }
    const value = target.values[field];
    return value === undefined ? "" : String(value);
};

const renderMarkdownSummary = (data) => {
    return `# Sandwich API Load Test Report

## Test Metadata

- target: ${publicBaseUrl}
- adminBaseUrl: ${adminBaseUrl}
- frontBaseUrl: ${frontBaseUrl}
- openBaseUrl: ${openBaseUrl}
- stages: ${JSON.stringify(options.scenarios.api_read.stages)}
- generatedAt: ${new Date().toISOString()}

## Result Summary

| Metric | Value |
| --- | --- |
| http_reqs.count | ${metricValue(data, "http_reqs", "count")} |
| http_req_failed.rate | ${metricValue(data, "http_req_failed", "rate")} |
| http_req_duration.avg | ${metricValue(data, "http_req_duration", "avg")} |
| http_req_duration.p95 | ${metricValue(data, "http_req_duration", "p(95)")} |
| http_req_duration.p99 | ${metricValue(data, "http_req_duration", "p(99)")} |
| iterations.count | ${metricValue(data, "iterations", "count")} |

## Required Analysis

- 说明测试环境规格、Docker Compose 配置、API 镜像版本和数据库数据量。
- 记录测试期间 API、MySQL、Redis、MinIO、RocketMQ、nginx 的 CPU、内存、连接数和错误日志。
- 标出错误率、P95、P99 是否超过阈值。
- 标出最先出现瓶颈的组件和证据。
`;
};

export function handleSummary(data) {
    const jsonPath = __ENV.SANDWICH_LOAD_SUMMARY_JSON || "reports/load/k6-summary.json";
    const markdownPath = __ENV.SANDWICH_LOAD_REPORT_MD || "reports/load/k6-report.md";
    return {
        stdout: `load summary written to ${jsonPath} and ${markdownPath}\n`,
        [jsonPath]: JSON.stringify(data, null, 2),
        [markdownPath]: renderMarkdownSummary(data)
    };
}
