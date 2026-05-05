CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` varchar(64) NOT NULL,
    `parent_id` varchar(64) DEFAULT NULL,
    `lft` int NOT NULL,
    `rgt` int NOT NULL,
    `name` varchar(128) NOT NULL,
    `short_name` varchar(128) DEFAULT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_sys_department_parent` (`parent_id`, `priority`),
    KEY `idx_sys_department_nested` (`lft`, `rgt`),
    KEY `idx_sys_department_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台部门表';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` varchar(64) NOT NULL,
    `department_id` varchar(64) DEFAULT NULL,
    `email` varchar(512) DEFAULT NULL,
    `mobile` varchar(512) DEFAULT NULL,
    `tel` varchar(64) DEFAULT NULL,
    `name` varchar(128) NOT NULL,
    `ranks` int NOT NULL DEFAULT 0,
    `super_flag` char(1) NOT NULL DEFAULT '0',
    `admin_flag` char(1) NOT NULL DEFAULT '0',
    `enable_flag` varchar(16) NOT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_sys_user_department` (`department_id`),
    KEY `idx_sys_user_status` (`enable_flag`, `priority`, `create_date`),
    KEY `idx_sys_user_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户主体表';

CREATE TABLE IF NOT EXISTS `sys_user_identity` (
    `id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `identity_type` varchar(16) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_identity_type_value` (`identity_type`, `identity_value`),
    KEY `idx_sys_user_identity_user` (`user_id`, `status`),
    KEY `idx_sys_user_identity_user_type` (`user_id`, `identity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户登录标识表';

CREATE TABLE IF NOT EXISTS `sys_user_credential` (
    `id` varchar(64) NOT NULL,
    `user_id` varchar(64) NOT NULL,
    `identity_id` varchar(64) NOT NULL,
    `credential_type` varchar(32) NOT NULL,
    `credential_value` varchar(1024) NOT NULL,
    `status` varchar(16) NOT NULL,
    `need_change_password` tinyint(1) NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failed_limit` int NOT NULL DEFAULT 5,
    `locked_until` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `last_verified_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_credential_identity_type` (`identity_id`, `credential_type`),
    KEY `idx_sys_user_credential_user` (`user_id`, `status`),
    KEY `idx_sys_user_credential_identity_status` (`identity_id`, `status`),
    KEY `idx_sys_user_credential_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户认证凭据表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `admin_flag` char(1) NOT NULL DEFAULT '0',
    `enable_flag` varchar(16) NOT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_sys_role_status` (`enable_flag`, `priority`, `create_date`),
    KEY `idx_sys_role_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` varchar(64) NOT NULL,
    `parent_id` varchar(64) DEFAULT NULL,
    `lft` int NOT NULL,
    `rgt` int NOT NULL,
    `name` varchar(128) NOT NULL,
    `perms` varchar(512) DEFAULT NULL,
    `ranks` int NOT NULL DEFAULT 0,
    `display_flag` varchar(16) NOT NULL,
    `display_params` text DEFAULT NULL,
    `url` varchar(512) DEFAULT NULL,
    `target` varchar(64) DEFAULT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_sys_menu_parent` (`parent_id`, `priority`),
    KEY `idx_sys_menu_nested` (`lft`, `rgt`),
    KEY `idx_sys_menu_display` (`display_flag`, `ranks`),
    KEY `idx_sys_menu_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台菜单表';

CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` varchar(64) NOT NULL,
    `type` varchar(128) NOT NULL,
    `label` varchar(128) NOT NULL,
    `value` varchar(255) NOT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_sys_dict_type` (`type`, `priority`, `create_date`),
    KEY `idx_sys_dict_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` varchar(64) NOT NULL,
    `user_id` varchar(64) DEFAULT NULL,
    `type` varchar(32) DEFAULT NULL,
    `log_date` datetime(3) NOT NULL,
    `title` varchar(255) DEFAULT NULL,
    `remote_addr` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `method` varchar(16) DEFAULT NULL,
    `request_uri` varchar(512) DEFAULT NULL,
    `request_params` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sys_log_date` (`log_date`),
    KEY `idx_sys_log_user` (`user_id`, `log_date`),
    KEY `idx_sys_log_type` (`type`, `log_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台系统日志表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` varchar(64) NOT NULL,
    `role_id` varchar(64) NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` varchar(64) NOT NULL,
    `menu_id` varchar(64) NOT NULL,
    PRIMARY KEY (`role_id`, `menu_id`),
    UNIQUE KEY `uk_sys_role_menu` (`role_id`, `menu_id`),
    KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关系表';
