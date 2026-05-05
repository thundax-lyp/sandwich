-- Default developer account:
--   account: developer
--   password hash is initialized from the deployment default password and must be rotated.

INSERT INTO `sys_department` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `short_name`,
    `priority`, `remarks`, `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES (
    'dept-root', NULL, 1, 2, 'GitHub', 'GitHub',
    0, '系统初始化部门', '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
) ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `lft` = VALUES(`lft`),
    `rgt` = VALUES(`rgt`),
    `name` = VALUES(`name`),
    `short_name` = VALUES(`short_name`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`),
    `del_flag` = VALUES(`del_flag`);

INSERT INTO `sys_user` (
    `id`, `department_id`, `email`, `mobile`, `tel`, `name`, `ranks`,
    `super_flag`, `admin_flag`, `enable_flag`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES (
    'user-developer', 'dept-root', NULL, NULL, NULL, '开发者', 0,
    '1', '1', 'ENABLED', 0, '系统初始化开发者用户',
    '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
) ON DUPLICATE KEY UPDATE
    `department_id` = VALUES(`department_id`),
    `name` = VALUES(`name`),
    `ranks` = VALUES(`ranks`),
    `super_flag` = VALUES(`super_flag`),
    `admin_flag` = VALUES(`admin_flag`),
    `enable_flag` = VALUES(`enable_flag`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`),
    `del_flag` = VALUES(`del_flag`);

INSERT INTO `sys_user_identity` (
    `id`, `user_id`, `identity_type`, `identity_value`, `status`
) VALUES (
    'identity-developer-account', 'user-developer', 'ACCOUNT', 'developer', 'ENABLED'
) ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `sys_user_credential` (
    `id`, `user_id`, `identity_id`, `credential_type`, `credential_value`, `status`,
    `need_change_password`, `failed_count`, `failed_limit`,
    `locked_until`, `expires_at`, `last_verified_at`
) VALUES (
    'credential-developer-password', 'user-developer', 'identity-developer-account', 'PASSWORD',
    'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
    1, 0, 5,
    NULL, NULL, NULL
) ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`),
    `locked_until` = VALUES(`locked_until`),
    `expires_at` = VALUES(`expires_at`),
    `last_verified_at` = VALUES(`last_verified_at`);

INSERT INTO `sys_role` (
    `id`, `name`, `admin_flag`, `enable_flag`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES (
    'role-super-admin', '超级管理员', '1', 'ENABLED', 0, '系统初始化角色',
    '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `admin_flag` = VALUES(`admin_flag`),
    `enable_flag` = VALUES(`enable_flag`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`),
    `del_flag` = VALUES(`del_flag`);

INSERT INTO `sys_menu` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `perms`, `ranks`,
    `display_flag`, `display_params`, `url`, `target`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES
    (
        'menu-system', NULL, 1, 12, '系统管理', 'system', 0,
        'VISIBLE', NULL, '/system', NULL, 0, '系统管理根菜单',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        'menu-user', 'menu-system', 2, 3, '用户管理', 'user', 0,
        'VISIBLE', NULL, '/system/users', NULL, 1, '用户管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        'menu-role', 'menu-system', 4, 5, '角色管理', 'role', 0,
        'VISIBLE', NULL, '/system/roles', NULL, 2, '角色管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        'menu-menu', 'menu-system', 6, 7, '菜单管理', 'menu', 0,
        'VISIBLE', NULL, '/system/menus', NULL, 3, '菜单管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        'menu-department', 'menu-system', 8, 9, '部门管理', 'department', 0,
        'VISIBLE', NULL, '/system/departments', NULL, 4, '部门管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        'menu-dict', 'menu-system', 10, 11, '字典管理', 'dict', 0,
        'VISIBLE', NULL, '/system/dicts', NULL, 5, '字典管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    )
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `lft` = VALUES(`lft`),
    `rgt` = VALUES(`rgt`),
    `name` = VALUES(`name`),
    `perms` = VALUES(`perms`),
    `ranks` = VALUES(`ranks`),
    `display_flag` = VALUES(`display_flag`),
    `display_params` = VALUES(`display_params`),
    `url` = VALUES(`url`),
    `target` = VALUES(`target`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`),
    `del_flag` = VALUES(`del_flag`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    ('user-developer', 'role-super-admin')
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
    ('role-super-admin', 'menu-system'),
    ('role-super-admin', 'menu-user'),
    ('role-super-admin', 'menu-role'),
    ('role-super-admin', 'menu-menu'),
    ('role-super-admin', 'menu-department'),
    ('role-super-admin', 'menu-dict')
ON DUPLICATE KEY UPDATE
    `menu_id` = VALUES(`menu_id`);

INSERT INTO `sys_dict` (
    `id`, `type`, `label`, `value`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES
    (
        'dict-user-status-enabled', 'user_status', '启用', 'ENABLED', 0, '用户启用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        'dict-user-status-disabled', 'user_status', '禁用', 'DISABLED', 1, '用户禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    )
ON DUPLICATE KEY UPDATE
    `type` = VALUES(`type`),
    `label` = VALUES(`label`),
    `value` = VALUES(`value`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`),
    `update_date` = VALUES(`update_date`),
    `update_by` = VALUES(`update_by`),
    `del_flag` = VALUES(`del_flag`);
