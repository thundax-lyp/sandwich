SET NAMES utf8mb4;

-- Integration submission data.

INSERT INTO `submission_submission` (
    `id`, `title`, `content`, `status`, `priority`, `submitted_at`
) VALUES
    (
        9100000000000080001, 'Integration Submitted Content',
        'Integration baseline content with an image reference.', 'SUBMITTED', 91001,
        '2026-01-01 12:00:00.000'
    ),
    (
        9100000000000080002, 'Integration Approved Content',
        'Integration baseline approved content for query filters.', 'APPROVED', 91002,
        '2026-01-01 12:10:00.000'
    ),
    (
        9100000000000080003, 'Integration Rejected Content',
        'Integration baseline rejected content for status filters.', 'REJECTED', 91003,
        '2026-01-01 12:20:00.000'
    )
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `submitted_at` = VALUES(`submitted_at`);

INSERT INTO `submission_image` (
    `id`, `submission_id`, `storage_object_id`, `sort_order`
) VALUES
    (
        9100000000000080101, 9100000000000080001, 9100000000000070001, 1
    )
ON DUPLICATE KEY UPDATE
    `submission_id` = VALUES(`submission_id`),
    `storage_object_id` = VALUES(`storage_object_id`),
    `sort_order` = VALUES(`sort_order`);
