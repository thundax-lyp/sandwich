SET NAMES utf8mb4;

-- Integration dictionaries for admin-api system tests.

INSERT INTO `sys_dict` (
    `id`, `type`, `label`, `value`, `priority`, `remarks`
) VALUES
    (
        9100000000000000201, 'it.status', 'Enabled', 'ENABLED', 9101,
        'Integration enabled status'
    ),
    (
        9100000000000000202, 'it.status', 'Disabled', 'DISABLED', 9102,
        'Integration disabled status'
    ),
    (
        9100000000000000203, 'it.category', 'Regression', 'REGRESSION', 9103,
        'Integration regression category'
    ),
    (
        9100000000000000204, 'it.category', 'Smoke', 'SMOKE', 9104,
        'Integration smoke category'
    )
ON DUPLICATE KEY UPDATE
    `type` = VALUES(`type`),
    `label` = VALUES(`label`),
    `value` = VALUES(`value`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);
