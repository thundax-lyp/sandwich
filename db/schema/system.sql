SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` bigint NOT NULL,
    `parent_id` bigint DEFAULT NULL,
    `lft` int NOT NULL,
    `rgt` int NOT NULL,
    `name` varchar(128) NOT NULL,
    `short_name` varchar(128) DEFAULT NULL,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sys_department_parent` (`parent_id`),
    KEY `idx_sys_department_nested` (`lft`, `rgt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台部门表';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint NOT NULL,
    `department_id` bigint DEFAULT NULL,
    `email` varchar(512) DEFAULT NULL,
    `mobile` varchar(512) DEFAULT NULL,
    `tel` varchar(64) DEFAULT NULL,
    `name` varchar(128) NOT NULL,
    `ranks` int NOT NULL DEFAULT 0,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sys_user_department` (`department_id`),
    KEY `idx_sys_user_status` (`status`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户主体表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sys_role_status` (`status`, `priority`),
    UNIQUE KEY `uk_sys_role_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` bigint NOT NULL,
    `parent_id` bigint DEFAULT NULL,
    `lft` int NOT NULL,
    `rgt` int NOT NULL,
    `name` varchar(128) NOT NULL,
    `perms` varchar(512) DEFAULT NULL,
    `ranks` int NOT NULL DEFAULT 0,
    `visibility` varchar(16) NOT NULL DEFAULT 'VISIBLE',
    `display_params` text DEFAULT NULL,
    `url` varchar(512) DEFAULT NULL,
    `target` varchar(64) DEFAULT NULL,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sys_menu_parent` (`parent_id`),
    KEY `idx_sys_menu_nested` (`lft`, `rgt`),
    KEY `idx_sys_menu_visibility` (`visibility`, `ranks`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台菜单表';

CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` bigint NOT NULL,
    `type` varchar(128) NOT NULL,
    `label` varchar(128) NOT NULL,
    `value` varchar(255) NOT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_dict_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` bigint NOT NULL,
    `user_id` bigint DEFAULT NULL,
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
    `user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` bigint NOT NULL,
    `menu_id` bigint NOT NULL,
    PRIMARY KEY (`role_id`, `menu_id`),
    UNIQUE KEY `uk_sys_role_menu` (`role_id`, `menu_id`),
    KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关系表';
