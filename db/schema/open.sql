SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `open_client` (
    `id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `ip_whitelist` text DEFAULT NULL,
    `expired_at` datetime(3) DEFAULT NULL,
    `remarks` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_open_client_status` (`status`, `id`),
    KEY `idx_open_client_expired` (`expired_at`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放接口第三方主体表';

CREATE TABLE IF NOT EXISTS `open_client_permission` (
    `id` bigint NOT NULL,
    `client_id` bigint NOT NULL,
    `permission` varchar(128) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_client_permission_client_permission` (`client_id`, `permission`),
    KEY `idx_open_client_permission_permission` (`permission`, `client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放接口第三方主体权限表';
