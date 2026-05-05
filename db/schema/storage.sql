CREATE TABLE IF NOT EXISTS `assist_storage` (
    `id` varchar(64) NOT NULL,
    `name` varchar(255) NOT NULL,
    `extend_name` varchar(64) DEFAULT NULL,
    `mime_type` varchar(128) DEFAULT NULL,
    `owner_id` varchar(64) DEFAULT NULL,
    `owner_type` varchar(64) DEFAULT NULL,
    `storage_type` varchar(32) NOT NULL,
    `bucket_name` varchar(128) DEFAULT NULL,
    `object_key` varchar(512) NOT NULL,
    `size` bigint NOT NULL,
    `access_endpoint` varchar(1024) DEFAULT NULL,
    `object_status` varchar(32) NOT NULL,
    `reference_status` varchar(32) NOT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `create_date` datetime(3) NOT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `del_flag` char(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assist_storage_key` (`storage_type`, `bucket_name`, `object_key`),
    KEY `idx_assist_storage_status` (`object_status`, `reference_status`, `create_date`),
    KEY `idx_assist_storage_mime_type` (`mime_type`),
    KEY `idx_assist_storage_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一存储对象主数据表';

CREATE TABLE IF NOT EXISTS `assist_storage_business` (
    `file_id` varchar(64) NOT NULL,
    `reference_owner_id` varchar(64) NOT NULL,
    `reference_owner_type` varchar(64) NOT NULL,
    `business_params` varchar(1024) DEFAULT NULL,
    `reference_status` varchar(32) NOT NULL,
    PRIMARY KEY (`file_id`, `reference_owner_type`, `reference_owner_id`),
    UNIQUE KEY `uk_assist_storage_business_owner` (`file_id`, `reference_owner_type`, `reference_owner_id`),
    KEY `idx_assist_storage_business_owner` (`reference_owner_type`, `reference_owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象引用关系表';

CREATE TABLE IF NOT EXISTS `assist_storage_multipart_upload` (
    `id` varchar(64) NOT NULL,
    `upload_id` varchar(64) NOT NULL,
    `owner_id` varchar(64) NOT NULL,
    `owner_type` varchar(64) NOT NULL,
    `business_type` varchar(64) DEFAULT NULL,
    `original_filename` varchar(255) NOT NULL,
    `mime_type` varchar(128) NOT NULL,
    `storage_type` varchar(32) NOT NULL,
    `bucket_name` varchar(128) DEFAULT NULL,
    `object_key` varchar(512) NOT NULL,
    `provider_upload_id` varchar(128) DEFAULT NULL,
    `total_size` bigint NOT NULL,
    `part_size` bigint NOT NULL,
    `uploaded_part_count` int NOT NULL DEFAULT 0,
    `upload_status` varchar(32) NOT NULL,
    `create_date` datetime(3) NOT NULL,
    `update_date` datetime(3) DEFAULT NULL,
    `completed_date` datetime(3) DEFAULT NULL,
    `aborted_date` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assist_storage_multipart_upload_upload_id` (`upload_id`),
    KEY `idx_assist_storage_multipart_upload_object_key` (`storage_type`, `bucket_name`, `object_key`),
    KEY `idx_assist_storage_multipart_upload_owner` (`owner_type`, `owner_id`, `upload_status`, `create_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传会话表';

CREATE TABLE IF NOT EXISTS `assist_storage_multipart_upload_part` (
    `id` varchar(64) NOT NULL,
    `upload_id` varchar(64) NOT NULL,
    `part_number` int NOT NULL,
    `etag` varchar(128) NOT NULL,
    `size` bigint NOT NULL,
    `create_date` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assist_storage_multipart_upload_part` (`upload_id`, `part_number`),
    KEY `idx_assist_storage_multipart_upload_part_upload_id` (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传分片表';
