SET NAMES utf8mb4;

-- Integration OpenAPI client.
-- API key: it-open-client
-- API secret: it-open-secret

INSERT INTO `open_client` (
    `id`, `name`, `status`, `ip_whitelist`, `expired_at`, `remarks`
) VALUES
    (
        9100000000000050001, 'Integration Open Client', 'ENABLED',
        '["127.0.0.1","0:0:0:0:0:0:0:1"]', NULL,
        'Integration OpenAPI client for signature and submission tests'
    ),
    (
        9100000000000050002, 'Integration Disabled Open Client', 'DISABLED',
        '["127.0.0.1"]', NULL,
        'Integration OpenAPI disabled client for failure tests'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `status` = VALUES(`status`),
    `ip_whitelist` = VALUES(`ip_whitelist`),
    `expired_at` = VALUES(`expired_at`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (
        9100000000000050101, 'OPEN_CLIENT', 9100000000000050001,
        'API_KEY', 'it-open-client', 'ENABLED'
    ),
    (
        9100000000000050102, 'OPEN_CLIENT', 9100000000000050002,
        'API_KEY', 'it-open-disabled-client', 'ENABLED'
    )
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_type` = VALUES(`identity_type`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `auth_principal_credential` (
    `id`, `principal_type`, `principal_id`, `identity_id`, `credential_type`, `credential_value`, `status`,
    `need_change_password`, `failed_count`, `failed_limit`,
    `locked_until`, `expires_at`, `last_verified_at`
) VALUES
    (
        9100000000000050201, 'OPEN_CLIENT', 9100000000000050001, 9100000000000050101,
        'API_SECRET', 'it-open-secret', 'ACTIVE', 0, 0, 0, NULL, NULL, NULL
    ),
    (
        9100000000000050202, 'OPEN_CLIENT', 9100000000000050002, 9100000000000050102,
        'API_SECRET', 'it-open-disabled-secret', 'ACTIVE', 0, 0, 0, NULL, NULL, NULL
    )
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_type` = VALUES(`credential_type`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`),
    `locked_until` = VALUES(`locked_until`),
    `expires_at` = VALUES(`expires_at`),
    `last_verified_at` = VALUES(`last_verified_at`);

INSERT INTO `open_client_permission` (
    `id`, `client_id`, `permission`
) VALUES
    (9100000000000050301, 9100000000000050001, 'submission:submission:create'),
    (9100000000000050302, 9100000000000050001, 'submission:submission:page'),
    (9100000000000050303, 9100000000000050001, 'submission:submission:change-status'),
    (9100000000000050304, 9100000000000050001, 'submission:submission:image:upload'),
    (9100000000000050305, 9100000000000050002, 'submission:submission:page')
ON DUPLICATE KEY UPDATE
    `client_id` = VALUES(`client_id`),
    `permission` = VALUES(`permission`);
