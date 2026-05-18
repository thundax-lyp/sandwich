SET NAMES utf8mb4;

-- Integration front-api members.
-- Login account: it-member / Q1w2e3r$

INSERT INTO `member_member` (
    `id`, `name`, `gender`, `status`, `priority`, `remarks`
) VALUES
    (
        9100000000000060001, 'Integration Member', 'PRIVATE', 'ACTIVE', 9101,
        'Integration test active member'
    ),
    (
        9100000000000060002, 'Integration Suspended Member', 'PRIVATE', 'SUSPENDED', 9102,
        'Integration test suspended member'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `gender` = VALUES(`gender`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (
        9100000000000060101, 'MEMBER', 9100000000000060001,
        'MEMBER_ACCOUNT', 'it-member', 'ENABLED'
    ),
    (
        9100000000000060102, 'MEMBER', 9100000000000060001,
        'MEMBER_MOBILE', '15500006666', 'ENABLED'
    ),
    (
        9100000000000060103, 'MEMBER', 9100000000000060001,
        'MEMBER_EMAIL', 'it-member@sandwish.local', 'ENABLED'
    ),
    (
        9100000000000060104, 'MEMBER', 9100000000000060002,
        'MEMBER_ACCOUNT', 'it-member-suspended', 'ENABLED'
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
        9100000000000060201, 'MEMBER', 9100000000000060001, 9100000000000060101,
        'MEMBER_PASSWORD', 'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)',
        'ACTIVE', 0, 0, 5, NULL, NULL, NULL
    ),
    (
        9100000000000060202, 'MEMBER', 9100000000000060002, 9100000000000060104,
        'MEMBER_PASSWORD', 'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)',
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
