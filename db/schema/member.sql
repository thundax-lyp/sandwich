CREATE TABLE IF NOT EXISTS `member_member` (
    `id` bigint NOT NULL,
    `name` varchar(128) DEFAULT NULL,
    `gender` varchar(16) DEFAULT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'ACTIVE',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(500) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_member_member_status` (`status`, `priority`, `create_date`),
    KEY `idx_member_member_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `member_identity` (
    `id` bigint NOT NULL,
    `member_id` bigint NOT NULL,
    `identity_type` varchar(16) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_identity_type_value` (`identity_type`, `identity_value`),
    KEY `idx_member_identity_member` (`member_id`, `status`),
    KEY `idx_member_identity_member_type` (`member_id`, `identity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `member_credential` (
    `id` bigint NOT NULL,
    `member_id` bigint NOT NULL,
    `identity_id` bigint NOT NULL,
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
    UNIQUE KEY `uk_member_credential_identity_type` (`identity_id`, `credential_type`),
    KEY `idx_member_credential_member` (`member_id`, `status`),
    KEY `idx_member_credential_identity_status` (`identity_id`, `status`),
    KEY `idx_member_credential_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `member_auth_session` (
    `id` bigint NOT NULL,
    `session_id` varchar(64) NOT NULL,
    `member_id` bigint NOT NULL,
    `identity_id` bigint DEFAULT NULL,
    `identity_type` varchar(16) DEFAULT NULL,
    `login_type` varchar(32) DEFAULT NULL,
    `status` varchar(16) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `last_access_time` datetime(3) DEFAULT NULL,
    `expire_at` datetime(3) DEFAULT NULL,
    `logout_at` datetime(3) DEFAULT NULL,
    `invalidate_reason` varchar(255) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_auth_session_session` (`session_id`),
    KEY `idx_member_auth_session_member` (`member_id`, `status`),
    KEY `idx_member_auth_session_status` (`status`, `last_access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `member_access_token` (
    `id` bigint NOT NULL,
    `token_id` varchar(64) NOT NULL,
    `token_hash` varchar(128) NOT NULL,
    `session_id` varchar(64) NOT NULL,
    `member_id` bigint NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `status` varchar(16) NOT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_access_token_id` (`token_id`),
    UNIQUE KEY `uk_member_access_token_hash` (`token_hash`),
    KEY `idx_member_access_token_member` (`member_id`, `status`),
    KEY `idx_member_access_token_session` (`session_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `member_refresh_token` (
    `id` bigint NOT NULL,
    `token_id` varchar(64) NOT NULL,
    `token_hash` varchar(128) NOT NULL,
    `access_token_id` varchar(64) NOT NULL,
    `session_id` varchar(64) NOT NULL,
    `member_id` bigint NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `status` varchar(16) NOT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_refresh_token_id` (`token_id`),
    UNIQUE KEY `uk_member_refresh_token_hash` (`token_hash`),
    KEY `idx_member_refresh_token_member` (`member_id`, `status`),
    KEY `idx_member_refresh_token_session` (`session_id`, `status`),
    KEY `idx_member_refresh_token_access` (`access_token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
