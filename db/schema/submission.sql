SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `submission_submission` (
    `id` bigint NOT NULL,
    `title` varchar(200) NOT NULL,
    `content` text NOT NULL,
    `source_client_id` varchar(128) NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'SUBMITTED',
    `priority` int NOT NULL DEFAULT 0,
    `submitted_at` datetime(3) NOT NULL,
    `last_status_changed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_submission_submission_priority` (`priority`),
    KEY `idx_submission_submission_status` (`status`, `priority`),
    KEY `idx_submission_submission_client` (`source_client_id`, `priority`),
    KEY `idx_submission_submission_submitted` (`submitted_at`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交内容主表';

CREATE TABLE IF NOT EXISTS `submission_image` (
    `id` bigint NOT NULL,
    `submission_id` bigint NOT NULL,
    `storage_object_id` bigint NOT NULL,
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_submission_image_order` (`submission_id`, `sort_order`),
    UNIQUE KEY `uk_submission_image_storage` (`submission_id`, `storage_object_id`),
    KEY `idx_submission_image_storage` (`storage_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交内容图片引用表';
