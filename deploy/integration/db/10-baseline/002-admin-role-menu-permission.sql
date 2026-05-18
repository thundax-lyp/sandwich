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
        9100000000000001001, NULL, 1, 28, 'Integration System', NULL, 0,
        'VISIBLE', '{"icon":"system"}', '/integration/system', NULL, 'Integration system root menu'
    ),
    (
        9100000000000001002, 9100000000000001001, 2, 7, 'Integration Users', NULL, 0,
        'VISIBLE', '{"icon":"users"}', '/integration/system/users', NULL, 'Integration user menu'
    ),
    (
        9100000000000001003, 9100000000000001002, 3, 4, 'Integration User View', 'sys:user:view', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration user view permission'
    ),
    (
        9100000000000001004, 9100000000000001002, 5, 6, 'Integration User Edit', 'sys:user:edit', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration user edit permission'
    ),
    (
        9100000000000001005, 9100000000000001001, 8, 13, 'Integration Roles', NULL, 0,
        'VISIBLE', '{"icon":"roles"}', '/integration/system/roles', NULL, 'Integration role menu'
    ),
    (
        9100000000000001006, 9100000000000001005, 9, 10, 'Integration Role View', 'sys:role:view', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration role view permission'
    ),
    (
        9100000000000001007, 9100000000000001005, 11, 12, 'Integration Role Edit', 'sys:role:edit', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration role edit permission'
    ),
    (
        9100000000000001008, 9100000000000001001, 14, 19, 'Integration Departments', NULL, 0,
        'VISIBLE', '{"icon":"departments"}', '/integration/system/departments', NULL, 'Integration department menu'
    ),
    (
        9100000000000001009, 9100000000000001008, 15, 16, 'Integration Department View', 'sys:department:view', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration department view permission'
    ),
    (
        9100000000000001010, 9100000000000001008, 17, 18, 'Integration Department Edit', 'sys:department:edit', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration department edit permission'
    ),
    (
        9100000000000001011, 9100000000000001001, 20, 25, 'Integration Dictionaries', NULL, 0,
        'VISIBLE', '{"icon":"dictionaries"}', '/integration/system/dictionaries', NULL, 'Integration dictionary menu'
    ),
    (
        9100000000000001012, 9100000000000001011, 21, 22, 'Integration Dict View', 'sys:dict:view', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration dictionary view permission'
    ),
    (
        9100000000000001013, 9100000000000001011, 23, 24, 'Integration Dict Edit', 'sys:dict:edit', 0,
        'HIDDEN', '{"icon":"permission"}', NULL, NULL, 'Integration dictionary edit permission'
    ),
    (
        9100000000000001014, 9100000000000001001, 26, 27, 'Integration Audit View', 'audit:view', 0,
        'HIDDEN', '{"icon":"audit"}', NULL, NULL, 'Integration audit view permission'
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
    (9100000000000000401, 9100000000000001008),
    (9100000000000000401, 9100000000000001009),
    (9100000000000000401, 9100000000000001010),
    (9100000000000000401, 9100000000000001011),
    (9100000000000000401, 9100000000000001012),
    (9100000000000000401, 9100000000000001013),
    (9100000000000000401, 9100000000000001014),
    (9100000000000000402, 9100000000000001001),
    (9100000000000000402, 9100000000000001002),
    (9100000000000000402, 9100000000000001003),
    (9100000000000000402, 9100000000000001005),
    (9100000000000000402, 9100000000000001006),
    (9100000000000000402, 9100000000000001008),
    (9100000000000000402, 9100000000000001009),
    (9100000000000000402, 9100000000000001011),
    (9100000000000000402, 9100000000000001012),
    (9100000000000000402, 9100000000000001014)
ON DUPLICATE KEY UPDATE
    `menu_id` = VALUES(`menu_id`);
