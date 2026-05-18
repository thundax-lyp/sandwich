SET NAMES utf8mb4;

-- Integration department tree for admin-api system tests.

INSERT INTO `sys_department` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `short_name`,
    `remarks`
) VALUES
    (
        9100000000000000001, NULL, 1, 8, 'Integration Company', 'IT Company',
        'Integration test root department'
    ),
    (
        9100000000000000002, 9100000000000000001, 2, 5, 'Integration Engineering', 'IT Engineering',
        'Integration test engineering department'
    ),
    (
        9100000000000000003, 9100000000000000002, 3, 4, 'Integration QA', 'IT QA',
        'Integration test quality department'
    ),
    (
        9100000000000000004, 9100000000000000001, 6, 7, 'Integration Operations', 'IT Ops',
        'Integration test operations department'
    )
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `lft` = VALUES(`lft`),
    `rgt` = VALUES(`rgt`),
    `name` = VALUES(`name`),
    `short_name` = VALUES(`short_name`),
    `remarks` = VALUES(`remarks`);

UPDATE `sys_user`
SET `department_id` = 9100000000000000002
WHERE `id` = 9100000000000000101;

UPDATE `sys_user`
SET `department_id` = 9100000000000000003
WHERE `id` = 9100000000000000102;
