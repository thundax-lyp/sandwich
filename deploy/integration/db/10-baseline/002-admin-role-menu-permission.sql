SET NAMES utf8mb4;

-- Integration admin-api roles, menus and permissions.
-- it-admin owns edit permissions; it-user owns read permissions only.

INSERT INTO `sys_role` (
    `id`, `name`, `privilege`, `status`, `priority`, `remarks`
) VALUES
    (
        9100000000000000401, 'Integration Administrator', 'ADMIN', 'ENABLED', 9101,
        'Integration test role with admin-api edit permissions'
    ),
    (
        9100000000000000402, 'Integration Readonly User', 'NORMAL', 'ENABLED', 9102,
        'Integration test role with admin-api read permissions'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `sys_menu` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `perms`, `ranks`,
    `visibility`, `display_params`, `url`, `target`, `remarks`
) VALUES
    (
        9100000000000001001, NULL, 1, 14, 'Integration System', NULL, 9100,
        'VISIBLE', '{"icon":"system"}', '/integration/system', NULL, 'Integration system root menu'
    ),
    (
        9100000000000001002, 9100000000000001001, 2, 7, 'Integration Users', NULL, 9101,
        'VISIBLE', '{"icon":"users"}', '/integration/system/users', NULL, 'Integration user menu'
    ),
    (
        9100000000000001003, 9100000000000001002, 3, 4, 'Integration User View', 'sys:user:view', 9102,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration user view permission'
    ),
    (
        9100000000000001004, 9100000000000001002, 5, 6, 'Integration User Edit', 'sys:user:edit', 9103,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration user edit permission'
    ),
    (
        9100000000000001005, 9100000000000001001, 8, 13, 'Integration Roles', NULL, 9104,
        'VISIBLE', '{"icon":"roles"}', '/integration/system/roles', NULL, 'Integration role menu'
    ),
    (
        9100000000000001006, 9100000000000001005, 9, 10, 'Integration Role View', 'sys:role:view', 9105,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration role view permission'
    ),
    (
        9100000000000001007, 9100000000000001005, 11, 12, 'Integration Role Edit', 'sys:role:edit', 9106,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration role edit permission'
    )
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `lft` = VALUES(`lft`),
    `rgt` = VALUES(`rgt`),
    `name` = VALUES(`name`),
    `perms` = VALUES(`perms`),
    `ranks` = VALUES(`ranks`),
    `visibility` = VALUES(`visibility`),
    `display_params` = VALUES(`display_params`),
    `url` = VALUES(`url`),
    `target` = VALUES(`target`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    (9100000000000000101, 9100000000000000401),
    (9100000000000000102, 9100000000000000402)
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
    (9100000000000000401, 9100000000000001001),
    (9100000000000000401, 9100000000000001002),
    (9100000000000000401, 9100000000000001003),
    (9100000000000000401, 9100000000000001004),
    (9100000000000000401, 9100000000000001005),
    (9100000000000000401, 9100000000000001006),
    (9100000000000000401, 9100000000000001007),
    (9100000000000000402, 9100000000000001001),
    (9100000000000000402, 9100000000000001002),
    (9100000000000000402, 9100000000000001003),
    (9100000000000000402, 9100000000000001005),
    (9100000000000000402, 9100000000000001006)
ON DUPLICATE KEY UPDATE
    `menu_id` = VALUES(`menu_id`);
