CREATE TABLE IF NOT EXISTS `auth_principal_identity` (
    `id` bigint NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` bigint NOT NULL,
    `identity_type` varchar(32) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_identity_type_value` (`identity_type`, `identity_value`),
    KEY `idx_auth_principal_identity_principal` (`principal_type`, `principal_id`, `status`),
    KEY `idx_auth_principal_identity_principal_type` (`principal_type`, `principal_id`, `identity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一认证主体登录标识表';

CREATE TABLE IF NOT EXISTS `auth_principal_credential` (
    `id` bigint NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` bigint NOT NULL,
    `identity_id` bigint NOT NULL,
    `credential_type` varchar(32) NOT NULL,
    `credential_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL,
    `need_change_password` tinyint(1) NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failed_limit` int NOT NULL DEFAULT 0,
    `locked_until` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `last_verified_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_credential_identity_type` (`identity_id`, `credential_type`),
    KEY `idx_auth_principal_credential_principal` (`principal_type`, `principal_id`, `status`),
    KEY `idx_auth_principal_credential_identity_status` (`identity_id`, `status`),
    KEY `idx_auth_principal_credential_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一认证主体凭据表';

CREATE TABLE IF NOT EXISTS `auth_principal_login_event` (
    `id` varchar(64) NOT NULL,
    `principal_type` varchar(32) DEFAULT NULL,
    `principal_id` bigint DEFAULT NULL,
    `client_id` varchar(64) NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `authentication_method` varchar(32) NOT NULL,
    `identity_type` varchar(32) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `ip` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `reason` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_auth_principal_login_event_principal_time` (`principal_type`, `principal_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_client_time` (`client_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_type_time` (`event_type`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一认证登录事件表';

CREATE TABLE IF NOT EXISTS `auth_session` (
    `id` bigint NOT NULL,
    `token` varchar(255) NOT NULL,
    `principal_type` varchar(16) NOT NULL,
    `principal_id` bigint NOT NULL,
    `identity_id` bigint NOT NULL,
    `identity_type` varchar(16) NOT NULL,
    `login_type` varchar(16) NOT NULL,
    `status` varchar(16) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `last_access_time` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `logout_at` datetime(3) DEFAULT NULL,
    `invalidate_reason` varchar(128) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_session_token` (`token`),
    KEY `idx_auth_session_principal_status` (`principal_type`, `principal_id`, `status`),
    KEY `idx_auth_session_identity` (`identity_id`, `identity_type`),
    KEY `idx_auth_session_expire` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台认证会话审计表';

CREATE TABLE IF NOT EXISTS `member_auth_session` (
    `id` bigint NOT NULL,
    `principal_type` varchar(16) NOT NULL,
    `principal_id` bigint NOT NULL,
    `identity_id` bigint DEFAULT NULL,
    `identity_type` varchar(16) DEFAULT NULL,
    `login_type` varchar(32) DEFAULT NULL,
    `status` varchar(16) NOT NULL,
    `issued_at` datetime(3) NOT NULL,
    `last_access_time` datetime(3) DEFAULT NULL,
    `expire_at` datetime(3) DEFAULT NULL,
    `logout_at` datetime(3) DEFAULT NULL,
    `invalidate_reason` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_member_auth_session_principal` (`principal_type`, `principal_id`, `status`),
    KEY `idx_member_auth_session_status` (`status`, `last_access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前台会员认证会话审计表';

CREATE TABLE IF NOT EXISTS `auth_oauth_client` (
    `id` bigint NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `client_secret_hash` varchar(255) NOT NULL,
    `client_name` varchar(128) NOT NULL,
    `client_type` varchar(32) NOT NULL,
    `grant_types` text NOT NULL,
    `scopes` text NOT NULL,
    `redirect_uris` text NOT NULL,
    `access_token_ttl_seconds` bigint NOT NULL,
    `refresh_token_ttl_seconds` bigint NOT NULL,
    `status` varchar(16) NOT NULL,
    `contact` varchar(128) DEFAULT NULL,
    `remark` varchar(255) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `create_by` varchar(64) DEFAULT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `update_by` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_oauth_client_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2客户端表';

CREATE TABLE IF NOT EXISTS `auth_oauth_authorization` (
    `id` bigint NOT NULL,
    `authorization_code` varchar(128) NOT NULL,
    `client_id` varchar(64) NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` bigint NOT NULL,
    `redirect_uri` varchar(512) NOT NULL,
    `scopes` text NOT NULL,
    `state` varchar(255) DEFAULT NULL,
    `code_challenge` varchar(128) DEFAULT NULL,
    `code_challenge_method` varchar(16) DEFAULT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expire_at` datetime(3) NOT NULL,
    `used` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_oauth_authorization_code` (`authorization_code`),
    KEY `idx_auth_oauth_authorization_client_principal` (`client_id`, `principal_type`, `principal_id`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2授权码表';
