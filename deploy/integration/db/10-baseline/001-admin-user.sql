SET NAMES utf8mb4;

-- Integration admin-api accounts.
-- Login accounts:
--   it-admin / Q1w2e3r$
--   it-user  / Q1w2e3r$

INSERT INTO `sys_user` (
    `id`, `department_id`, `email`, `mobile`, `tel`, `name`, `ranks`,
    `privilege`, `status`, `remarks`
) VALUES
    (
        9100000000000000101, NULL, 'it-admin@sandwish.local', NULL, 'IT-1001',
        'Integration Admin', 100, 'ADMIN', 'ENABLED', 'Integration test administrator'
    ),
    (
        9100000000000000102, NULL, 'it-user@sandwish.local', NULL, 'IT-1002',
        'Integration User', 90, 'NORMAL', 'ENABLED', 'Integration test normal user'
    )
ON DUPLICATE KEY UPDATE
    `department_id` = VALUES(`department_id`),
    `email` = VALUES(`email`),
    `mobile` = VALUES(`mobile`),
    `tel` = VALUES(`tel`),
    `name` = VALUES(`name`),
    `ranks` = VALUES(`ranks`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (
        9100000000000010101, 'USER', 9100000000000000101,
        'USER_ACCOUNT', 'it-admin', 'ENABLED'
    ),
    (
        9100000000000010102, 'USER', 9100000000000000102,
        'USER_ACCOUNT', 'it-user', 'ENABLED'
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
        9100000000000010201, 'USER', 9100000000000000101, 9100000000000010101,
        'USER_PASSWORD', 'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)',
        'ACTIVE', 0, 0, 5, NULL, NULL, NULL
    ),
    (
        9100000000000010202, 'USER', 9100000000000000102, 9100000000000010102,
        'USER_PASSWORD', 'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)',
        'ACTIVE', 0, 0, 5, NULL, NULL, NULL
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
