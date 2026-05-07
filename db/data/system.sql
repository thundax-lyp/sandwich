-- Default developer account:
--   account: developer
--   password hash is initialized from the deployment default password and must be rotated.

INSERT INTO `sys_department` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `short_name`,
    `priority`, `remarks`, `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES (
    1000000000000000001, NULL, 1, 2, 'GitHub', 'GitHub',
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
    1000000000000000101, 1000000000000000001, NULL, NULL, NULL, '开发者', 0,
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
    1000000000000000201, 1000000000000000101, 'ACCOUNT', 'developer', 'ENABLED'
) ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `sys_user_credential` (
    `id`, `user_id`, `identity_id`, `credential_type`, `credential_value`, `status`,
    `need_change_password`, `failed_count`, `failed_limit`,
    `locked_until`, `expires_at`, `last_verified_at`
) VALUES (
    1000000000000000301, 1000000000000000101, 1000000000000000201, 'PASSWORD',
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
    1000000000000000401, '超级管理员', '1', 'ENABLED', 0, '系统初始化角色',
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

-- Default navigation menus are permission-backed resources and must be seeded here.
-- Dashboard is the login landing page, not a menu resource, so it is intentionally not inserted.
INSERT INTO `sys_menu` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `perms`, `ranks`,
    `display_flag`, `display_params`, `url`, `target`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES
    (
        1000000000000001001, NULL, 1, 30, '系统管理', NULL, 0,
        'VISIBLE', NULL, '/system', NULL, 0, '系统管理根菜单',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001002, 1000000000000001001, 2, 7, '用户管理', NULL, 0,
        'VISIBLE', NULL, '/system/users', NULL, 1, '用户管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001003, 1000000000000001002, 3, 4, '用户查看', 'sys:user:view', 0,
        'HIDDEN', NULL, NULL, NULL, 1, '用户查看权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001004, 1000000000000001002, 5, 6, '用户编辑', 'sys:user:edit', 0,
        'HIDDEN', NULL, NULL, NULL, 2, '用户编辑权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001005, 1000000000000001001, 8, 13, '角色管理', NULL, 0,
        'VISIBLE', NULL, '/system/roles', NULL, 2, '角色管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001006, 1000000000000001005, 9, 10, '角色查看', 'sys:role:view', 0,
        'HIDDEN', NULL, NULL, NULL, 1, '角色查看权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001007, 1000000000000001005, 11, 12, '角色编辑', 'sys:role:edit', 0,
        'HIDDEN', NULL, NULL, NULL, 2, '角色编辑权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001008, 1000000000000001001, 14, 15, '菜单管理', 'super', 0,
        'VISIBLE', NULL, '/system/menus', NULL, 3, '菜单管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001009, 1000000000000001001, 16, 21, '部门管理', NULL, 0,
        'VISIBLE', NULL, '/system/departments', NULL, 4, '部门管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001010, 1000000000000001009, 17, 18, '部门查看', 'sys:department:view', 0,
        'HIDDEN', NULL, NULL, NULL, 1, '部门查看权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001011, 1000000000000001009, 19, 20, '部门编辑', 'sys:department:edit', 0,
        'HIDDEN', NULL, NULL, NULL, 2, '部门编辑权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001012, 1000000000000001001, 22, 27, '字典管理', NULL, 0,
        'VISIBLE', NULL, '/system/dictionaries', NULL, 5, '字典管理',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001013, 1000000000000001012, 23, 24, '字典查看', 'sys:dict:view', 0,
        'HIDDEN', NULL, NULL, NULL, 1, '字典查看权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001014, 1000000000000001012, 25, 26, '字典编辑', 'sys:dict:edit', 0,
        'HIDDEN', NULL, NULL, NULL, 2, '字典编辑权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001015, 1000000000000001001, 28, 29, '系统日志', 'super', 0,
        'VISIBLE', NULL, '/system/logs', NULL, 6, '系统日志',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001016, NULL, 31, 38, '存储管理', NULL, 0,
        'VISIBLE', NULL, '/storage', NULL, 1, '存储管理根菜单',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001017, 1000000000000001016, 32, 37, '存储对象', NULL, 0,
        'VISIBLE', NULL, '/storage/objects', NULL, 1, '存储对象',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001018, 1000000000000001017, 33, 34, '存储对象查看', 'assist:storage:view', 0,
        'HIDDEN', NULL, NULL, NULL, 1, '存储对象查看权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001019, 1000000000000001017, 35, 36, '存储对象编辑', 'assist:storage:edit', 0,
        'HIDDEN', NULL, NULL, NULL, 2, '存储对象编辑权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001020, NULL, 39, 44, '辅助工具', NULL, 0,
        'HIDDEN', NULL, NULL, NULL, 2, '辅助工具权限根节点',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001021, 1000000000000001020, 40, 41, '签名查看', 'assist:signature:view', 0,
        'HIDDEN', NULL, NULL, NULL, 1, '签名查看权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000001022, 1000000000000001020, 42, 43, '签名编辑', 'assist:signature:edit', 0,
        'HIDDEN', NULL, NULL, NULL, 2, '签名编辑权限',
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
    (1000000000000000101, 1000000000000000401)
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
    (1000000000000000401, 1000000000000001001),
    (1000000000000000401, 1000000000000001002),
    (1000000000000000401, 1000000000000001003),
    (1000000000000000401, 1000000000000001004),
    (1000000000000000401, 1000000000000001005),
    (1000000000000000401, 1000000000000001006),
    (1000000000000000401, 1000000000000001007),
    (1000000000000000401, 1000000000000001008),
    (1000000000000000401, 1000000000000001009),
    (1000000000000000401, 1000000000000001010),
    (1000000000000000401, 1000000000000001011),
    (1000000000000000401, 1000000000000001012),
    (1000000000000000401, 1000000000000001013),
    (1000000000000000401, 1000000000000001014),
    (1000000000000000401, 1000000000000001015),
    (1000000000000000401, 1000000000000001016),
    (1000000000000000401, 1000000000000001017),
    (1000000000000000401, 1000000000000001018),
    (1000000000000000401, 1000000000000001019),
    (1000000000000000401, 1000000000000001020),
    (1000000000000000401, 1000000000000001021),
    (1000000000000000401, 1000000000000001022)
ON DUPLICATE KEY UPDATE
    `menu_id` = VALUES(`menu_id`);

INSERT INTO `sys_dict` (
    `id`, `type`, `label`, `value`, `priority`, `remarks`,
    `create_date`, `create_by`, `update_date`, `update_by`, `del_flag`
) VALUES
    (
        1000000000000002001, 'user_status', '启用', 'ENABLED', 0, '用户启用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002002, 'user_status', '禁用', 'DISABLED', 1, '用户禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002003, 'user_privilege', '普通用户', 'NORMAL', 0, '后台普通用户权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002004, 'user_privilege', '管理员', 'ADMIN', 1, '后台管理员权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002005, 'user_privilege', '超级管理员', 'SUPER', 2, '后台超级管理员权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002006, 'user_identity_type', '账号', 'ACCOUNT', 0, '账号登录标识',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002007, 'user_identity_type', '手机号', 'MOBILE', 1, '手机号登录标识',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002008, 'user_identity_type', '邮箱', 'EMAIL', 2, '邮箱登录标识',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002009, 'user_identity_type', '企业微信', 'WECOM', 3, '企业微信登录标识',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002010, 'user_identity_type', 'GitHub', 'GITHUB', 4, 'GitHub 登录标识',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002011, 'user_identity_status', '启用', 'ENABLED', 0, '登录标识启用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002012, 'user_identity_status', '禁用', 'DISABLED', 1, '登录标识禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002013, 'user_credential_type', '密码', 'PASSWORD', 0, '密码认证凭据',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002014, 'user_credential_status', '可用', 'ACTIVE', 0, '认证凭据可用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002015, 'user_credential_status', '锁定', 'LOCKED', 1, '认证凭据锁定状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002016, 'user_credential_status', '过期', 'EXPIRED', 2, '认证凭据过期状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002017, 'user_credential_status', '禁用', 'DISABLED', 3, '认证凭据禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002018, 'role_status', '启用', 'ENABLED', 0, '角色启用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002019, 'role_status', '禁用', 'DISABLED', 1, '角色禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002020, 'role_privilege', '普通角色', 'NORMAL', 0, '普通角色权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002021, 'role_privilege', '管理员角色', 'ADMIN', 1, '管理员角色权限',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002022, 'menu_visibility', '显示', 'VISIBLE', 0, '菜单显示状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002023, 'menu_visibility', '隐藏', 'HIDDEN', 1, '菜单隐藏状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002024, 'log_type', '访问日志', 'ACCESS', 0, '系统访问日志',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002025, 'log_type', '异常日志', 'EXCEPTION', 1, '系统异常日志',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002026, 'member_status', '启用', 'ENABLED', 0, '会员启用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002027, 'member_status', '禁用', 'DISABLED', 1, '会员禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002028, 'auth_session_status', '活跃', 'ACTIVE', 0, '认证会话活跃状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002029, 'auth_session_status', '已登出', 'LOGGED_OUT', 1, '认证会话登出状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002030, 'auth_session_status', '已失效', 'INVALIDATED', 2, '认证会话失效状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002031, 'auth_session_status', '已过期', 'EXPIRED', 3, '认证会话过期状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002032, 'oauth_client_status', '启用', 'ENABLED', 0, 'OAuth2 客户端启用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002033, 'oauth_client_status', '禁用', 'DISABLED', 1, 'OAuth2 客户端禁用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002034, 'oauth_access_token_status', '有效', 'ACTIVE', 0, 'OAuth2 访问令牌有效状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002035, 'oauth_access_token_status', '已撤销', 'REVOKED', 1, 'OAuth2 访问令牌撤销状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002036, 'oauth_access_token_status', '已过期', 'EXPIRED', 2, 'OAuth2 访问令牌过期状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002037, 'oauth_refresh_token_status', '有效', 'ACTIVE', 0, 'OAuth2 刷新令牌有效状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002038, 'oauth_refresh_token_status', '已使用', 'USED', 1, 'OAuth2 刷新令牌已使用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002039, 'oauth_refresh_token_status', '已撤销', 'REVOKED', 2, 'OAuth2 刷新令牌撤销状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002040, 'oauth_refresh_token_status', '已过期', 'EXPIRED', 3, 'OAuth2 刷新令牌过期状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002041, 'storage_type', '本地文件', 'LOCAL_FILE', 0, '本地文件存储后端',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002042, 'storage_type', '对象存储', 'OSS', 1, '对象存储后端',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002043, 'storage_owner_type', '后台用户', 'USER', 0, '后台用户存储归属',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002044, 'storage_owner_type', '前台会员', 'MEMBER', 1, '前台会员存储归属',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002045, 'stored_object_status', '可用', 'ACTIVE', 0, '存储对象可用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002046, 'stored_object_status', '删除中', 'DELETING', 1, '存储对象删除中状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002047, 'stored_object_status', '已删除', 'DELETED', 2, '存储对象已删除状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002048, 'stored_object_reference_status', '未引用', 'UNREFERENCED', 0, '存储对象未引用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002049, 'stored_object_reference_status', '已引用', 'REFERENCED', 1, '存储对象已引用状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002050, 'multipart_upload_status', '已初始化', 'INITIATED', 0, '分片上传初始化状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002051, 'multipart_upload_status', '上传中', 'UPLOADING', 1, '分片上传进行中状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002052, 'multipart_upload_status', '已完成', 'COMPLETED', 2, '分片上传完成状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002053, 'multipart_upload_status', '已中止', 'ABORTED', 3, '分片上传中止状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002054, 'async_task_status', '空闲', 'IDLE', 0, '异步任务空闲状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002055, 'async_task_status', '执行中', 'ACTIVE', 1, '异步任务执行中状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002056, 'async_task_status', '已暂停', 'SUSPENDED', 2, '异步任务暂停状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002057, 'async_task_status', '成功', 'SUCCESS', 3, '异步任务成功状态',
        '2026-05-05 00:00:00.000', 'system', NULL, NULL, '0'
    ),
    (
        1000000000000002058, 'async_task_status', '失败', 'ERROR', 4, '异步任务失败状态',
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
