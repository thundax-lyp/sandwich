-- Default front-api member account:
--   account: member
--   password hash is initialized from the deployment default password and must be rotated.

INSERT INTO `member_member` (
    `id`, `name`, `gender`, `status`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES (
    1000000000000020001, '会员用户', 'PRIVATE', 'ACTIVE', 0, '系统初始化会员用户',
    '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `gender` = VALUES(`gender`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`),
    `del_flag` = VALUES(`del_flag`);

INSERT INTO `member_identity` (
    `id`, `member_id`, `identity_type`, `identity_value`, `status`
) VALUES (
    1000000000000020101, 1000000000000020001, 'ACCOUNT', 'member', 'ENABLED'
) ON DUPLICATE KEY UPDATE
    `member_id` = VALUES(`member_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `member_credential` (
    `id`, `member_id`, `identity_id`, `credential_type`, `credential_value`, `status`,
    `need_change_password`, `failed_count`, `failed_limit`,
    `locked_until`, `expires_at`, `last_verified_at`
) VALUES (
    1000000000000020201, 1000000000000020001, 1000000000000020101, 'PASSWORD',
    'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
    1, 0, 5,
    NULL, NULL, NULL
) ON DUPLICATE KEY UPDATE
    `member_id` = VALUES(`member_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`),
    `locked_until` = VALUES(`locked_until`),
    `expires_at` = VALUES(`expires_at`),
    `last_verified_at` = VALUES(`last_verified_at`);
