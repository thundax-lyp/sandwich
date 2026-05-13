-- Execute after db/data/system.sql so user_id and identity_id references are stable.
-- Default principal accounts:
--   admin-api: developer / Q1w2e3r$
--   front-api: member / Q1w2e3r$
-- Default OAuth2 admin client:
--   client_id: sandwich-admin-web
--   client_secret hash is a seed placeholder and must be replaced for real environments.

INSERT INTO `auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (
        1000000000000010101, 'USER', 1000000000000000101, 'USER_ACCOUNT', 'developer', 'ENABLED'
    ),
    (
        1000000000000010103, 'USER', 1000000000000000102, 'USER_ACCOUNT', 'lin.zhiyuan', 'ENABLED'
    ),
    (
        1000000000000010104, 'USER', 1000000000000000103, 'USER_ACCOUNT', 'zhou.chengce', 'ENABLED'
    ),
    (
        1000000000000010105, 'USER', 1000000000000000104, 'USER_ACCOUNT', 'gu.qinghe', 'ENABLED'
    ),
    (
        1000000000000010106, 'USER', 1000000000000000105, 'USER_ACCOUNT', 'xu.mubai', 'ENABLED'
    ),
    (
        1000000000000010107, 'USER', 1000000000000000106, 'USER_ACCOUNT', 'chen.bozhou', 'ENABLED'
    ),
    (
        1000000000000010108, 'USER', 1000000000000000107, 'USER_ACCOUNT', 'ye.wanqing', 'ENABLED'
    ),
    (
        1000000000000010109, 'USER', 1000000000000000108, 'USER_ACCOUNT', 'tang.yining', 'ENABLED'
    ),
    (
        1000000000000010110, 'USER', 1000000000000000109, 'USER_ACCOUNT', 'shen.jiamu', 'DISABLED'
    ),
    (
        1000000000000010111, 'USER', 1000000000000000110, 'USER_ACCOUNT', 'lu.jingming', 'ENABLED'
    ),
    (
        1000000000000010112, 'USER', 1000000000000000111, 'USER_ACCOUNT', 'jiang.shuyao', 'ENABLED'
    ),
    (
        1000000000000010113, 'USER', 1000000000000000112, 'USER_ACCOUNT', 'song.yian', 'ENABLED'
    ),
    (
        1000000000000010114, 'USER', 1000000000000000113, 'USER_ACCOUNT', 'han.xingye', 'ENABLED'
    ),
    (
        1000000000000010115, 'USER', 1000000000000000114, 'USER_ACCOUNT', 'qin.ruochuan', 'ENABLED'
    ),
    (
        1000000000000010116, 'USER', 1000000000000000115, 'USER_ACCOUNT', 'cheng.yumo', 'ENABLED'
    ),
    (
        1000000000000010117, 'USER', 1000000000000000116, 'USER_ACCOUNT', 'zhao.beichen', 'ENABLED'
    ),
    (
        1000000000000010118, 'USER', 1000000000000000117, 'USER_ACCOUNT', 'he.wangshu', 'ENABLED'
    ),
    (
        1000000000000010119, 'USER', 1000000000000000118, 'USER_ACCOUNT', 'qiu.mingche', 'ENABLED'
    ),
    (
        1000000000000010120, 'USER', 1000000000000000119, 'USER_ACCOUNT', 'liang.siheng', 'ENABLED'
    ),
    (
        1000000000000010121, 'USER', 1000000000000000120, 'USER_ACCOUNT', 'luo.zijin', 'ENABLED'
    ),
    (
        1000000000000010122, 'USER', 1000000000000000121, 'USER_ACCOUNT', 'su.yunzhou', 'ENABLED'
    ),
    (
        1000000000000010123, 'USER', 1000000000000000122, 'USER_ACCOUNT', 'meng.qinglan', 'ENABLED'
    ),
    (
        1000000000000010124, 'USER', 1000000000000000123, 'USER_ACCOUNT', 'bai.jingxing', 'ENABLED'
    ),
    (
        1000000000000010125, 'USER', 1000000000000000124, 'USER_ACCOUNT', 'xia.ange', 'DISABLED'
    ),
    (
        1000000000000010126, 'USER', 1000000000000000125, 'USER_ACCOUNT', 'ji.xinghe', 'ENABLED'
    ),
    (
        1000000000000010127, 'USER', 1000000000000000126, 'USER_ACCOUNT', 'yu.qingyue', 'ENABLED'
    ),
    (
        1000000000000010128, 'USER', 1000000000000000127, 'USER_ACCOUNT', 'feng.ruogu', 'ENABLED'
    ),
    (
        1000000000000010129, 'USER', 1000000000000000128, 'USER_ACCOUNT', 'wei.nanfeng', 'ENABLED'
    ),
    (
        1000000000000010130, 'USER', 1000000000000000129, 'USER_ACCOUNT', 'ding.xiaotang', 'ENABLED'
    ),
    (
        1000000000000010131, 'USER', 1000000000000000130, 'USER_ACCOUNT', 'ma.yanqiu', 'ENABLED'
    ),
    (
        1000000000000010132, 'USER', 1000000000000000131, 'USER_ACCOUNT', 'xie.changfeng', 'ENABLED'
    ),
    (
        1000000000000010133, 'USER', 1000000000000000132, 'USER_ACCOUNT', 'jiang.nanxing', 'ENABLED'
    ),
    (
        1000000000000010134, 'USER', 1000000000000000133, 'USER_ACCOUNT', 'yuan.chuxia', 'DISABLED'
    ),
    (
        1000000000000010135, 'USER', 1000000000000000134, 'USER_ACCOUNT', 'cao.yuanshan', 'ENABLED'
    ),
    (
        1000000000000010136, 'USER', 1000000000000000135, 'USER_ACCOUNT', 'ren.lanxi', 'ENABLED'
    ),
    (
        1000000000000010102, 'MEMBER', 1000000000000020001, 'MEMBER_ACCOUNT', 'member', 'ENABLED'
    )
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `auth_principal_credential` (
    `id`, `principal_type`, `principal_id`, `identity_id`, `credential_type`, `credential_value`, `status`,
    `need_change_password`, `failed_count`, `failed_limit`,
    `locked_until`, `expires_at`, `last_verified_at`
) VALUES
    (
        1000000000000010201, 'USER', 1000000000000000101, 1000000000000010101, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010203, 'USER', 1000000000000000102, 1000000000000010103, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010204, 'USER', 1000000000000000103, 1000000000000010104, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010205, 'USER', 1000000000000000104, 1000000000000010105, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010206, 'USER', 1000000000000000105, 1000000000000010106, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010207, 'USER', 1000000000000000106, 1000000000000010107, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010208, 'USER', 1000000000000000107, 1000000000000010108, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010209, 'USER', 1000000000000000108, 1000000000000010109, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010210, 'USER', 1000000000000000109, 1000000000000010110, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'LOCKED',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010211, 'USER', 1000000000000000110, 1000000000000010111, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010212, 'USER', 1000000000000000111, 1000000000000010112, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010213, 'USER', 1000000000000000112, 1000000000000010113, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010214, 'USER', 1000000000000000113, 1000000000000010114, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010215, 'USER', 1000000000000000114, 1000000000000010115, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010216, 'USER', 1000000000000000115, 1000000000000010116, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010217, 'USER', 1000000000000000116, 1000000000000010117, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010218, 'USER', 1000000000000000117, 1000000000000010118, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010219, 'USER', 1000000000000000118, 1000000000000010119, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010220, 'USER', 1000000000000000119, 1000000000000010120, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010221, 'USER', 1000000000000000120, 1000000000000010121, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010222, 'USER', 1000000000000000121, 1000000000000010122, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010223, 'USER', 1000000000000000122, 1000000000000010123, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010224, 'USER', 1000000000000000123, 1000000000000010124, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010225, 'USER', 1000000000000000124, 1000000000000010125, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'LOCKED',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010226, 'USER', 1000000000000000125, 1000000000000010126, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010227, 'USER', 1000000000000000126, 1000000000000010127, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010228, 'USER', 1000000000000000127, 1000000000000010128, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010229, 'USER', 1000000000000000128, 1000000000000010129, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010230, 'USER', 1000000000000000129, 1000000000000010130, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010231, 'USER', 1000000000000000130, 1000000000000010131, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010232, 'USER', 1000000000000000131, 1000000000000010132, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010233, 'USER', 1000000000000000132, 1000000000000010133, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010234, 'USER', 1000000000000000133, 1000000000000010134, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'LOCKED',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010235, 'USER', 1000000000000000134, 1000000000000010135, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010236, 'USER', 1000000000000000135, 1000000000000010136, 'USER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    ),
    (
        1000000000000010202, 'MEMBER', 1000000000000020001, 1000000000000010102, 'MEMBER_PASSWORD',
        'ENC(fdf7,030f84263f1a4eef98be3a7689f52385a2d1bf1fe83af14ecdcc66948b39af75)', 'ACTIVE',
        1, 0, 5,
        NULL, NULL, NULL
    )
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`),
    `locked_until` = VALUES(`locked_until`),
    `expires_at` = VALUES(`expires_at`),
    `last_verified_at` = VALUES(`last_verified_at`);

