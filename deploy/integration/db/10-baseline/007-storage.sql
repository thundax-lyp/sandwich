SET NAMES utf8mb4;

-- Integration storage objects and multipart upload data.

INSERT INTO `assist_storage` (
    `id`, `name`, `extend_name`, `mime_type`, `owner_id`, `owner_type`, `bucket_name`,
    `object_key`, `size`, `access_endpoint`, `object_status`, `reference_status`,
    `priority`, `remarks`
) VALUES
    (
        9100000000000070001, 'it-submission-image.jpg', 'jpg', 'image/jpeg',
        '9100000000000080001', 'SUBMISSION', 'sandwish-it',
        'integration/submission/it-submission-image.jpg', 1024, '/content/integration/submission/it-submission-image.jpg',
        'ACTIVE', 'REFERENCED', 9101, 'Integration submission image'
    ),
    (
        9100000000000070002, 'it-unreferenced.txt', 'txt', 'text/plain',
        '9100000000000060001', 'MEMBER', 'sandwish-it',
        'integration/member/it-unreferenced.txt', 128, '/content/integration/member/it-unreferenced.txt',
        'ACTIVE', 'UNREFERENCED', 9102, 'Integration unreferenced storage object'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `extend_name` = VALUES(`extend_name`),
    `mime_type` = VALUES(`mime_type`),
    `owner_id` = VALUES(`owner_id`),
    `owner_type` = VALUES(`owner_type`),
    `bucket_name` = VALUES(`bucket_name`),
    `object_key` = VALUES(`object_key`),
    `size` = VALUES(`size`),
    `access_endpoint` = VALUES(`access_endpoint`),
    `object_status` = VALUES(`object_status`),
    `reference_status` = VALUES(`reference_status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `assist_storage_business` (
    `file_id`, `reference_owner_id`, `reference_owner_type`, `business_params`, `reference_status`
) VALUES
    (
        9100000000000070001, '9100000000000080001', 'SUBMISSION',
        '{"source":"integration"}', 'REFERENCED'
    )
ON DUPLICATE KEY UPDATE
    `business_params` = VALUES(`business_params`),
    `reference_status` = VALUES(`reference_status`);

INSERT INTO `assist_storage_multipart_upload` (
    `id`, `upload_id`, `owner_id`, `owner_type`, `business_type`, `original_filename`, `mime_type`,
    `bucket_name`, `object_key`, `provider_upload_id`, `total_size`, `part_size`,
    `uploaded_part_count`, `upload_status`, `completed_date`, `aborted_date`
) VALUES
    (
        9100000000000070101, 'it-upload-completed', '9100000000000060001', 'MEMBER', 'avatar',
        'it-avatar.jpg', 'image/jpeg', 'sandwish-it', 'integration/upload/it-avatar.jpg',
        'provider-it-upload-completed', 2048, 1024, 2, 'COMPLETED', '2026-01-01 11:00:00.000', NULL
    )
ON DUPLICATE KEY UPDATE
    `owner_id` = VALUES(`owner_id`),
    `owner_type` = VALUES(`owner_type`),
    `business_type` = VALUES(`business_type`),
    `original_filename` = VALUES(`original_filename`),
    `mime_type` = VALUES(`mime_type`),
    `bucket_name` = VALUES(`bucket_name`),
    `object_key` = VALUES(`object_key`),
    `provider_upload_id` = VALUES(`provider_upload_id`),
    `total_size` = VALUES(`total_size`),
    `part_size` = VALUES(`part_size`),
    `uploaded_part_count` = VALUES(`uploaded_part_count`),
    `upload_status` = VALUES(`upload_status`),
    `completed_date` = VALUES(`completed_date`),
    `aborted_date` = VALUES(`aborted_date`);

INSERT INTO `assist_storage_multipart_upload_part` (
    `id`, `upload_id`, `part_number`, `etag`, `size`
) VALUES
    (9100000000000070201, 'it-upload-completed', 1, 'etag-it-part-1', 1024),
    (9100000000000070202, 'it-upload-completed', 2, 'etag-it-part-2', 1024)
ON DUPLICATE KEY UPDATE
    `upload_id` = VALUES(`upload_id`),
    `part_number` = VALUES(`part_number`),
    `etag` = VALUES(`etag`),
    `size` = VALUES(`size`);
