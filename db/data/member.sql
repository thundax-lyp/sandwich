-- Default front-api member account:
--   account: member
--   password hash is initialized from the deployment default password and must be rotated.

INSERT INTO `member_member` (
    `id`, `name`, `gender`, `status`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`
) VALUES (
    1000000000000020001, '会员用户', 'PRIVATE', 'ACTIVE', 0, '系统初始化会员用户',
    '2026-05-05 00:00:00.000', 'system', NULL, NULL
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `gender` = VALUES(`gender`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`);
