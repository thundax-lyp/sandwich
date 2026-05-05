-- Execute after db/data/system.sql so user_id and identity_id references are stable.
-- Default OAuth2 admin client:
--   client_id: sandwich-admin-web
--   client_secret hash is a seed placeholder and must be replaced for real environments.

INSERT INTO `auth_oauth_client` (
    `id`, `client_id`, `client_secret_hash`, `client_name`, `client_type`,
    `grant_types`, `scopes`, `redirect_uris`,
    `access_token_ttl_seconds`, `refresh_token_ttl_seconds`,
    `status`, `contact`, `remark`,
    `create_date`, `create_by`, `update_date`, `update_by`
) VALUES (
    'oauth-client-admin-web', 'sandwich-admin-web', 'CHANGE_ME_CLIENT_SECRET_HASH', 'Sandwich Admin Web', 'CONFIDENTIAL',
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
