-- Default front-api member account:
--   account: member
--   password hash is initialized from the deployment default password and must be rotated.

INSERT INTO `member_member` (
    `id`, `name`, `gender`, `status`, `priority`, `remarks`
) VALUES (
    1000000000000020001, '会员用户', 'PRIVATE', 'ACTIVE', 0, '系统初始化会员用户'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `gender` = VALUES(`gender`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);
