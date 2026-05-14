SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `member_member` (
    `id` bigint NOT NULL,
    `name` varchar(128) DEFAULT NULL,
    `gender` varchar(16) DEFAULT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'ACTIVE',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(500) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_member_priority` (`priority`),
    KEY `idx_member_member_status` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
