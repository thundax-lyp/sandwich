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
