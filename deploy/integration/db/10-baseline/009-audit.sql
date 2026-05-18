SET NAMES utf8mb4;

-- Integration audit metadata, audit logs and system logs.

INSERT INTO `audit_meta` (
    `id`, `object_type`, `object_id`, `version`, `last_log_id`, `last_action`,
    `last_operator_type`, `last_operator_id`, `last_operator_name`, `last_operated_at`,
    `created_log_id`, `created_at`
) VALUES
    (
        9100000000000090001, 'Department', '9100000000000000002', 2, 9100000000000090102, 'UPDATE',
        'USER', '9100000000000000101', 'Integration Admin', '2026-01-01 10:01:00.000',
        9100000000000090101, '2026-01-01 10:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `object_type` = VALUES(`object_type`),
    `object_id` = VALUES(`object_id`),
    `version` = VALUES(`version`),
    `last_log_id` = VALUES(`last_log_id`),
    `last_action` = VALUES(`last_action`),
    `last_operator_type` = VALUES(`last_operator_type`),
    `last_operator_id` = VALUES(`last_operator_id`),
    `last_operator_name` = VALUES(`last_operator_name`),
    `last_operated_at` = VALUES(`last_operated_at`),
    `created_log_id` = VALUES(`created_log_id`),
    `created_at` = VALUES(`created_at`);

INSERT INTO `audit_log` (
    `id`, `meta_id`, `object_type`, `object_id`, `version`, `previous_version`, `action`,
    `idempotency_key`, `operator_type`, `operator_id`, `operator_name`, `source`,
    `request_id`, `trace_id`, `remote_addr`, `summary`, `snapshot_schema_version`,
    `before_snapshot`, `after_snapshot`, `changed_fields`, `occurred_at`
) VALUES
    (
        9100000000000090101, 9100000000000090001, 'Department', '9100000000000000002', 1, 0, 'CREATE',
        'it-audit-department-create', 'USER', '9100000000000000101', 'Integration Admin', 'SERVICE',
        'it-request-create', 'it-trace-create', '127.0.0.1', 'Create integration department', 1,
        'null',
        '{"schemaVersion":1,"objectType":"Department","objectId":"9100000000000000002","displayName":"Integration Engineering","fields":[{"fieldName":"name","fieldLabel":"Name","value":"Integration Engineering","displayValue":"Integration Engineering","valueType":"STRING","sensitive":false}]}',
        '[]',
        '2026-01-01 10:00:00.000'
    ),
    (
        9100000000000090102, 9100000000000090001, 'Department', '9100000000000000002', 2, 1, 'UPDATE',
        'it-audit-department-update', 'USER', '9100000000000000101', 'Integration Admin', 'SERVICE',
        'it-request-update', 'it-trace-update', '127.0.0.1', 'Update integration department', 1,
        '{"schemaVersion":1,"objectType":"Department","objectId":"9100000000000000002","displayName":"Integration Engineering","fields":[{"fieldName":"name","fieldLabel":"Name","value":"Integration Engineering","displayValue":"Integration Engineering","valueType":"STRING","sensitive":false}]}',
        '{"schemaVersion":1,"objectType":"Department","objectId":"9100000000000000002","displayName":"Integration Engineering Team","fields":[{"fieldName":"name","fieldLabel":"Name","value":"Integration Engineering Team","displayValue":"Integration Engineering Team","valueType":"STRING","sensitive":false}]}',
        '[{"fieldName":"name","fieldLabel":"Name","beforeValue":"Integration Engineering","beforeDisplayValue":"Integration Engineering","afterValue":"Integration Engineering Team","afterDisplayValue":"Integration Engineering Team"}]',
        '2026-01-01 10:01:00.000'
    )
ON DUPLICATE KEY UPDATE
    `meta_id` = VALUES(`meta_id`),
    `object_type` = VALUES(`object_type`),
    `object_id` = VALUES(`object_id`),
    `version` = VALUES(`version`),
    `previous_version` = VALUES(`previous_version`),
    `action` = VALUES(`action`),
    `idempotency_key` = VALUES(`idempotency_key`),
    `operator_type` = VALUES(`operator_type`),
    `operator_id` = VALUES(`operator_id`),
    `operator_name` = VALUES(`operator_name`),
    `source` = VALUES(`source`),
    `request_id` = VALUES(`request_id`),
    `trace_id` = VALUES(`trace_id`),
    `remote_addr` = VALUES(`remote_addr`),
    `summary` = VALUES(`summary`),
    `snapshot_schema_version` = VALUES(`snapshot_schema_version`),
    `before_snapshot` = VALUES(`before_snapshot`),
    `after_snapshot` = VALUES(`after_snapshot`),
    `changed_fields` = VALUES(`changed_fields`),
    `occurred_at` = VALUES(`occurred_at`);

INSERT INTO `sys_log` (
    `id`, `user_id`, `type`, `log_date`, `title`, `remote_addr`, `user_agent`,
    `method`, `request_uri`, `request_params`
) VALUES
    (
        9100000000000090201, 9100000000000000101, 'ACCESS', '2026-01-01 10:02:00.000',
        'Integration admin system log', '127.0.0.1', 'integration-test',
        'POST', '/api/sys/log/page', '{}'
    ),
    (
        9100000000000090202, 9100000000000000102, 'ACCESS', '2026-01-01 10:03:00.000',
        'Integration user system log', '127.0.0.1', 'integration-test',
        'POST', '/api/sys/user/page', '{}'
    )
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `type` = VALUES(`type`),
    `log_date` = VALUES(`log_date`),
    `title` = VALUES(`title`),
    `remote_addr` = VALUES(`remote_addr`),
    `user_agent` = VALUES(`user_agent`),
    `method` = VALUES(`method`),
    `request_uri` = VALUES(`request_uri`),
    `request_params` = VALUES(`request_params`);
