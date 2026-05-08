-- Execute after db/data/system.sql so user_id and identity_id references are stable.
-- Default principal accounts:
--   admin-api: developer / Q1w2e3r$
--   front-api: member / Q1w2e3r$
-- Default OAuth2 admin client:
--   client_id: sandwich-admin-web
--   client_secret hash is a seed placeholder and must be replaced for real environments.

INSERT INTO `auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (
        1000000000000010101, 'USER', 1000000000000000101, 'USER_ACCOUNT', 'developer', 'ENABLED'
    ),
    (
        1000000000000010102, 'MEMBER', 1000000000000020001, 'MEMBER_ACCOUNT', 'member', 'ENABLED'
    )
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `auth_principal_credential` (
    `id`, `principal_type`, `principal_id`, `identity_id`, `credential_type`, `credential_value`, `status`,
    `need_change_password`, `failed_count`, `failed_limit`,
    `locked_until`, `expires_at`, `last_verified_at`
) VALUES
    (
        1000000000000010201, 'USER', 1000000000000000101, 1000000000000010101, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010202, 'MEMBER', 1000000000000020001, 1000000000000010102, 'MEMBER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    )
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`),
    `locked_until` = VALUES(`locked_until`),
    `expires_at` = VALUES(`expires_at`),
    `last_verified_at` = VALUES(`last_verified_at`);

INSERT INTO `auth_oauth_client` (
    `id`, `client_id`, `client_secret_hash`, `client_name`, `client_type`,
    `grant_types`, `scopes`, `redirect_uris`,
    `access_token_ttl_seconds`, `refresh_token_ttl_seconds`,
    `status`, `contact`, `remark`,
    `create_date`, `create_by`, `update_date`, `update_by`
) VALUES (
    1000000000000010001, 'sandwich-admin-web', 'CHANGE_ME_CLIENT_SECRET_HASH', 'Sandwich Admin Web', 'CONFIDENTIAL',
    '["authorization_code","refresh_token"]',
    '["openid","profile","user.read"]',
    '["http://127.0.0.1:5173/login/oauth2/code/sandwich"]',
    7200, 2592000,
    'ENABLED', 'developer@sandwich.local', '系统初始化 OAuth2 client',
    '2026-05-05 00:00:00.000', 'user-developer', NULL, NULL
) ON DUPLICATE KEY UPDATE
    `client_secret_hash` = VALUES(`client_secret_hash`),
    `client_name` = VALUES(`client_name`),
    `client_type` = VALUES(`client_type`),
    `grant_types` = VALUES(`grant_types`),
    `scopes` = VALUES(`scopes`),
    `redirect_uris` = VALUES(`redirect_uris`),
    `access_token_ttl_seconds` = VALUES(`access_token_ttl_seconds`),
    `refresh_token_ttl_seconds` = VALUES(`refresh_token_ttl_seconds`),
    `status` = VALUES(`status`),
    `contact` = VALUES(`contact`),
    `remark` = VALUES(`remark`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`);