INSERT INTO `auth_oauth_client` (
    `id`, `client_id`, `client_secret_hash`, `client_name`, `client_type`,
    `grant_types`, `scopes`, `redirect_uris`,
    `access_token_ttl_seconds`, `refresh_token_ttl_seconds`,
    `status`, `contact`, `remark`
) VALUES (
    1000000000000010001, 'sandwich-admin-web', 'CHANGE_ME_CLIENT_SECRET_HASH', 'Sandwich Admin Web', 'CONFIDENTIAL',
    '["authorization_code","refresh_token"]',
    '["openid","profile","user.read"]',
    '["http://127.0.0.1:5173/login/oauth2/code/sandwich"]',
    7200, 2592000,
    'ENABLED', 'developer@sandwich.local', '系统初始化 OAuth2 client'
) ON DUPLICATE KEY UPDATE
    `client_secret_hash` = VALUES(`client_secret_hash`),
    `client_name` = VALUES(`client_name`),
    `client_type` = VALUES(`client_type`),
    `grant_types` = VALUES(`grant_types`),
    `scopes` = VALUES(`scopes`),
    `redirect_uris` = VALUES(`redirect_uris`),
    `access_token_ttl_seconds` = VALUES(`access_token_ttl_seconds`),
    `refresh_token_ttl_seconds` = VALUES(`refresh_token_ttl_seconds`),
    `status` = VALUES(`status`),
    `contact` = VALUES(`contact`),
    `remark` = VALUES(`remark`);
